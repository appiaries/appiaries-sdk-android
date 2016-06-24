package com.appiaries.baas.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.SharedPreferencesTokenCachingStrategy;
import com.facebook.TokenCachingStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;

/**
 * Facebook用認証プロバイダ
 *
 * @author Appiaries Corporation
 * @since 1.3.0
 */
class ABFacebookAuthenticationProvider implements ABAuthenticationProvider {

    private static String TAG = ABFacebookAuthenticationProvider.class.getSimpleName();

    /**
     * Facebook用認証プロバイダID
     * @since 1.4.0
     */
    public static String ID = "facebook";

    private Context applicationContext;
    private String applicationId;
    private Session session;
    private int activityCode;
    private WeakReference<Activity> baseContext;
    private Collection<String> permissions;
    private AuthenticationCallback currentOperationCallback;
    private final DateFormat preciseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    private String userId;
    private SessionDefaultAudience defaultAudience;

    protected static String JSON_KEY_FACEBOOK_ID = "id";
    protected static String JSON_KEY_ACCESS_TOKEN = "access_token";
    protected static String JSON_KEY_EXPIRATION_DATE = "expiration_date"; //NOTE: Facebookからのレスンポンスキー
    protected static String JSON_KEY_EXPIRES_AT = "expires_at"; //NOTE: アピアリーズへのリクエストキー

    /**
     * コンストラクタ。
     * <p></p>
     *
     * @param context       コンテキスト
     * @param applicationId FacebookアプリID
     */
    public ABFacebookAuthenticationProvider(Context context, String applicationId) {
        this.preciseDateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
        this.activityCode = Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE;
        this.applicationId = applicationId;
        if (context != null) {
            this.applicationContext = context.getApplicationContext();
        }
    }

    @Override
    public synchronized void authenticate(AuthenticationCallback pCallback) {
        if (this.currentOperationCallback != null) {
            cancel();
        }
        this.currentOperationCallback = pCallback;

        Activity activity = null;
        if (this.baseContext != null) {
            activity = this.baseContext.get();
        }
        if (activity == null) {
            throw new IllegalStateException("Constructor argument 'context' cannot specify null value.");
        }

        TokenCachingStrategy strategy = new SharedPreferencesTokenCachingStrategy(activity);
        this.session = new Session.Builder(activity).setApplicationId(this.applicationId).setTokenCachingStrategy(strategy).build();

        Session.OpenRequest openRequest = new Session.OpenRequest(activity);
        openRequest.setRequestCode(this.activityCode);
        if (this.defaultAudience != null) {
            openRequest.setDefaultAudience(this.defaultAudience);
        }
        if (this.permissions != null) {
            openRequest.setPermissions(new ArrayList<String>(this.permissions));
        }
        openRequest.setCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState sessionState, Exception e) {
                if (SessionState.OPENING == sessionState) {
                    ABLog.d(TAG, "Facebook Session is opening");
                    return;
                }

                if (sessionState.isOpened()) {
                    if (ABFacebookAuthenticationProvider.this.currentOperationCallback == null) {
                        return;
                    }

                    Request meReq = Request.newGraphPathRequest(session, "me", new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            if (response.getError() != null) {
                                FacebookException fbException = response.getError().getException();
                                if (fbException != null) {
                                    ABFacebookAuthenticationProvider.this.handleError(fbException);
                                } else {
                                    ABException ex = new ABException(-9001, "An error occurred on Facebook SDK.");
                                    ABFacebookAuthenticationProvider.this.handleError(ex);
                                }
                            } else {
                                String fetchedId = (String) response.getGraphObject().getProperty(JSON_KEY_FACEBOOK_ID);
                                ABFacebookAuthenticationProvider.this.handleSuccess(fetchedId);
                            }
                        }
                    });
                    meReq.getParameters().putString("fields", JSON_KEY_FACEBOOK_ID);
                    meReq.executeAsync();
                } else if (e != null) {
                    ABFacebookAuthenticationProvider.this.handleError(e);
                } else {
                    ABFacebookAuthenticationProvider.this.handleCancel();
                }
            }
        });
        this.session.openForRead(openRequest);
    }

    @Override
    public void deauthenticate() {
        restoreAuthentication(null);
    }

    @Override
    public boolean restoreAuthentication(Map<String, Object> authData) {
        if (authData == null) {
            this.session = null;
            return true;
        }
        try {
            JSONObject json = new JSONObject(authData);
            Date expirationDate = this.preciseDateFormat.parse(json.getJSONObject(JSON_KEY_EXPIRATION_DATE).getString("iso"));
            TokenCachingStrategy strategy = new SharedPreferencesTokenCachingStrategy(this.applicationContext);
            Bundle data = strategy.load();
            TokenCachingStrategy.putToken(data, json.getString(JSON_KEY_ACCESS_TOKEN));
            TokenCachingStrategy.putExpirationDate(data, expirationDate);
            strategy.save(data);

            Session newSession = new Session.Builder(this.applicationContext).setApplicationId(this.applicationId).setTokenCachingStrategy(strategy).build();
            if (newSession.getState() == SessionState.CREATED_TOKEN_LOADED) {
                newSession.openForRead(null);
                this.session = newSession;
                Session.setActiveSession(this.session);
            } else {
                this.session = null;
            }
            return true;
        } catch (Exception e) {
            ABLog.e(TAG, "Failed to restore facebook authentication data from SharedPreferences.", e);
            return false;
        }
    }

    @Override
    public synchronized void cancel() {
        handleCancel();
    }

    @Override
    public void logOut() {
        if (this.session == null) {
            TokenCachingStrategy strategy = new SharedPreferencesTokenCachingStrategy(this.applicationContext);
            this.session = new Session.Builder(this.applicationContext).setApplicationId(this.applicationId).setTokenCachingStrategy(strategy).build();
        }
        this.session.closeAndClearTokenInformation();
        this.session = null;
    }

    /**
     * 認証後処理。
     * <p>認証処理を完了させるために、呼び出し元 Activity の onActivityResult() から実行する。</p>
     *
     * @param requestCode リクエストコード
     * @param resultCode  結果コード
     * @param data        結果データ
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Activity activity = this.baseContext.get();
        if (activity != null) {
            this.session.onActivityResult(activity, requestCode, resultCode, data);
        }
    }

    /// Handlers ///

    private void handleSuccess(String userId) {
        if (this.currentOperationCallback == null) {
            return;
        }

        this.userId = userId;
        Map<String, Object> authData;
        try {
            authData = getAuthData(userId, this.session.getAccessToken(), this.session.getExpirationDate());
        } catch (JSONException e) {
            handleError(e);
            return;
        }
        try {
            this.currentOperationCallback.onSuccess(authData);
        } finally {
            this.currentOperationCallback = null;
        }
    }

    private void handleError(Throwable error) {
        if (this.currentOperationCallback == null) {
            return;
        }

        try {
            this.currentOperationCallback.onError(error);
        } finally {
            this.currentOperationCallback = null;
        }
    }

    private void handleCancel() {
        if (this.currentOperationCallback == null) {
            return;
        }

        try {
            this.currentOperationCallback.onCancel();
        } finally {
            this.currentOperationCallback = null;
        }
    }

    /// Accessors ///

    @Override
    public String getId() {
        return ABFacebookAuthenticationProvider.ID;
    }

    public int getActivityCode() {
        return this.activityCode;
    }

    public Map<String, Object> getAuthData(String facebookId, String accessToken, Date expirationDate) throws JSONException {
        Map<String, Object> authData = new HashMap<String, Object>();
        authData.put(JSON_KEY_FACEBOOK_ID, facebookId);
        authData.put(JSON_KEY_ACCESS_TOKEN, accessToken);
        authData.put(JSON_KEY_EXPIRES_AT, String.valueOf(expirationDate.getTime()));
        return authData;
    }

    public Session getSession() {
        return this.session;
    }

    public String getUserId() {
        return this.userId;
    }

    public synchronized void setActivity(Activity activity) {
        this.baseContext = new WeakReference<Activity>(activity);
    }

    public synchronized void setActivityCode(int activityCode) {
        this.activityCode = activityCode;
    }

    public synchronized void setPermissions(Collection<String> permissions) {
        this.permissions = permissions;
    }

}
