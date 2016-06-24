package com.appiaries.baas.sdk;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Twitter用認証プロバイダ
 *
 * @author Appiaries Corporation
 * @since 1.3.0
 */
class ABTwitterAuthenticationProvider implements ABAuthenticationProvider {

    /**
     * Twitter用認証プロバイダID
     * @since 1.4.0
     */
    public static String ID = "twitter";
    protected static String JSON_KEY_OAUTH_TOKEN = "oauth_token";
    protected static String JSON_KEY_OAUTH_TOKEN_SECRET = "oauth_token_secret";
    protected static String JSON_KEY_OAUTH_CONSUMER_KEY = "consumer_key";
    protected static String JSON_KEY_OAUTH_CONSUMER_SECRET = "consumer_secret";
    protected static String JSON_KEY_OAUTH_USER_ID = "id";
    protected static String JSON_KEY_OAUTH_SCREEN_NAME = "screen_name";

    private Context applicationContext;
    private WeakReference<Context> baseContext;
    private final ABTwitter twitter;
    private AuthenticationCallback currentOperationCallback;

    /**
     * コンストラクタ。
     * <p></p>
     *
     * @param context コンテキスト
     * @param twitter APISTwitterオブジェクト
     */
    public ABTwitterAuthenticationProvider(Context context, ABTwitter twitter) {
        this.applicationContext = context.getApplicationContext();
        this.twitter = twitter;
    }

    @Override
    public void authenticate(final AuthenticationCallback callback) {
        if (this.currentOperationCallback != null) {
            cancel();
        }
        this.currentOperationCallback = callback;

        Context context = this.baseContext == null ? null : this.baseContext.get();
        if (context == null) {
            throw new IllegalStateException("Constructor argument 'context' cannot specify null value.");
        }

        this.twitter.authorize(context, new ProviderCallback() {

            @Override
            public void onCancel() {
                ABTwitterAuthenticationProvider.this.handleCancel(callback);
            }

            @Override
            public void onFailure(Throwable error) {
                if (ABTwitterAuthenticationProvider.this.currentOperationCallback != callback) {
                    return;
                }
                try {
                    callback.onError(error);
                } finally {
                    ABTwitterAuthenticationProvider.this.currentOperationCallback = null;
                }
            }

            @Override
            public void onSuccess(Object result) {
                if (ABTwitterAuthenticationProvider.this.currentOperationCallback != callback) {
                    return;
                }
                try {
                    Map<String, Object> authData;
                    try {
                        authData =
                                ABTwitterAuthenticationProvider.this.getAuthData(
                                        ABTwitterAuthenticationProvider.this.twitter.getUserId(),
                                        ABTwitterAuthenticationProvider.this.twitter.getScreenName(),
                                        ABTwitterAuthenticationProvider.this.twitter.getAuthToken(),
                                        ABTwitterAuthenticationProvider.this.twitter.getAuthTokenSecret()
                                );
                    } catch (JSONException e) {
                        callback.onError(e);
                        ABTwitterAuthenticationProvider.this.currentOperationCallback = null;
                        return;
                    }
                    callback.onSuccess(authData);
                } finally {
                    ABTwitterAuthenticationProvider.this.currentOperationCallback = null;
                }
            }
        });
    }

    /**
     * JSON形式のTwitter認証情報を返す。
     * <p></p>
     *
     * @param userId          ユーザID
     * @param screenName      スクリーンネーム
     * @param authToken       OAuthトークン
     * @param authTokenSecret OAuthトークン・シークレット
     * @return JSON形式のTwitter認証情報
     * @throws JSONException
     */
    public Map<String, Object> getAuthData(String userId, String screenName, String authToken, String authTokenSecret) throws JSONException {
        Map<String, Object> authData = new HashMap<String, Object>();
        authData.put(JSON_KEY_OAUTH_TOKEN, authToken);
        authData.put(JSON_KEY_OAUTH_TOKEN_SECRET, authTokenSecret);
        authData.put(JSON_KEY_OAUTH_CONSUMER_KEY, this.twitter.getConsumerKey());
        authData.put(JSON_KEY_OAUTH_CONSUMER_SECRET, this.twitter.getConsumerSecret());
        authData.put(JSON_KEY_OAUTH_USER_ID, userId);
        authData.put(JSON_KEY_OAUTH_SCREEN_NAME, screenName);
        return authData;
    }

    @Override
    public void cancel() {
        handleCancel(this.currentOperationCallback);
    }

    @Override
    public void logOut() {
        this.twitter.clearAccessToken(this.applicationContext);
    }

    @Override
    public void deauthenticate() {
        this.twitter.setAuthToken(null);
        this.twitter.setAuthTokenSecret(null);
        this.twitter.setScreenName(null);
        this.twitter.setUserId(null);
    }

    /**
     * APISTwitterオブジェクトを返す。
     *
     * @return APISTwitterオブジェクト
     */
    public ABTwitter getTwitter() {
        return this.twitter;
    }

    @Override
    public boolean restoreAuthentication(Map<String, Object> authData) {
        if (authData == null) {
            this.twitter.setAuthToken(null);
            this.twitter.setAuthTokenSecret(null);
            this.twitter.setScreenName(null);
            this.twitter.setUserId(null);
            return true;
        }

        try {
            JSONObject json = new JSONObject(authData);
            this.twitter.setAuthToken(json.getString(JSON_KEY_OAUTH_TOKEN));
            this.twitter.setAuthTokenSecret(json.getString(JSON_KEY_OAUTH_TOKEN_SECRET));
            this.twitter.setUserId(json.getString(JSON_KEY_OAUTH_USER_ID));
            this.twitter.setScreenName(json.getString(JSON_KEY_OAUTH_SCREEN_NAME));
            return true;
        } catch (Exception ignored) {
        }

        return false;
    }

    /// Handlers ///

    private void handleCancel(AuthenticationCallback callback) {
        if ((this.currentOperationCallback != callback) || (callback == null)) {
            return;
        }
        try {
            callback.onCancel();
        } finally {
            this.currentOperationCallback = null;
        }
    }

    /// Accessors ///

    @Override
    public String getId() {
        return ABTwitterAuthenticationProvider.ID;
    }

    /**
     * コンテキストをセットする。
     * <p></p>
     *
     * @param context コンテキスト
     */
    public void setContext(Context context) {
        this.baseContext = new WeakReference<Context>(context);
    }

}
