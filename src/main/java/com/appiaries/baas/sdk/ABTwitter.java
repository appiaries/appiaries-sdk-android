package com.appiaries.baas.sdk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.CookieSyncManager;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Twitter API クライアント。
 *
 * @author Appiaries Corporation
 * @since 1.3.0
 */
public class ABTwitter {

    private static String TAG = ABTwitter.class.getSimpleName();

    private static String CALLBACK_URL = "twitter-oauth://complete";

    private String consumerKey;
    private String consumerSecret;
    private String authToken;
    private String authTokenSecret;
    private String userId;
    private String screenName;
    private TwitterFactory twitterFactory;

    /**
     * コンストラクタ。
     * <p>引数に Twitter のコンシューマ・キーとコンシューマ・シークレットを取ります。</p>
     * @param consumerKey    Twitterコンシューマ・キー
     * @param consumerSecret Twitterコンシューマ・シークレット
     */
    public ABTwitter(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;

        ConfigurationBuilder confBuilder = new ConfigurationBuilder();
        confBuilder.setOAuthConsumerKey(getConsumerKey());
        confBuilder.setOAuthConsumerSecret(getConsumerSecret());
        this.twitterFactory = new TwitterFactory(confBuilder.build());
    }

    /*
     * Twitter OAuth認証。
     * @param context  コンテキスト
     * @param callback コールバック
     */
    void authorize(Context context, ABAuthenticationProvider.ProviderCallback callback) {
        String consumerKey = getConsumerKey();
        String consumerSecret = getConsumerSecret();
        if (consumerKey == null || consumerKey.length() == 0 || consumerSecret == null || consumerSecret.length() == 0) {
            throw new IllegalStateException("Constructor arguments ('context' and 'consumerSecret') cannot specify null value.");
        }

        final Context f_context = context;
        final ABAuthenticationProvider.ProviderCallback f_callback = callback;
        final ProgressDialog f_progress = new ProgressDialog(f_context);
        f_progress.setMessage("ロード中..."); //XXX: localize

        AccessToken accessToken = loadAccessToken(f_context);
        if (accessToken != null) {
            AsyncTask<AccessToken, Void, User> task = new AsyncTask<AccessToken, Void, User>() {
                @Override
                protected User doInBackground(AccessToken... params) {
                    AccessToken accessToken = params[0];
                    Twitter twitter4j = twitterFactory.getInstance();
                    twitter4j.setOAuthAccessToken(accessToken);
                    User user = null;
                    try {
                        user = twitter4j.verifyCredentials();
                    } catch (TwitterException e) {
                        ABLog.e(TAG, "Twitter service or network is unavailable, or supplied credential is wrong");
                    }
                    if (user == null) {
                        clearAccessToken(f_context);
                    } else {
                        ABTwitter.this.setAuthToken(accessToken.getToken());
                        ABTwitter.this.setAuthTokenSecret(accessToken.getTokenSecret());
                        ABTwitter.this.setUserId(String.valueOf(accessToken.getUserId()));
                        ABTwitter.this.setScreenName(ABTwitter.this.screenName); //XXX: AccessTokenのコンストラクタに渡せないのでインスタンス変数を介してセットしている
                        f_callback.onSuccess(ABTwitter.this);
                    }
                    return user;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    f_progress.show();
                }

                @Override
                protected void onPostExecute(User user) {
                    super.onPostExecute(user);
                    f_progress.dismiss();
                    if (user == null) {
                        authorize(f_context, f_callback);
                    }
                }
            };
            task.execute(accessToken);
            return;
        }

        AsyncTask<Void, Void, RequestToken> task = new AsyncTask<Void, Void, RequestToken>() {
            private Throwable error;
            private twitter4j.Twitter twitter4j;
            private RequestToken requestToken;

            @Override
            protected void onPostExecute(final RequestToken requestToken) {
                super.onPostExecute(requestToken);
                try {
                    if (this.error != null) {
                        f_callback.onFailure(this.error);
                        return;
                    }
                    CookieSyncManager.createInstance(f_context);
                    ABTwitterLogInDialog dialog = new ABTwitterLogInDialog(
                            f_context,
                            requestToken.getAuthorizationURL(),
                            CALLBACK_URL,
                            "api.twitter",
                            new ABTwitterLogInDialog.FlowResultHandler() {

                                @Override
                                public void onCancel() {
                                    f_callback.onCancel();
                                }

                                @Override
                                public void onError(int errorCode, String description, String failingUrl) {
                                    f_callback.onFailure(new ABTwitterLogInException(errorCode, description, failingUrl));
                                }

                                @Override
                                public void onComplete(final String callbackUrl) {
                                    CookieSyncManager.getInstance().sync();
                                    Uri uri = Uri.parse(callbackUrl);
                                    final String verifier = uri.getQueryParameter("oauth_verifier");
                                    if (verifier == null) {
                                        f_callback.onCancel();
                                        return;
                                    }

                                    AsyncTask<Void, Void, twitter4j.auth.AccessToken> getTokenTask = new AsyncTask<Void, Void, AccessToken>() {
                                        private Throwable error;

                                        @Override
                                        protected AccessToken doInBackground(Void... params) {
                                            AccessToken accessToken = null;
                                            try {
                                                accessToken = twitter4j.getOAuthAccessToken(requestToken, verifier);
                                            } catch (Throwable t) {
                                                this.error = t;
                                            }
                                            return accessToken;
                                        }

                                        @Override
                                        protected void onPreExecute() {
                                            super.onPreExecute();
                                            f_progress.show();
                                        }

                                        @Override
                                        protected void onPostExecute(AccessToken accessToken) {
                                            super.onPostExecute(accessToken);
                                            try {
                                                if (this.error != null) {
                                                    f_callback.onFailure(this.error);
                                                    return;
                                                }
                                                try {
                                                    ABTwitter.this.setAuthToken(accessToken.getToken());
                                                    ABTwitter.this.setAuthTokenSecret(accessToken.getTokenSecret());
                                                    ABTwitter.this.setUserId(String.valueOf(accessToken.getUserId()));
                                                    ABTwitter.this.setScreenName(accessToken.getScreenName());
                                                    saveAccessToken(f_context, accessToken);
                                                    f_callback.onSuccess(ABTwitter.this);
                                                } catch (Throwable t) {
                                                    f_callback.onFailure(t);
                                                    f_progress.dismiss();
                                                }
                                            } finally {
                                                f_progress.dismiss();
                                            }
                                        }
                                    };
                                    getTokenTask.execute();
                                }
                            }
                    );
                    dialog.show();
                } finally {
                    f_progress.dismiss();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                f_progress.show();
            }

            @Override
            protected RequestToken doInBackground(Void... params) {
                try {
                    this.twitter4j = twitterFactory.getInstance();
                    this.requestToken = this.twitter4j.getOAuthRequestToken(CALLBACK_URL);
                    return requestToken;
                } catch (Throwable t) {
                    this.error = t;
                }
                return null;
            }

        };
        task.execute();
    }

    /* (Non-Public)
     * SharedPreferences に AccessToken を保存(永続化)する。
     * @param context     コンテキスト
     * @param accessToken AccessTokenオブジェクト
     */
    protected void saveAccessToken(final Context context, AccessToken accessToken) {
        SharedPreferences prefs = context.getSharedPreferences(AB.Preference.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AB.Preference.PREF_KEY_SESSION_TWITTER_TOKEN, accessToken.getToken());
        editor.putString(AB.Preference.PREF_KEY_SESSION_TWITTER_TOKEN_SECRET, accessToken.getTokenSecret());
        editor.putLong(AB.Preference.PREF_KEY_SESSION_TWITTER_USER_ID, accessToken.getUserId());
        editor.putString(AB.Preference.PREF_KEY_SESSION_TWITTER_SCREEN_NAME, accessToken.getScreenName());
        editor.apply();
    }

    /* (Non-Public)
     * SharedPreferences から AccessToken オブジェクトをロードする。
     * @param context コンテキスト
     * @return AccessTokenオブジェクト
     */
    protected AccessToken loadAccessToken(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AB.Preference.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(AB.Preference.PREF_KEY_SESSION_TWITTER_TOKEN, null);
        String tokenSecret = prefs.getString(AB.Preference.PREF_KEY_SESSION_TWITTER_TOKEN_SECRET, null);
        Long userId = prefs.getLong(AB.Preference.PREF_KEY_SESSION_TWITTER_USER_ID, 0);
        this.screenName = prefs.getString(AB.Preference.PREF_KEY_SESSION_TWITTER_SCREEN_NAME, null); //XXX: AccessTokenのコンストラクタに渡せないのでインスタンス変数を介してセットしている
        AccessToken accessToken = null;
        if (token != null && tokenSecret != null && userId > 0 && screenName != null) {
            accessToken = new AccessToken(token, tokenSecret, userId);
        }
        return accessToken;
    }

    /* (Non-Public)
     * SharedPreferences に保存されている AccessToken をクリア(削除)する。
     * @param context コンテキスト
     */
    protected void clearAccessToken(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AB.Preference.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(AB.Preference.PREF_KEY_SESSION_TWITTER_TOKEN);
        editor.remove(AB.Preference.PREF_KEY_SESSION_TWITTER_TOKEN_SECRET);
        editor.remove(AB.Preference.PREF_KEY_SESSION_TWITTER_USER_ID);
        editor.remove(AB.Preference.PREF_KEY_SESSION_TWITTER_SCREEN_NAME);
        editor.apply();
    }

    /// Accessors ///

    /**
     * コンシューマ・キーを取得します。
     * @return コンシューマ・キー
     */
    public String getConsumerKey() {
        return this.consumerKey;
    }

    /**
     * コンシューマ・キーをセットします。
     * @param consumerKey コンシューマ・キー
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * コンシューマ・シークレットを取得します。
     * @return コンシューマ・シークレット
     */
    public String getConsumerSecret() {
        return this.consumerSecret;
    }

    /**
     * コンシューマ・シークレットをセットします。
     * @param consumerSecret コンシューマ・シークレット
     */
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    /**
     * アクセストークンを取得します。
     * @return アクセストークン
     */
    public String getAuthToken() {
        return this.authToken;
    }

    /**
     * アクセストークンをセットします。
     * @param authToken アクセストークン
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    /**
     * トークンシークレットを取得します。
     * @return トークンシークレット
     */
    public String getAuthTokenSecret() {
        return this.authTokenSecret;
    }

    /**
     * トークンシークレットをセットします。
     * @param authTokenSecret トークンシークレット
     */
    public void setAuthTokenSecret(String authTokenSecret) {
        this.authTokenSecret = authTokenSecret;
    }

    /**
     * ユーザID(Twitter ID)を取得します。
     * @return ユーザID
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * ユーザID(Twitter ID)をセットします。
     * @param userId ユーザID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * スクリーンネームを取得します。
     * @return スクリーンネーム
     */
    public String getScreenName() {
        return this.screenName;
    }

    /**
     * スクリーンネームのセットします。
     * @param screenName スクリーンネーム
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

}
