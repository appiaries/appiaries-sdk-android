//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
/*
　 奇跡のカーニバル

　　開　　幕　　だ
　ｎ　　＿＿＿　　ｎ
　||　／＿＿＿＼　||
　|| ｜(ﾟ)　(ﾟ)｜ ||
「｢｢| ＼￣￣￣／ ｢｢｢|
「￣|　 ￣冂￣　 ｢￣|
`ヽ |／￣|￣|￣＼| ノ

クラスの利便性を優先させ static クラスを多用した結果、巨大なクラスとなりメンテナビリティが低下してしまっているので、分割する方法を検討してみてください。
 */

/**
 * アピアリーズ BaaS。
 * <p>アピアリーズ BaaS の各種サービスにアクセスするための窓口となるクラスです。</p>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class AB {
    private static String TAG = AB.class.getSimpleName();

    static Context sApplicationContext;
    static boolean sActivated; // 初期化済みかどうか (true:初期化済み)

    private AB() {}

    /**
     * Appiaries SDK を初期化します。
     * @param applicationContext アプリケーション・コンテキスト
     * @return true: 初期化成功
     */
    public static boolean activate(Context applicationContext) {
        sApplicationContext = applicationContext;

        if (sApplicationContext == null)
            throw new RuntimeException("Insufficient arguments (applicationContext is null)");
        if (Config.sApplicationID == null || Config.sApplicationID.length() == 0)
            throw new RuntimeException("AB.Config.applicationID not set yet.");
        if (Config.sApplicationToken == null || Config.sApplicationToken.length() == 0)
            throw new RuntimeException("AB.Config.applicationToken not set yet.");
        if (Config.sDatastoreID == null || Config.sDatastoreID.length() == 0)
            throw new RuntimeException("AB.Config.datastoreID not set yet.");
        if (Config.getUserClass() == null) {
            Config.setUserClass(ABUser.class);
        }

        // Init ClassRepository
        AB.ClassRepository.registerClass(ABDevice.class);
        AB.ClassRepository.registerClass(ABUser.class);
        AB.ClassRepository.registerClass(ABDBObject.class);
        AB.ClassRepository.registerClass(ABFile.class);
        AB.ClassRepository.registerClass(ABSequence.class);
        AB.ClassRepository.registerClass(ABPushMessage.class);
        AB.ClassRepository.registerClass(ABModel.class);

        //SharedPreferences に保存されているデバイスをセッションにロードする
        //>> NOTE: AB.DeviceService.register(...)によりデバイスをBaaSへ登録済みの場合のみ
        try {
            String jsonString = AB.Preference.load(Preference.PREF_KEY_SESSION_DEVICE);
            if (jsonString != null && jsonString.length() > 0) {
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(jsonString, Map.class);
                if (jsonMap != null && jsonMap.size() > 0) {
                    ABDevice registeredDevice = new ABDevice(jsonMap);
                    registeredDevice.apply();
                    AB.Session.setDevice(registeredDevice);
                }
            }
        } catch (Exception e) {
            ABLog.e(TAG, e.getMessage());
        }

        //SharedPreferences に保存されているストアトークンをセッションにロードする
        //>> NOTE: 前回ログイン(orサインアップ)字に自動ログイン・オプションを指定していた場合のみ
        String token = AB.Preference.load(Preference.PREF_KEY_SESSION_TOKEN);
        AB.Session.setToken(token);

        //SharedPreferences に保存されているログインユーザ情報をセッションにロードする
        //>> NOTE: 前回ログイン(orサインアップ)字に自動ログイン・オプションを指定していた場合のみ
        try {
            String jsonString = AB.Preference.load(Preference.PREF_KEY_SESSION_USER);
            if (jsonString != null && jsonString.length() > 0) {
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(jsonString, Map.class);
                if (jsonMap != null && jsonMap.size() > 0) {
                    ABUser loggedInUser = new ABUser(jsonMap);
                    loggedInUser.apply();
                    AB.Session.setUser(loggedInUser);
                }
            }
        } catch (Exception e) {
            ABLog.e(TAG, e.getMessage());
        }

        //SharedPreferences に保存されている匿名ユーザ用UUIDをダンプする (DEBUG用)
        String anonymousUUID = AB.Preference.load(Preference.PREF_KEY_ANONYMOUS_UUID);
        ABLog.d(TAG, Preference.PREF_KEY_ANONYMOUS_UUID + ": " + anonymousUUID);

        if (Config.Twitter.isEnabled()) {
            sActivated = TwitterService.activate();
            //TODO: check return value
        }
        //TODO: SharedPreferences に保存されているTwitter認証情報をセッションにロードする

        if (Config.Facebook.isEnabled()) {
            sActivated = FacebookService.activate();
            //TODO: check return value
        }

        sActivated = migrateIfNeeded();

        return sActivated;
    }

    /**
     * Appiaries SDK が初期化済みかどうかを取得します。
     * @return true: 初期化済み
     */
    public static boolean isActivated() {
        return sActivated;
    }

    /**
     * 組み込みクラスを拡張して作成したサブクラスをリポジトリに登録します。
     * @param clazz 登録するサブクラス
     * @param <T> {@link ABModel} クラスの派生クラス
     */
    public static <T extends ABModel> void registerClass(Class<T> clazz) {
        AB.ClassRepository.registerClass(clazz);
    }

    /**
     * 組み込みクラスを拡張して作成した複数のサブクラスをリポジトリに登録します。
     * @param classes 登録する {@link ABModel} クラスの派生クラスのリスト
     */
    public static void registerClasses(List<Class<? extends ABModel>> classes) {
        AB.ClassRepository.registerClasses(classes);
    }

    /**
     * リポジトリをリセットします。
     * <p>本メソッドを実行すると、リポジトリは初期状態（サブクラス未登録状態）となります。</p>
     */
    public static void resetClassRepository() {
        AB.ClassRepository.reset();
    }

    static int getAppVersion() {
        try {
            Context context = AB.sApplicationContext;
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    //ref) https://www2.opro.net/jp/opss/docs/version.html
    private static Map<String, Object> getVersion(String version) {
        if (version == null || version.isEmpty()) return null;

        Map<String, Object> map = new HashMap<>();

        if (version.endsWith("b")) {
            map.put("suffix", "b");
            version = version.substring(0, version.lastIndexOf("b"));
        }

        String[] chunk = version.split(".");
        //メジャーバージョン
        if (chunk.length > 0) {
            try {
                Integer major =Integer.parseInt(chunk[0]);
                map.put("major", major);
            } catch (Exception e) {
                map.put("major", -1);
            }
        }
        //マイナーバージョン
        if (chunk.length > 1) {
            try {
                Integer minor =Integer.parseInt(chunk[1]);
                map.put("minor", minor);
            } catch (Exception e) {
                map.put("minor", -1);
            }
        }
        //ビルド番号
        if (chunk.length > 2) {
            try {
                Integer build =Integer.parseInt(chunk[2]);
                map.put("build", build);
            } catch (Exception e) {
                map.put("build", -1);
            }
        }
        return map;
    }

    private static boolean migrateIfNeeded() {
        Map<String, Object> currentVersion = getVersion(BuildConfig.VERSION_NAME);
        Map<String, Object> storedVersion  = getVersion((String)AB.Preference.load(Preference.PREF_KEY_SDK_VERSION));
        int ret = compareVersions(currentVersion, storedVersion);
        boolean needsUpgrade   = (ret < 0);
        boolean needsDowngrade = (ret > 0);

        boolean success = false;
        if (needsDowngrade) {
            success = up_1_4_0_to_2_0_0();
            //XXX: 2015.5時点でマイグレーション対象のバージョンは 1.4.0 => 2.0.0 のみなので分岐していない。
            //     マイグレーションが必要なバージョンが追加されたタイミングで分岐コードを追記していく想定。
        }

        if (success) {
            //移行したことをマークするためSDKバージョンをPreferencesに保存
            Preference.save(Preference.PREF_KEY_SDK_VERSION, BuildConfig.VERSION_CODE);
        }

        return true;
    }

    private static int compareVersions(Map<String, Object> currentVersion, Map<String, Object> storedVersion) {
        if (storedVersion == null) return -9; //-9: not yet stored

        if (storedVersion.containsKey("major")) {
            int storedMajor  = (int)storedVersion.get("major");
            int currentMajor = (int)currentVersion.get("major");
            if (storedMajor < currentMajor) return -1;
            if (storedMajor > currentMajor) return 1;
        }
        if (storedVersion.containsKey("minor")) {
            int storedMinor = (int)storedVersion.get("minor");
            int currentMinor = (int)currentVersion.get("minor");
            if (storedMinor < currentMinor) return -1;
            if (storedMinor > currentMinor) return 1;
        }
        if (storedVersion.containsKey("build")) {
            int storedBuild = (int)storedVersion.get("build");
            int currentBuild = (int)currentVersion.get("build");
            if (storedBuild < currentBuild) return -1;
            if (storedBuild > currentBuild) return 1;
        }
        if (storedVersion.containsKey("suffix")) {
            String storedSuffix  = (String)storedVersion.get("suffix");
            String currentSuffix = (String)currentVersion.get("suffix");
            //XXX : 2015.5時点では　"b"(beta) の有無のみサポート
            if ("b".equals(storedSuffix)) {
                if (currentSuffix == null || currentSuffix.isEmpty()) {
                    //NOTE: "b"なしなのでリリース版
                    return -1;
                }/* else { } // "b".equals(currentSuffix) */
            } else { // storedSuffix is empty
                if ("b".equals(currentSuffix)) {
                    //NOTE: "b"付きなのでベータ版
                    return 1;
                }/* else { } // currentSuffix.isEmpty() == true */
            }
        }
        return 0; // storedVersion == currentVersion
    }

    // v1.4.0 から 2.0.0 へのマイグレーション実行
    private static boolean up_1_4_0_to_2_0_0() {
        boolean ret = false;

        //SharedPreferencesの移行
        SharedPreferences oldPrefs = AB.sApplicationContext.getSharedPreferences(Preference.V1_4_PREFERENCE_NAME, Context.MODE_PRIVATE);
        //>> StoreToken
        String token = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_TOKEN, null);
        if (token != null) {
            Preference.save(Preference.PREF_KEY_SESSION_TOKEN, token);
        }
        //>> Auto Login
        boolean autoLogInFlag = oldPrefs.getBoolean(Preference.V1_4_PREF_KEY_LOGIN_AUTO, false);
        if (autoLogInFlag) {
            Preference.save(Preference.PREF_KEY_LOGIN_AUTO, true);
        }
        //>> Log-In UserID
        String userId = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_USER_ID, null);
        if (userId != null) {
            Preference.save(Preference.PREF_KEY_SESSION_USER_ID, userId);
        }
        //>> AuthData
        String authDataString = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_AUTH_DATA, null);
        if (authDataString != null) {
            Preference.save(Preference.PREF_KEY_SESSION_AUTH_DATA, authDataString); //TODO: 格納値は JSONObject.toString() したデータ (復元時は Map に戻す?)
        }
        //>> Anonymous UUID
        String uuid = oldPrefs.getString(Preference.V1_4_PREF_KEY_ANONYMOUS_UUID, null);
        if (uuid != null) Preference.save(Preference.PREF_KEY_ANONYMOUS_UUID, uuid);
        //>> RegistrationID
        String regId = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_REG_ID, null);
        if (regId != null) Preference.save(Preference.PREF_KEY_SESSION_REG_ID, regId);
        //>> Device Attributes
        String attributesString = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_DEVICE_ATTRIBUTES, null);
        if (attributesString != null) {
            Preference.save(Preference.PREF_KEY_SESSION_DEVICE_ATTRIBUTES, attributesString); //TODO: 格納値は JSONObject.toString() したデータ (復元時は Map に戻す?)
        }
        //>> Twitter
        String twitterToken = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_TWITTER_TOKEN, null);
        if (twitterToken != null) Preference.save(Preference.PREF_KEY_SESSION_TWITTER_TOKEN, twitterToken);
        String twitterTokenSecret = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_TWITTER_TOKEN_SECRET, null);
        if (twitterTokenSecret != null) Preference.save(Preference.PREF_KEY_SESSION_TWITTER_TOKEN_SECRET, twitterTokenSecret);
        String twitterUserId = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_TWITTER_USER_ID, null);
        if (twitterUserId != null) Preference.save(Preference.PREF_KEY_SESSION_TWITTER_USER_ID, twitterUserId);
        String twitterScreenName = oldPrefs.getString(Preference.V1_4_PREF_KEY_SESSION_TWITTER_SCREEN_NAME, null);
        if (twitterScreenName != null) Preference.save(Preference.PREF_KEY_SESSION_TWITTER_SCREEN_NAME, twitterScreenName);
        //>> 移行済み旧Preferencesの格納値を破棄
        SharedPreferences.Editor oldPrefsEditor = oldPrefs.edit();
        oldPrefsEditor.clear();
        oldPrefsEditor.apply();

        return ret;
    }

    static String serverURL() {
        return BuildConfig.AB_SERVER_URL;
    }


    static String baasAPIBaseURL(String baseUrl, String version) {
        return String.format("%s/%s/", baseUrl, version);
    }

    static String baasTokenAPIURL(String path) {
        StringBuilder url = new StringBuilder();
        url.append(String.format("%stkn/%s/%s",
                AB.baasAPIBaseURL(AB.serverURL(), AB.Config.getApiVersion()),
                AB.Config.getDatastoreID(), AB.Config.getApplicationID()));
        if (path != null) url.append(path);
        return url.toString();
    }

    static String baasTokenAPIURLWithFormat(String format, Object ... args) {
        String path = String.format(format, args);
        return AB.baasTokenAPIURL(path);
    }

    static String baasUserAPIURL(String path) {
        StringBuilder url = new StringBuilder();
        url.append(String.format("%susr/%s/%s",
                AB.baasAPIBaseURL(AB.serverURL(), AB.Config.getApiVersion()),
                AB.Config.getDatastoreID(), AB.Config.getApplicationID()));
        if (path != null) url.append(path);
        return url.toString();
    }

    static String baasUserAPIURLWithFormat(String format, Object ... args) {
        String path = String.format(format, args);
        return AB.baasUserAPIURL(path);
    }

    static String baasDatastoreAPIURL(String path) {
        StringBuilder url = new StringBuilder();
        url.append(String.format("%sdat/%s/%s",
                AB.baasAPIBaseURL(AB.serverURL(), AB.Config.getApiVersion()),
                AB.Config.getDatastoreID(), AB.Config.getApplicationID()));
        if (path != null) url.append(path);
        return url.toString();
    }

    static String baasDatastoreAPIURLWithFormat(String format, Object ... args) {
        String path = String.format(format, args);
        return AB.baasDatastoreAPIURL(path);
    }

    static String baasFileAPIURL(String path) {
        StringBuilder url = new StringBuilder();
        url.append(String.format("%sbin/%s/%s",
                AB.baasAPIBaseURL(AB.serverURL(), AB.Config.getApiVersion()),
                AB.Config.getDatastoreID(), AB.Config.getApplicationID()));
        if (path != null) url.append(path);
        return url.toString();
    }

    static String baasFileAPIURLWithFormat(String format, Object ... args) {
        String path = String.format(format, args);
        return AB.baasFileAPIURL(path);
    }

    static String baasPushAPIURL(AB.Platform platform, String path) {
        StringBuilder url = new StringBuilder();
        if (AB.Platform.ANDROID == platform) {
            url.append(String.format("%spush/gcm/%s/%s",
                    AB.baasAPIBaseURL(AB.serverURL(), AB.Config.getApiVersion()),
                    AB.Config.getDatastoreID(), AB.Config.getApplicationID()));
        } else { //AB.Platform.IOS == platform
            url.append(String.format("%spush/apns/%s/%s",
                    AB.baasAPIBaseURL(AB.serverURL(), AB.Config.getApiVersion()),
                    AB.Config.getDatastoreID(), AB.Config.getApplicationID()));
        }
        if (path != null) url.append(path);
        return url.toString();
    }

    static String baasPushAPIURLWithFormat(AB.Platform platform, String format, Object ... args) {
        String path = String.format(format, args);
        return AB.baasPushAPIURL(platform, path);
    }

    static String baasPushAnalyticsAPIURL(String path) {
        StringBuilder url = new StringBuilder();
        url.append(String.format("%spush/analytics/%s/%s",
                AB.baasAPIBaseURL(AB.serverURL(), AB.Config.getApiVersion()),
                AB.Config.getDatastoreID(), AB.Config.getApplicationID()));
        if (path != null) url.append(path);
        return url.toString();
    }

    static String baasPushAnalyticsAPIURLWithFormat(String format, Object ... args) {
        String path = String.format(format, args);
        return AB.baasPushAnalyticsAPIURL(path);
    }

    static String baasSequenceAPIURL(String path) {
        StringBuilder url = new StringBuilder();
        url.append(String.format("%sseq/%s/%s",
                AB.baasAPIBaseURL(AB.serverURL(), AB.Config.getApiVersion()),
                AB.Config.getDatastoreID(), AB.Config.getApplicationID()));
        if (path != null) url.append(path);
        return url.toString();
    }

    static String baasSequenceAPIURLWithFormat(String format, Object ... args) {
        String path = String.format(format, args);
        return AB.baasSequenceAPIURL(path);
    }

    /// 定数 ///

    public static final String AUTHENTICATION_PROVIDER_UNKNOWN_KEY   = "unknown";
    public static final String AUTHENTICATION_PROVIDER_ANONYMOUS_KEY = "anonymous";
    public static final String AUTHENTICATION_PROVIDER_TWITTER_KEY   = "twitter";
    public static final String AUTHENTICATION_PROVIDER_FACEBOOK_KEY  = "facebook";

    public static final String JSON_AUTH_DATA_KEY = "authData";
    public static final String JSON_AUTH_DATA_ANONYMOUS_ID_KEY = "id";
    public static final String JSON_AUTH_DATA_TWITTER_ID_KEY = "id";
    public static final String JSON_AUTH_DATA_TWITTER_SCREEN_NAME_KEY = "screen_name";
    public static final String JSON_AUTH_DATA_TWITTER_OAUTH_TOKEN_KEY = "oauth_token";
    public static final String JSON_AUTH_DATA_TWITTER_OAUTH_TOKEN_SECRET_KEY = "oauth_token_secret";
    public static final String JSON_AUTH_DATA_TWITTER_CONSUMER_KEY_KEY = "consumer_key";
    public static final String JSON_AUTH_DATA_TWITTER_CONSUMER_SECRET_KEY = "consumer_secret";

    //static final String INTENT_EXTRA_KEY_LAUNCH_ACTIVITY = "STARTACTIVITY";
    public static final String EXTRA_KEY_CONFIG = "CONFIG";
    public static final String EXTRA_KEY_PUSH_MESSAGE = "PUSH_MESSAGE";
    static final String EXTRA_KEY_TITLE = "title";
    static final String EXTRA_KEY_MESSAGE = "message";
    public static final String EXTRA_KEY_URL = "_openUrl";
    static final String EXTRA_KEY_PUSH_ID = "pushId";
    static final String EXTRA_KEY_COLLAPSE_KEY = "collapse_key";
    static final String EXTRA_KEY_TIME_TO_LIVE = "time_to_live";
    static final String EXTRA_KEY_DELAY_WHILE_IDLE = "delay_while_idle";
    static final String EXTRA_KEY_RESTRICTED_PACKAGE_NAME = "restricted_package_name";
    static final String EXTRA_KEY_DRY_RUN = "dry_run";
    static final String EXTRA_KEY_FROM = "from";
    static final String EXTRA_KEY_REGISTRATION_IDS = "registration_ids";

    /**
     * デバイス・プラットフォーム。
     */
    public enum Platform {
        /** Android プラットフォーム・デバイスであることを表します。 */
        ANDROID,
        /** iOS プラットフォーム・デバイスであることを表します。 */
        IOS,
        /** プラットフォームが不明なデバイスであることを表します。 */
        UNKNOWN,
    }

    /**
     * プッシュ通知配信環境。
     */
    public enum PushEnvironment {
        /** APNs の Sandbox 環境下でプッシュ通知を配信することを表します。 */
        SANDBOX,
        /** APNs の Production 環境下でプッシュ通知を配信することを表します。 */
        PRODUCTION,
    }

    /**
     * プッシュ通知モード。
     * <p>プッシュ通知を受信した際の、メッセージの表示方法を指定するオプションです。</p>
     */
    public enum PushMode {
        /** プッシュ通知ダイアログを表示します。 */
        DIALOG,
        /** ステータスバーに通知を表示します。 */
        NOTIFICATION,
    }

    /**
     * プッシュ通知ダイアログ・スタイル。
     */
    public enum PushDialogStyle {
        /** シンプルなデザインのダイアログです。 */
        SIMPLE,
        /** ポップなデザインのダイアログです。 */
        POP,
        /** フラットなデザインのダイアログです。 */
        FLAT,
        /** クールなデザインのダイアログです。 */
        COOL,
    }

    /**
     * DBオブジェクト保存用オプション。
     */
    public enum DBObjectSaveOption {
        /** 未指定。 */
        NONE
    }
    /**
     * DBオブジェクト削除用オプション。
     */
    public enum DBObjectDeleteOption {
        /** 未指定。 */
        NONE
    }
    /**
     * DBオブジェクト・リフレッシュ用オプション。
     */
    public enum DBObjectRefreshOption {
        /** 未指定。 */
        NONE
    }
    /**
     * DBオブジェクト取得用オプション。
     */
    public enum DBObjectFetchOption {
        /** 未指定。 */
        NONE
    }
    /**
     * DBオブジェクト・キャンセルオプション。
     */
    public enum DBObjectCancelOption {
        /** 未指定。 */
        NONE
    }
    /**
     * DBオブジェクト検索用オプション。
     */
    public enum DBObjectFindOption {
        /** 未指定。 */
        NONE
    }


    /**
     * ファイル保存用オプション。
     */
    public enum FileSaveOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ファイル削除用オプション。
     */
    public enum FileDeleteOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ファイル・リフレッシュ用オプション。
     */
    public enum FileRefreshOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ファイル取得用オプション。
     */
    public enum FileFetchOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ファイル検索用オプション。
     */
    public enum FileFindOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ファイル・ダウンロード用オプション。
     */
    public enum FileDownloadOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ファイル・キャンセル用オプション。
     */
    public enum FileCancelOption {
        /** 未指定。 */
        NONE
    }


    /**
     * シーケンス保存用オプション。
     */
    enum SequenceSaveOption { //** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * シーケンス削除用オプション。
     */
    enum SequenceDeleteOption { //** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * シーケンス・リフレッシュ用オプション。
     */
    enum SequenceRefreshOption { //** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * シーケンス取得用オプション。
     */
    public enum SequenceFetchOption {
        /** 未指定。 */
        NONE
    }
    /**
     * シーケンス・キャンセルオプション。
     */
    public enum SequenceCancelOption {
        /** 未指定。 */
        NONE
    }
    /**
     * シーケンス検索用オプション。
     */
    enum SequenceFindOption { //** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * シーケンス加減算用オプション。
     */
    public enum SequenceAddOption {
        /** 未指定。 */
        NONE
    }
    /**
     * シーケンス・リセット用オプション。
     */
    public enum SequenceResetOption {
        /** 未指定。 */
        NONE
    }


    /**
     * ユーザ・サインアップ用オプション。
     */
    public enum UserSignUpOption {
        /** 未指定。 */
        NONE,
        /** ユーザ登録のみを行いログインしません。 */
        WITHOUT_LOGIN,
        /** ログイン成功時の認証情報を永続化し、次回以降のアプリ起動時に自動的にログイン状態に遷移します。 */
        LOGIN_AUTOMATICALLY,
        /**
         * ログイン成功時のレスポンスにIDのみを返却します。
         * <div class="important">ログインIDやメールアドレス、その他属性情報などをセッションに保持しておきたい場合は、このオプションは指定しないでください。</div>
         */
        USE_INCOMPLETE_DATA,
    }
    /**
     * ユーザ・ログイン用オプション。
     */
    public enum UserLogInOption {
        /** 未指定。 */
        NONE,
        /** ログイン成功時の認証情報を永続化し、次回以降のアプリ起動時に自動的にログイン状態に遷移します。 */
        LOGIN_AUTOMATICALLY,
        /**
         * ログイン成功時のレスポンスにIDのみを返却します。
         * <div class="important">ログインIDやメールアドレス、その他属性情報などをセッションに保持しておきたい場合は、このオプションは指定しないでください。</div>
         */
        USE_INCOMPLETE_DATA,
    }
    /**
     * ユーザ・ログアウト用オプション。
     */
    public enum UserLogOutOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ユーザ本人確認用メール送信要求用オプション。
     */
    public enum UserVerifyEmailOption {
        /** 未指定。 */
        NONE
    }
    /**
     * パスワード再設定メール送信要求用オプション。
     */
    public enum UserResetPasswordOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ユーザ保存用オプション。
     */
    public enum UserSaveOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ユーザ削除用オプション。
     */
    public enum UserDeleteOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ユーザ・リフレッシュ用オプション。
     */
    public enum UserRefreshOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ユーザ取得用オプション。
     */
    public enum UserFetchOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ユーザ・キャンセル用オプション。
     */
    public enum UserCancelOption {
        /** 未指定。 */
        NONE
    }
    /**
     * ユーザ検索用オプション。
     */
    public enum UserFindOption {
        /** 未指定。 */
        NONE
    }


    /**
     * デバイス登録用オプション。
     */
    public enum DeviceRegistrationOption {
        /** 未指定。 */
        NONE,
        /** 登録したデバイス情報の永続化を行いません。 */
        WITHOUT_SAVING,
//        /**
//         * 登録成功時のレスポンスにIDのみを返却します。<br>
//         * その他属性情報などをセッションに保持しておきたい場合は、このオプションは指定しないでください。
//         */
//        USE_INCOMPLETE_DATA
    }
    /**
     * デバイス登録解除用オプション。
     */
    public enum DeviceUnregistrationOption {
        /** 未指定。 */
        NONE
    }
    /**
     * デバイス保存用オプション。
     */
    enum DeviceSaveOption { // ** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * デバイス削除用オプション。
     */
    enum DeviceDeleteOption { // ** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * デバイス・リフレッシュ用オプション。
     */
    enum DeviceRefreshOption { // ** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * デバイス取得用オプション。
     */
    enum DeviceFetchOption { // ** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * デバイス・キャンセル用オプション。
     */
    enum DeviceCancelOption { // ** 非公開 **
        /** 未指定。 */
        NONE
    }
    /**
     * デバイス検索用オプション。
     */
    enum DeviceFindOption { // ** 非公開 **
        /** 未指定。 */
        NONE
    }


    /**
     * プッシュ通知メッセージ開封通知用オプション。
     */
    public enum PushMessageOpenOption {
        /** 未指定。 */
        NONE
    }
    /**
     * プッシュ通知メッセージ保存用オプション。
     */
    public enum PushMessageSaveOption {
        /** 未指定。 */
        NONE
    }
    /**
     * プッシュ通知メッセージ削除用オプション。
     */
    public enum PushMessageDeleteOption {
        /** 未指定。 */
        NONE
    }
    /**
     * プッシュ通知メッセージ・リフレッシュ用オプション。
     */
    public enum PushMessageRefreshOption {
        /** 未指定。 */
        NONE
    }
    /**
     * プッシュ通知メッセージ取得用オプション。
     */
    public enum PushMessageFetchOption {
        /** 未指定。 */
        NONE
    }
    /**
     * プッシュ通知メッセージ・キャンセル用オプション。
     */
    public enum PushMessageCancelOption {
        /** 未指定。 */
        NONE
    }
    /**
     * プッシュ通知メッセージ検索用オプション。
     */
    public enum PushMessageFindOption {
        /** 未指定。 */
        NONE
    }

    /**
     * クラス・リポジトリ。
     * <p>組み込みクラスを拡張して作成したユーザ定義モデルクラスを管理します。</p>
     * @version 2.0.0
     * @since 2.0.0
     */
    public static class ClassRepository {

        private static final String TAG = ClassRepository.class.getSimpleName();

        private static Map<Class<? extends ABModel>, String> sBaaSClassRepository = new ConcurrentHashMap<>();
        private static Map<String, Class<? extends ABModel>> sBaaSCollectionRepository = new ConcurrentHashMap<>();

        private static int sNumberOfEmbeddedClasses = 7; //XXX: ABDevice,ABUser,ABDBObject,ABFile,ABSequence,ABPushMessage,ABModel

        /**
         * 登録済みのユーザ定義モデルクラス数を返します。
         * @return 登録済みのユーザ定義モデルクラス数
         */
        public static int size() {
            return sBaaSClassRepository.size() - sNumberOfEmbeddedClasses;
        }

        /**
         * 引数モデルクラスに対応するコレクションIDを取得します。
         * @param clazz コレクションID を取得する {@link ABModel} クラスの派生クラス
         * @return コレクションID
         */
        public static String getCollectionID(Class<? extends ABModel> clazz) {
            String collectionID = sBaaSClassRepository.get(clazz);
            if (collectionID == null) {
                ABCollection annotation = clazz.getAnnotation(ABCollection.class);
                if (annotation != null && annotation.value().length() > 0) {
                    collectionID = annotation.value();
                    sBaaSClassRepository.put(clazz, collectionID);
                } else {
                    return null;
                }
            }
            return collectionID;
        }

        /**
         * 組み込みクラスを拡張して作成したユーザ定義モデルクラスをリポジトリに登録します。
         * @param clazz 登録するモデルクラス
         * @param <T> {@link ABModel} クラスの派生クラス
         */
        public static <T extends ABModel> void registerClass(Class<T> clazz) {
            /*
            ABCollection collection = clazz.getAnnotation(ABCollection.class);
            String baasClassName = collection.value();
            if (baasClassName == null) {
                baasClassName = clazz.getName();
            }
            sBaaSClassRepository.put(baasClassName, clazz);
            */

            String collectionID = getCollectionID(clazz);

            //SDK組み込みクラスは特殊な扱いとなる (key=className, value=Class)
            if (clazz == ABModel.class || clazz == ABDBObject.class || clazz == ABFile.class ||
                clazz == ABUser.class  || clazz == ABDevice.class || clazz == ABSequence.class ||
                clazz == ABPushMessage.class) {
                sBaaSClassRepository.put(clazz, collectionID);
                sBaaSCollectionRepository.put(collectionID, clazz);
                return;
            }

            //コレクションを持たない組み込みクラスの派生クラスの場合はクラス名で登録する
            if (clazz.getSuperclass() == ABUser.class ||
                clazz.getSuperclass() == ABDevice.class ||
                clazz.getSuperclass() == ABPushMessage.class) {
                collectionID = clazz.getName();
                sBaaSClassRepository.put(clazz, collectionID);
                sBaaSCollectionRepository.put(collectionID, clazz);
                return;
            }

            if (collectionID == null) {
                throw new IllegalArgumentException("No ABCollection annotation provided on " + clazz);
            }
            if (clazz.getDeclaredConstructors().length > 0) {
                try {
                    Member m = clazz.getDeclaredConstructor(new Class[0]);
                    boolean isAccessible =
                            (Modifier.isPublic(m.getModifiers())) ||
                            ((m.getDeclaringClass().getPackage().getName().equals("com.appiaries.baas.sdk")) &&
                            (!Modifier.isPrivate(m.getModifiers())) &&
                            (!Modifier.isProtected(m.getModifiers())));
                    if (!isAccessible)
                        throw new IllegalArgumentException("Default constructor for " + clazz + " is not accessible.");
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("No default constructor provided for " + clazz);
                }
            }
            Class oldValue = (Class)sBaaSCollectionRepository.get(collectionID);
            if ((oldValue != null) && (clazz.isAssignableFrom(oldValue))) {
                return;
            }
            sBaaSCollectionRepository.put(collectionID, clazz);
        }

        /**
         * リポジトリに複数のユーザ定義モデルクラスを登録します。
         * @param classes 登録する {@link ABModel} クラスの派生クラスのリスト
         */
        public static void registerClasses(List<Class<? extends ABModel>> classes) {
            for (Class clazz : classes) {
                registerClass(clazz);
            }
        }

        /**
         * リポジトリをリセットします。
         * <p>本メソッドを実行すると、リポジトリは初期状態（ユーザ定義モデルクラス未登録状態）となります。</p>
         */
        public static void reset() {
            sBaaSClassRepository.clear();
        }

        /**
         * 引数クエリ・オブジェクトから対応するモデルクラスを取得します。
         * @param query クエリ・オブジェクト
         * @return {@link ABModel} クラスの派生クラス
         */
        public static Class getBaaSClass(ABQuery query) {
            return getBaaSClass(query, null);
        }

        /**
         * 引数クエリ・オブジェクトから対応するモデルクラスを取得します。
         * @param query クエリ・オブジェクト
         * @param clazz 該当クラス未登録時に返却するデフォルトクラス
         * @return {@link ABModel} クラスの派生クラス
         */
        public static Class getBaaSClass(ABQuery query, Class<? extends ABModel> clazz) {
            String baasClassName = query.getCollectionID();
            if (sBaaSCollectionRepository.containsKey(baasClassName)) {
                return sBaaSCollectionRepository.get(baasClassName);
            } else {
                if (clazz != null) {
                    return clazz;
                } else {
                    return ABModel.class;
                }
            }
        }

        /**
         * 引数オブジェクトのモデルクラスを取得します。
         * @param object {@link ABModel} クラスの派生オブジェクト
         * @param <T> {@link ABModel} クラスの派生クラス
         */
        public static <T extends ABModel> Class getBaaSClass(T object) {
            String baasClassName = object.getCollectionID();
            if (sBaaSCollectionRepository.containsKey(baasClassName)) {
                return sBaaSCollectionRepository.get(baasClassName);
            } else {
                if (object instanceof ABDBObject) {
                    return ABDBObject.class;
                } else if (object instanceof ABFile) {
                    return ABFile.class;
                } else if (object instanceof ABDevice) {
                    return ABDevice.class;
                } else if (object instanceof ABPushMessage) {
                    return ABPushMessage.class;
                } else if (object instanceof ABUser) {
                    return ABUser.class;
                } else if (object instanceof ABSequence) {
                    return ABSequence.class;
                } else {
                    return ABModel.class;
                }
            }
        }

        /**
         * 引数コレクションIDに対応するモデルクラスを取得します。
         * @param collectionID コレクションID
         * @return {@link ABModel} クラスの派生クラス
         */
        public static Class<? extends ABModel> getBaaSClass(String collectionID) {
            if (sBaaSCollectionRepository.containsKey(collectionID)) {
                return sBaaSCollectionRepository.get(collectionID);
            }
            return null;
        }
    }

    /**
     * アピアリーズ BaaS 設定。
     *
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=60">アピアリーズドキュメント &raquo; アプリを設定する</a>
     */
    public static class Config {

        private static String sApplicationID;
        private static String sApplicationToken;
        private static String sDatastoreID;
        private static String sSdkVersion = BuildConfig.VERSION_NAME;
        private static String sApiVersion = BuildConfig.AB_API_VERSION;
        private static Class<? extends ABUser> sUserClass;
        private static int sConnectionTimeoutInterval = 30;  //sec
        private static int sUploadTimeoutInterval = 300; //sec
        private static int sDownloadTimeoutInterval = 300; //sec

        //private Config() {
        //    sSdkVersion = BuildConfig.VERSION_NAME;
        //    sApiVersion = BuildConfig.AB_API_VERSION;
        //}

        /**
         * Appiaries SDK のバージョンを取得します。
         * @return Appiaries SDK のバージョン
         */
        public static String getSdkVersion() {
            return sSdkVersion;
        }
        //static void setSdkVersion(String sdkVersion) {
        //    Config.sSdkVersion = sdkVersion;
        //}

        /**
         * 使用するアピアリーズ BaaS API のバージョンを取得します。
         * @return アピアリーズ BaaS API のバージョン
         */
        public static String getApiVersion() {
            return sApiVersion;
        }
        //static void setApiVersion(String apiVersion) {
        //    Config.sApiVersion = apiVersion;
        //}

        static String getUserAgent() {
            return "ab-android-" + sSdkVersion;
        }

        /**
         * アピアリーズのアプリIDを取得します。
         * @return アピアリーズのアプリID
         */
        public static String getApplicationID() {
            return sApplicationID;
        }

        /**
         * アピアリーズのアプリIDをセットします。
         * <div class="important">[コントロールパネル] &raquo; [ダッシュボード] &raquo; [アプリ情報] 欄に表記されているアプリIDを指定します。</div>
         * @param applicationID アプリID
         */
        public static void setApplicationID(String applicationID) {
            sApplicationID = applicationID;
        }

        /**
         * アピアリーズのアプリトークンを取得します。
         * @return アプリトークン
         */
        public static String getApplicationToken() {
            return sApplicationToken;
        }

        /**
         * アピアリーズのアプリトークンをセットします。
         * <div class="important">[コントロールパネル] &raquo; [ダッシュボード] &raquo; [アプリ情報] 欄に表記されているアプリトークンを指定します。</div>
         * @param applicationToken アプリトークン
         */
        public static void setApplicationToken(String applicationToken) {
            sApplicationToken = applicationToken;
        }

        /**
         * アピアリーズのデータストアIDを取得します。
         * @return データストアID
         */
        public static String getDatastoreID() {
            return sDatastoreID;
        }

        /**
         * アピアリーズのデータストアIDをセットします。
         * <div class="important">[コントロールパネル] &raquo; [ダッシュボード] &raquo; [アプリ情報] 欄に表記されているデータストアIDを指定します。</div>
         * @param datastoreID データストアID
         */
        public static void setDatastoreID(String datastoreID) {
            sDatastoreID = datastoreID;
        }

        /**
         * 認証に使用する {@link ABUser} クラス (またはその派生クラス) を取得します。
         * @return {@link ABUser} クラス (またはその派生クラス)
         */
        public static Class<? extends ABUser> getUserClass() {
            return sUserClass;
        }

        /**
         * 認証に使用する {@link ABUser} クラス (またはその派生クラス) をセットします。
         * @param userClass {@link ABUser} クラス (またはその派生クラス)
         */
        public static void setUserClass(Class<? extends ABUser> userClass) {
            sUserClass = userClass;
        }

        /**
         * 通常通信時（アップロード／ダウンロード以外）の接続タイムアウト秒を取得します。
         * <p>デフォルト値は「30秒」です。</p>
         * @return 接続タイムアウト秒
         */
        public static int getConnectionTimeoutInterval() {
            return sConnectionTimeoutInterval;
        }

        /**
         * 通常通信時（アップロード／ダウンロード以外）の接続タイムアウト秒をセットします。
         * @param connectionTimeoutInterval タイムアウト秒
         */
        public static void setConnectionTimeoutInterval(int connectionTimeoutInterval) {
            sConnectionTimeoutInterval = connectionTimeoutInterval;
        }

        /**
         * アップロード通信時のタイムアウト秒を取得します。
         * <p>デフォルト値は「300秒」です。</p>
         * @return タイムアウト秒
         */
        public static int getUploadTimeoutInterval() {
            return sUploadTimeoutInterval;
        }

        /**
         * アップロード通信時のタイムアウト秒をセットします。
         * @param uploadTimeoutInterval タイムアウト秒
         */
        public static void setUploadTimeoutInterval(int uploadTimeoutInterval) {
            sUploadTimeoutInterval = uploadTimeoutInterval;
        }

        /**
         * ダウンロード通信時のタイムアウト秒を取得します。
         * <p>デフォルト値は「300秒」です。</p>
         * @return タイムアウト秒
         */
        public static int getDownloadTimeoutInterval() {
            return sDownloadTimeoutInterval;
        }

        /**
         * ダウンロード通信時のタイムアウト秒をセットします。
         * @param downloadTimeoutInterval タイムアウト秒
         */
        public static void setDownloadTimeoutInterval(int downloadTimeoutInterval) {
            sDownloadTimeoutInterval = downloadTimeoutInterval;
        }

        /**
         * プリファレンスから指定キーを削除します。
         * @param key キー
         */
        public static void discard(String key) {
            // FIXME: AB.Preference へメソッドを移動
            if (key == null) return;
            AB.Preference.remove(key);
        }

        /**
         * プリファレンスからすべての設定値を削除します。
         */
        public static void discardAll() {
            // FIXME: AB.Preference へメソッドを移動
            AB.Preference.removeAll();
        }

        /**
         * プッシュ通知設定。
         */
        public static class Push {

            private static ABPushConfiguration sConfig = new ABPushConfiguration();

            /**
             * AB.Push にその時点で設定されている値を用いて複製した ABPushConfiguration オブジェクトを取得します。
             * <p>複数のプッシュ通知設定を使い分ける必要がある場合などに利用します。</p>
             * @return {@link ABPushConfiguration} オブジェクト
             */
            public static ABPushConfiguration getDefaultConfiguration() {
                ABPushConfiguration config  = null;
                try {
                    config = sConfig.clone();
                } catch (Exception ignored) {}
                return config;
            }

            /**
             * Sender ID を取得します。
             * <p>Google Developers で作成した当該アプリ用プロジェクトの「Project Number」が Sender ID となります。</p>
             * @return Sender ID
             */
            public static String getSenderID() {
                return sConfig.getSenderID();
            }

            /**
             * Sender ID をセットします。
             * <p>Google Developers で作成した当該アプリ用プロジェクトの「Project Number」が Sender ID となります。</p>
             * @param senderID Sender ID
             */
            public static void setSenderID(String senderID) {
                sConfig.setSenderID(senderID);
            }

            /**
             * 通知モードを取得します。
             * @return 通知モード
             */
            public static AB.PushMode getMode() {
                return sConfig.getMode();
            }

            /**
             * 通知モードをセットします。
             * @param mode 通知モード
             */
            public static void setMode(AB.PushMode mode) {
                sConfig.setMode(mode);
            }

            /**
             * 開封通知フラグを取得します。
             * @return true: プッシュ通知メッセージを閲覧した際に開封通知APIをコールする
             */
            public static boolean isOpenMessage() {
                return sConfig.isOpenMessage();
            }

            /**
             * 開封通知フラグをセットします。
             * @param flag true: プッシュ通知メッセージを閲覧した際に開封通知APIをコールする
             */
            public static void setOpenMessage(boolean flag) {
                sConfig.setOpenMessage(flag);
            }

            /**
             * プッシュ通知ダイアログ設定を取得します。
             * @return {@link ABPushDialogConfiguration} オブジェクト
             */
            public static ABPushDialogConfiguration getDialogConfiguration() {
                return sConfig.getDialogConfiguration();
            }

            /**
             * プッシュ通知ダイアログ設定をセットします。
             * @param configuration {@link ABPushDialogConfiguration} オブジェクト
             */
            public static void setDialogConfiguration(ABPushDialogConfiguration configuration) {
                sConfig.setDialogConfiguration(configuration);
            }

            /**
             * プッシュ通知受信時の Intent にセットするアクションを取得します。
             * @return プッシュ通知受信時の Intent にセットするアクション
             */
            public static String getAction() {
                return sConfig.getAction();
            }

            /**
             * プッシュ通知受信時の Intent にセットするアクションをセットします。
             * @param action プッシュ通知受信時の Intent にセットするアクション
             */
            public static void setAction(String action) {
                sConfig.setAction(action);
            }

            /**
             * プッシュ通知ダイアログで OK ボタンをタップした際に表示するアクティビティ・クラスを取得します。
             * @return アクティビティ・クラス
             */
            public static String getLaunchActivityClass() {
                return sConfig.getLaunchActivityClass();
            }

            /**
             * プッシュ通知ダイアログで OK ボタンをタップした際に表示するアクティビティ・クラスをセットします。
             * @param activityClass アクティビティ・クラス
             */
            public static void setLaunchActivityClass(String activityClass) {
                sConfig.setLaunchActivityClass(activityClass);
            }

            /**
             * プッシュ通知ダイアログの左上にアイコンを表示させるかどうかを取得します。
             * @return true: アイコンを表示させる
             * @deprecated
             */
            public static boolean getIconVisibility() {
                //FIXME: ABPushDialogConfiguration に移動する
                return sConfig.getIconVisibility();
            }

            /**
             * プッシュ通知ダイアログの左上にアイコンを表示させるかどうかをセットします。
             * @param visible true: アイコンを表示させる
             * @deprecated
             */
            public static void setIconVisibility(boolean visible) {
                //FIXME: ABPushDialogConfiguration に移動する
                sConfig.setIconVisibility(visible);
            }

            /**
             * プッシュ通知ダイアログの左上に表示させるアイコンファイル名を取得します。
             * <div class="important">アイコンファイルは /res/drawable 配下に配置済みである必要があります。</div>
             * @return アイコンファイル名
             * @deprecated
             */
            public static String getIconName() {
                //FIXME: ABPushDialogConfiguration に移動する
                return sConfig.getIconName();
            }

            /**
             * プッシュ通知ダイアログの左上に表示させるアイコンファイル名をセットします。
             * <div class="important">アイコンファイルは /res/drawable 配下に配置済みである必要があります。</div>
             * @param iconName アイコンファイル名
             * @deprecated
             */
            public static void setIconName(String iconName) {
                //FIXME: ABPushDialogConfiguration に移動する
                sConfig.setIconName(iconName);
            }

            /**
             * プッシュ通知受信時のバイブレーションのパターンを取得します。
             * @return バイブレーション・パターン
             * @deprecated
             */
            public static long[] getVibratePattern() {
                //FIXME: ABPushDialogConfiguration に移動する
                return sConfig.getVibratePattern();
            }

            /**
             * プッシュ通知受信時のバイブレーションのパターンをセットします。
             * @param pattern バイブレーション・パターン
             * @deprecated
             */
            public static void setVibratePattern(long[] pattern) {
                //FIXME: ABPushDialogConfiguration に移動する
                sConfig.setVibratePattern(pattern);
            }

        }

        /**
         * Twitter 設定。
         * <p>Twitter との連携に必要となる設定情報を保持します。</p>
         * <div class="important">設定情報は、{@link AB#activate(android.content.Context)} を実行する前にセットする必要があります。</div>
         * @version 2.0.0
         * @since 2.0.0
         */
        public static class Twitter {

            private static boolean sEnabled;
            private static String sConsumerKey;
            private static String sConsumerSecret;

            /**
             * AB.TwitterService 有効化フラグを取得します。
             * @return true: {@link AB.TwitterService} を有効にする
             */
            public static boolean isEnabled() {
                return sEnabled;
            }

            /**
             * AB.TwitterService 有効化フラグをセットします。
             * @param enabled true: {@link AB.TwitterService} を有効にする
             */
            public static void setEnabled(boolean enabled) {
                sEnabled = enabled;
            }

            /**
             * Twitter コンシューマ・キーを取得します。
             * @return コンシューマ・キー
             */
            public static String getConsumerKey() {
                return sConsumerKey;
            }

            /**
             * Twitter コンシューマ・キーをセットします。
             * @param consumerKey コンシューマ・キー
             */
            public static void setConsumerKey(String consumerKey) {
                sConsumerKey = consumerKey;
            }

            /**
             * Twitter コンシューマ・シークレットを取得します。
             * @return コンシューマ・シークレット
             */
            public static String getConsumerSecret() {
                return sConsumerSecret;
            }

            /**
             * Twitter コンシューマ・シークレットをセットします。
             * @param consumerSecret コンシューマ・シークレット
             */
            public static void setConsumerSecret(String consumerSecret) {
                sConsumerSecret = consumerSecret;
            }

        }

        /**
         * Facebook 設定。
         * <p>Facebook との連携に必要となる設定情報を保持します。</p>
         * <div class="important">設定情報は、{@link AB#activate(android.content.Context)} を実行する前にセットする必要があります。</div>
         * @version 2.0.0
         * @since 2.0.0
         */
        public static class Facebook {
            private static boolean sEnabled;
            private static List<String> sPermissions;
            private static String sUrlSchemeSuffix;

            /**
             * AB.FacebookService 有効化フラグを取得します。
             * @return true: {@link AB.FacebookService} を有効にする
             */
            public static boolean isEnabled() {
                return sEnabled;
            }

            /**
             * AB.FacebookService 有効化フラグをセットします。
             * @param enabled true: {@link AB.FacebookService} を有効にする
             */
            public static void setEnabled(boolean enabled) {
                sEnabled = enabled;
            }

            /**
             * Facebookログイン時にアクセス許可を求めるパーミッション・リストを取得します。
             * @return パーミッション・リスト
             */
            public static List<String> getPermissions() {
                return sPermissions;
            }

            /**
             * Facebookログイン時にアクセス許可を求めるパーミッション・リストをセットします。
             * @param permissions パーミッション・リスト
             */
            public static void setPermissions(List<String> permissions) {
                sPermissions = permissions;
            }

            /**
             * URL Scheme Suffix を取得します。
             * @return URL Scheme Suffix
             */
            public static String getUrlSchemeSuffix() {
                return sUrlSchemeSuffix;
            }

            /**
             * URL Scheme Suffix をセットします。
             * @param urlSchemeSuffix URL Scheme Suffix
             */
            public static void setUrlSchemeSuffix(String urlSchemeSuffix) {
                sUrlSchemeSuffix = urlSchemeSuffix;
            }

        }

    }

    /**
     * セッション。
     * <p></p>
     * @version 2.0.0
     * @since 2.0.0
     */
    public static class Session {

        private static String   mToken;  // ログイン(or サインアップ)成功時にサーバから受け取ったストアトークンが格納される
        private static Object mDevice; // レジストレーションID送信完了時にデバイス情報(自機)が格納される
        private static Object mUser;   // ログイン(or サインアップ)成功時にログインしたユーザの情報が格納される

//region Initialization

        /**
         * Twitter セッション。
         *
         * @version 2.0.0
         * @since 2.0.0
         */
        public static class Twitter {
            //TODO: not yet implemented
        }

//endregion

//region Invalidate

        /**
         * セッションに保持しているデバイス情報を破棄します。
         */
        public static void invalidateDevice() {
            mDevice = null;
            AB.Config.discard(Preference.PREF_KEY_SESSION_DEVICE);
        }

        /**
         * セッッションに保持しているストアトークンを破棄します。
         */
        public static void invalidateToken() {
            mToken = null;
            AB.Config.discard(Preference.PREF_KEY_SESSION_TOKEN);
        }

        /**
         * セッションに保持しているユーザ情報を破棄します。
         */
        public static void invalidateUser() {
            mUser = null;
            AB.Config.discard(Preference.PREF_KEY_SESSION_USER);
        }

        /**
         * セッションに保持している Twitter セッション情報を破棄します。
         */
        public static void invalidateTwitter() {
            //TODO: not yet implemented
            AB.Config.discard(Preference.PREF_KEY_SESSION_TWITTER);
        }

        /**
         * セッションに保持している Facebook セッション情報を破棄します。
         */
        public static void invalidateFacebook() {
            //TODO: not yet implemented
        }

        /**
         * セッションに保持している全ての情報を破棄します。
         */
        public static void invalidateAll() {
            invalidateDevice();
            invalidateToken();
            invalidateUser();
            invalidateToken();
            invalidateFacebook();
        }

//endregion

//region Accessors

        /**
         * ストアトークンを取得します。
         * @return ストアトークン
         */
        public static String getToken() {
            return mToken;
        }

        /*
         * ストアトークンをセットします。
         * @param token ストアトークン
         */
        static void setToken(String token) {
            //isPermanentlyが未指定の場合は、前回のセッションへの保存状況から推測する
            boolean isPermanently = AB.Preference.load(Preference.PREF_KEY_SESSION_TOKEN) != null;
            setToken(token, isPermanently);
        }

        /*
         * ストアトークンをセットします。
         * @param token ストアトークン
         * @param isPermanently true: SharedPreferences に永続化する
         */
        static void setToken(String token, boolean isPermanently) {
            Session.mToken = token;
            if (isPermanently) {
                if (token == null) {
                    AB.Preference.remove(Preference.PREF_KEY_SESSION_TOKEN);
                } else {
                    AB.Preference.save(Preference.PREF_KEY_SESSION_TOKEN, token);
                }
            }
        }

        /**
         * 登録済みのデバイス情報を取得します。
         * @param <T> {@link ABDevice} クラス（またはその派生クラス）
         * @return {@link ABDevice} オブジェクト（またはその派生オブジェクト）
         */
        public static <T extends ABDevice> T getDevice() {
            @SuppressWarnings("unchecked") T device = (T)mDevice; //unsafe cast
            return device;
        }

        /*
         * 登録済みのデバイス情報をセットします。
         * @param {@link ABDevice} オブジェクト（またはその派生オブジェクト）
         * @param <T> {@link ABDevice} クラス（またはその派生クラス）
         */
        static <T extends ABDevice> void setDevice(T device) {
            //isPermanentlyが未指定の場合は、前回のセッションへの保存状況から推測する
            boolean isPermanently = AB.Preference.load(Preference.PREF_KEY_SESSION_TOKEN) != null;
            setDevice(device, isPermanently);
        }

        /*
         * 登録済みのデバイス情報をセットします。
         * @param {@link ABDevice} オブジェクト（またはその派生オブジェクト）
         * @param isPermanently true: SharedPreferences に永続化する
         * @param <T> {@link ABDevice} クラス（またはその派生クラス）
         */
        static <T extends ABDevice> void setDevice(T device, boolean isPermanently) {
            Session.mDevice = device;
            if (isPermanently) {
                if (device == null) {
                    AB.Preference.remove(Preference.PREF_KEY_SESSION_DEVICE);
                } else {
                    AB.Preference.save(Preference.PREF_KEY_SESSION_DEVICE, device);
                }
            }
        }

        /**
         * ログイン済みユーザ情報を取得します。
         * @param <T> {@link ABUser} クラス（またはその派生クラス）
         * @return {@link ABUser} オブジェクト（またはその派生オブジェクト）
         */
        public static <T extends ABUser> T getUser() {
            @SuppressWarnings("unchecked") T user = (T)mUser; //unsafe cast
            return user;
        }

        /*
         * ログイン済みユーザ情報をセットします。
         * @param user {@link ABUser} オブジェクト（またはその派生オブジェクト）
         * @param <T> {@link ABUser} クラス（またはその派生クラス）
         */
        static <T extends ABUser> void setUser(T user) {
            //isPermanentlyが未指定の場合は、前回のセッションへの保存状況から推測する
            boolean isPermanently = AB.Preference.load(Preference.PREF_KEY_SESSION_USER) != null;
            setUser(user, isPermanently);
        }

        /*
         * ログイン済みユーザ情報をセットします。
         * @param user {@link ABuser} オブジェクト（またはその派生オブジェクト）
         * @param isPermanently true: SharedPreferences に永続化する
         * @param <T> {@link ABUser} クラス（またはその派生クラス）
         */
        static <T extends ABUser> void setUser(T user, boolean isPermanently) {
            Session.mUser = user;
            if (isPermanently) {
                if (user == null) {
                    AB.Preference.remove(Preference.PREF_KEY_SESSION_USER);
                } else {
                    AB.Preference.save(Preference.PREF_KEY_SESSION_USER, user);
                }
            }
        }

//endregion
    }

    /**
     * プリファレンス。
     * <p>セッション情報の永続化などに使用します。</p>
     */
    public static class Preference {

        static final String PREFERENCE_NAME = "AB_Pref";

        static final String PREF_KEY_SDK_VERSION = "ab.sdk.version";
        static final String PREF_KEY_APP_VERSION = "ab.app.version";
        static final String PREF_KEY_APP_VERSION_WHEN_REG_ID_FETCHED = "ab.app.version.forregid";
        static final String PREF_KEY_LOGIN_AUTO = "ab.login.auto"; //TODO: どう互換性をもたせるか
        static final String PREF_KEY_ANONYMOUS_UUID = "ab.anonymous.uuid";
        static final String PREF_KEY_SESSION_REG_ID = "ab.session.regid";
        static final String PREF_KEY_SESSION_DEVICE = "ab.session.device";
        static final String PREF_KEY_SESSION_DEVICE_ATTRIBUTES = "ab.session.device.attributes"; //TODO: どう互換性をもたせるか
        static final String PREF_KEY_SESSION_TOKEN = "ab.session.token";
        static final String PREF_KEY_SESSION_USER = "ab.session.user";
        static final String PREF_KEY_SESSION_USER_ID = "ab.session.user.id"; //TODO: どう互換性をもたせるか
        static final String PREF_KEY_SESSION_AUTH_DATA = "ab.session.authdata"; //TODO: どう互換性をもたせるか
        static final String PREF_KEY_SESSION_TWITTER = "ab.session.twitter";
        static final String PREF_KEY_SESSION_TWITTER_TOKEN = "ab.session.twitter.token";
        static final String PREF_KEY_SESSION_TWITTER_TOKEN_SECRET = "ab.session.twitter.tokensecret";
        static final String PREF_KEY_SESSION_TWITTER_USER_ID = "ab.session.twitter.userid";
        static final String PREF_KEY_SESSION_TWITTER_SCREEN_NAME = "ab.session.twitter.screenname";

        static final String V1_4_PREFERENCE_NAME = "APIS_Pref";
        static final String V1_4_PREF_KEY_LOGIN_AUTO = "apis_auto_login";
        static final String V1_4_PREF_KEY_ANONYMOUS_UUID = "anonymous.uuid";
        static final String V1_4_PREF_KEY_SESSION_TOKEN = "apis_store_token";
        static final String V1_4_PREF_KEY_SESSION_USER_ID = "apis_user_id";
        static final String V1_4_PREF_KEY_SESSION_AUTH_DATA = "apis_auth_data";
        static final String V1_4_PREF_KEY_SESSION_REG_ID = "apis_registrationID";
        static final String V1_4_PREF_KEY_SESSION_DEVICE_ATTRIBUTES = "apis_attr";
        static final String V1_4_PREF_KEY_SESSION_TWITTER_TOKEN = "twitter.oauthAccessToken.token";
        static final String V1_4_PREF_KEY_SESSION_TWITTER_TOKEN_SECRET = "twitter.oauthAccessToken.tokenSecret";
        static final String V1_4_PREF_KEY_SESSION_TWITTER_USER_ID = "twitter.oauthAccessToken.userId";
        static final String V1_4_PREF_KEY_SESSION_TWITTER_SCREEN_NAME = "twitter.oauthAccessToken.screenName";

        /**
         * SharedPreferences にデータを保存します。
         * @param key キー
         * @param value 値
         * @param <T> 値value のデータ型
         */
        public static <T> void save(String key, T value) {
            SharedPreferences prefs = sApplicationContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                if (editor != null) {
                    if (value.getClass().equals(String.class)) {
                        editor.putString(key, (String) value);
                    } else if (value.getClass().equals(Set.class)) {
                        Set<?> set = (Set<?>)value;
                        Object[] setValues = set.toArray();
                        boolean isString = setValues[0].getClass().equals(String.class);
                        if (isString) {
                            @SuppressWarnings("unchecked") Set<String> casted = (Set<String>)value; //XXX: unsafe cast
                            editor.putStringSet(key, casted);
                        }
                    } else if (value.getClass().equals(Integer.class)) {
                        editor.putInt(key, (Integer) value);
                    } else if (value.getClass().equals(Long.class)) {
                        editor.putLong(key, (Long) value);
                    } else if (value.getClass().equals(Boolean.class)) {
                        editor.putBoolean(key, (Boolean) value);
                    } else if (value.getClass().equals(Float.class)) {
                        editor.putFloat(key, (Float) value);
                    } else {
                        if (value instanceof ABModel) {
                            ABModel obj = (ABModel)value;
                            try {
                                String jsonString = Helper.ModelHelper.toJson(obj);
                                editor.putString(key, jsonString);
                            } catch (ABException e) {
                                ABLog.e(TAG, "Failed to store the value to Preferences [reason:" + e.getMessage() + "] [value:" + value + "]");
                            }
                        } else {
                            ABLog.e(TAG, "Cannot store the value (The value is NOT a serializable object) [value:" + value + "]");
                        }
                    }
                    editor.apply();
                }
            }
        }

        /**
         * SharedPreferences からデータをロードします。
         * @param key キー
         * @param <T> 値のデータ型
         * @return 値
         */
        public static <T> T load(String key) {
            SharedPreferences prefs = sApplicationContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            if (prefs != null) {
                if (prefs.contains(key)) {
                    Map<String, ?> map = prefs.getAll();
                    @SuppressWarnings("unchecked") T val = (T)map.get(key); //XXX: unsafe cast
                    return val;
                }
            }
            return null;
        }

        /**
         * SharedPreferences からデータをロードします。
         * @param key キー
         * @param defaultValue 値が取得できなかった場合のデフォルト値
         * @param <T> 値のデータ型
         * @return 値
         */
        public static <T> T load(String key, T defaultValue) {
            SharedPreferences prefs = sApplicationContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            if (prefs != null) {
                if (prefs.contains(key)) {
                    Map<String, ?> map = prefs.getAll();
                    @SuppressWarnings("unchecked") T val = (T)map.get(key); //XXX: unsafe cast
                    return val;
                }
            }
            return defaultValue;
        }

        /**
         * SharedPreferences から指定キーに該当するデータを削除します。
         * @param key キー
         */
        public static void remove(String key) {
            if (key == null || key.length() == 0) return;
            SharedPreferences prefs = sApplicationContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                if (editor != null) {
                    editor.remove(key);
                    editor.apply();
                }
            }
        }

        /**
         * SharedPreferences から AppiariesSDK 管理下のすべてのデータを削除します。
         */
        public static void removeAll() {
            SharedPreferences prefs = sApplicationContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                if (editor != null) {
                    editor.remove(Preference.PREF_KEY_SESSION_REG_ID);
                    editor.remove(Preference.PREF_KEY_SESSION_DEVICE);
                    editor.remove(Preference.PREF_KEY_SESSION_TOKEN);
                    editor.remove(Preference.PREF_KEY_SESSION_USER);
                    editor.remove(Preference.PREF_KEY_SESSION_TWITTER);
                    editor.remove(Preference.PREF_KEY_ANONYMOUS_UUID);
                    editor.apply();
                }
            }
        }
    }

    /**
     * ユーザ・サービス。
     * <p></p>
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=30">アピアリーズドキュメント &raquo; 会員管理</a>
     */
    public static class UserService {

        private static String TAG = UserService.class.getSimpleName();

        private static Map<String, ABAuthenticationProvider> authProviders = new HashMap<String, ABAuthenticationProvider>();

//region Sign-Up

        /**
         * 同期モードでサインアップ（ユーザ作成＋ログイン）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> signUpSynchronously(final T user) throws ABException {
            return signUpSynchronously(user, EnumSet.of(AB.UserSignUpOption.NONE));
        }

        /**
         * 同期モードでサインアップ（ユーザ作成＋ログイン）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.UserSignUpOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> signUpSynchronously(final T user, final AB.UserSignUpOption option) throws ABException {
            return signUpSynchronously(user, EnumSet.of(option));
        }

        /**
         * 同期モードでサインアップ（ユーザ作成＋ログイン）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.UserSignUpOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> signUpSynchronously(final T user, final EnumSet<AB.UserSignUpOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("user", user);
            validate("signUp", params);

            //リクエストBODYの組み立て
            String body = Helper.ModelHelper.toJson(user);
            //TODO: CHECK: login_id, pw, email, authData, attributes

            final boolean isPermanently     = options.contains(AB.UserSignUpOption.LOGIN_AUTOMATICALLY);
            final boolean withoutLogIn      = options.contains(AB.UserSignUpOption.WITHOUT_LOGIN);
            final boolean useIncompleteData = options.contains(AB.UserSignUpOption.USE_INCOMPLETE_DATA);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                    AB.baasUserAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeaders()
            );

            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            Map<String, Object> json = restResult.getData();
            String token = (String)json.get("_token");
            if (token != null && !withoutLogIn) {
                //セッションにストアトークンを保存 (ログイン状態となる)
                AB.Session.setToken(token, isPermanently);
                json.remove("_token");
            }
            //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
            if (json.containsKey("userId")) {
                json.remove("userId");
            }

            @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>)AB.Config.getUserClass();
            T signedUpUser = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), json, false);

            if (useIncompleteData) {
                // AB.Session.getUser() で取得できるオブジェクトには、ID(objectID) しか格納されていない。
                if (signedUpUser != null) {
                    ret.setData(signedUpUser);
                    //セッションにログインユーザを保存
                    AB.Session.setUser(signedUpUser, isPermanently);
                }
                return ret;
            } else {
                // AB.Session.getUser() で取得できるオブジェクトには、完全なユーザ・オブジェクトが格納される。
                ABResult<T> fetchResult = fetchSynchronously(signedUpUser);
                T fetchedUser = fetchResult.getData();
                if (fetchedUser != null) {
                    ret.setData(fetchedUser);
                    //セッションにログインユーザを保存
                    AB.Session.setUser(fetchedUser, isPermanently);
                }
                return ret;
                //NOTE: code は signUp 成功時の 201 が引き継がれる点に注意
            }
        }

        /**
         * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void signUp(final T user) {
            signUp(user, null, EnumSet.of(AB.UserSignUpOption.NONE));
        }

        /**
         * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void signUp(final T user, final ResultCallback<T> callback) {
            signUp(user, callback, EnumSet.of(AB.UserSignUpOption.NONE));
        }

        /**
         * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserSignUpOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void signUp(final T user, final ResultCallback<T> callback, final AB.UserSignUpOption option) {
            signUp(user, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserSignUpOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void signUp(final T user, final ResultCallback<T> callback, final EnumSet<AB.UserSignUpOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("user", user);
                validate("signUp", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            String body;
            try {
                body = Helper.ModelHelper.toJson(user);
                //TODO: CHECK: login_id, pw, email, authData, attributes
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            final boolean isPermanently     = options.contains(AB.UserSignUpOption.LOGIN_AUTOMATICALLY);
            final boolean withoutLogIn      = options.contains(AB.UserSignUpOption.WITHOUT_LOGIN);
            final boolean useIncompleteData = options.contains(AB.UserSignUpOption.USE_INCOMPLETE_DATA);

            //APIの実行
            ABRestClient.POST(
                    AB.baasUserAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeaders(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            final ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                Map<String, Object> json = restResult.getData();
                                String token = (String) json.get("_token");
                                if (token != null && !withoutLogIn) {
                                    //セッションにストアトークンを保存 (ログイン状態となる)
                                    AB.Session.setToken(token, isPermanently);
                                    json.remove("_token");
                                }
                                //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
                                if (json.containsKey("userId")) {
                                    json.remove("userId");
                                }

                                @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>)AB.Config.getUserClass();
                                T signedUpUser = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), json, false);

                                if (useIncompleteData) {
                                    // AB.Session.getUser() で取得できるオブジェクトには、ID(objectID) しか格納されていない。
                                    if (signedUpUser != null) {
                                        ret.setData(signedUpUser);
                                        //セッションにログインユーザを保存
                                        AB.Session.setUser(signedUpUser, isPermanently);
                                    }
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } else {
                                    // AB.Session.getUser() で取得できるオブジェクトには、完全なユーザ・オブジェクトが格納される。
                                    fetch(signedUpUser, new ResultCallback<T>() {
                                        @Override
                                        public void done(ABResult<T> fetchResult, ABException e) {
                                            if (e == null) {
                                                T fetchedUser = fetchResult.getData();
                                                if (fetchedUser != null) {
                                                    ret.setData(fetchedUser);
                                                    //セッションにログインユーザを保存
                                                    AB.Session.setUser(fetchedUser, isPermanently);
                                                }
                                                executeResultCallbackIfNeeded(callback, ret, null);
                                                //NOTE: code は login 成功時の 201 が引き継がれる点に注意
                                            } else {
                                                executeResultCallbackIfNeeded(callback, null, e);
                                            }
                                        }
                                    });
                                }
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Log-In

        /**
         * 同期モードでログインします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> logInSynchronously(final T user) throws ABException {
            return logInSynchronously(user, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * 同期モードでログインします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> logInSynchronously(final T user, final AB.UserLogInOption option) throws ABException {
            return logInSynchronously(user, EnumSet.of(option));
        }

        /**
         * 同期モードでログインします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> logInSynchronously(final T user, final EnumSet<AB.UserLogInOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("user", user);
            validate("logIn", params);

            //リクエストBODYの組み立て
            String body = Helper.ModelHelper.toJson(user);
            //TODO: CHECK: login_id, password (!pw)

            final boolean isPermanently     = options.contains(AB.UserLogInOption.LOGIN_AUTOMATICALLY);
            final boolean useIncompleteData = options.contains(AB.UserLogInOption.USE_INCOMPLETE_DATA);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                    AB.baasTokenAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeaders()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            Map<String, Object> json = restResult.getData();
            String token = (String)json.get("_token");
            if (token != null) {
                //セッションにストアトークンを保存 (ログイン状態となる)
                AB.Session.setToken(token, isPermanently);
                json.remove("_token");
            }
            //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
            if (json.containsKey("userId")) {
                json.remove("userId");
            }

            @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>)AB.Config.getUserClass();
            T loggedInUser = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), json, false);

            if (useIncompleteData) {
                // AB.Session.getUser() で取得できるオブジェクトには、ID(objectID) しか格納されていない。
                if (loggedInUser != null) {
                    ret.setData(loggedInUser);
                    //セッションにログインユーザを保存
                    AB.Session.setUser(loggedInUser, isPermanently);
                }
                return ret;
            } else {
                // AB.Session.getUser() で取得できるオブジェクトには、完全なユーザ・オブジェクトが格納される。
                ABResult<T> fetchResult = fetchSynchronously(loggedInUser);
                T fetchedUser = fetchResult.getData();
                if (fetchedUser != null) {
                    ret.setData(fetchedUser);
                    //セッションにログインユーザを保存
                    AB.Session.setUser(fetchedUser, isPermanently);
                }
                return ret;
                //NOTE: code は login 成功時の 201 が引き継がれる点に注意
            }
        }

        /**
         * 非同期モードでログインします。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void logIn(final T user) {
            logIn(user, null, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * 非同期モードでログインします。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void logIn(final T user, final ResultCallback<T> callback) {
            logIn(user, callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * 非同期モードでログインします。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void logIn(final T user, final ResultCallback<T> callback, final AB.UserLogInOption option) {
            logIn(user, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでログインします。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void logIn(final T user, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("user", user);
                validate("logIn", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            String body;
            try {
                body = Helper.ModelHelper.toJson(user);
                //TODO: CHECK: login_id, password (!pw)
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            final boolean isPermanently     = options.contains(AB.UserLogInOption.LOGIN_AUTOMATICALLY);
            final boolean useIncompleteData = options.contains(AB.UserLogInOption.USE_INCOMPLETE_DATA);

            //APIの実行
            ABRestClient.POST(
                    AB.baasTokenAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeaders(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            final ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                Map<String, Object> json = restResult.getData();
                                String token = (String) json.get("_token");
                                if (token != null) {
                                    //セッションにストアトークンを保存 (ログイン状態となる)
                                    AB.Session.setToken(token, isPermanently);
                                    json.remove("_token");
                                }
                                //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
                                if (json.containsKey("userId")) {
                                    json.remove("userId");
                                }

                                @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>)AB.Config.getUserClass();
                                final T loggedInUser = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), json, false);

                                if (useIncompleteData) {
                                    // AB.Session.getUser() で取得できるオブジェクトには、ID(objectID) しか格納されていない。
                                    if (loggedInUser != null) {
                                        ret.setData(loggedInUser);
                                        //セッションにログインユーザを保存
                                        AB.Session.setUser(loggedInUser, isPermanently);
                                    }
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } else {
                                    // AB.Session.getUser() で取得できるオブジェクトには、完全なユーザ・オブジェクトが格納される。
                                    fetch(loggedInUser, new ResultCallback<T>() {
                                        @Override
                                        public void done(ABResult<T> fetchResult, ABException e) {
                                            if (e == null) {
                                                T fetchedUser = fetchResult.getData();
                                                if (fetchedUser != null) {
                                                    ret.setData(fetchedUser);
                                                    //セッションにログインユーザを保存
                                                    AB.Session.setUser(fetchedUser, isPermanently);
                                                }
                                                executeResultCallbackIfNeeded(callback, ret, null);
                                                //NOTE: code は login 成功時の 201 が引き継がれる点に注意
                                            } else {
                                                executeResultCallbackIfNeeded(callback, null, e);
                                            }
                                        }
                                    });
                                }
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Log-In As Anonymous

        /**
         * 同期モードで匿名ログインします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> logInAsAnonymousSynchronously() throws ABException {
            return logInAsAnonymousSynchronously(EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * 同期モードで匿名ログインします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> logInAsAnonymousSynchronously(final AB.UserLogInOption option) throws ABException {
            return logInAsAnonymousSynchronously(EnumSet.of(option));
        }

        private static ABAnonymousAuthenticationProvider anonymousAuthProvider;
        /**
         * 同期モードで匿名ログインします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> ABResult<T> logInAsAnonymousSynchronously(final EnumSet<AB.UserLogInOption> options) throws ABException {

            if (!authProviders.containsKey(ABAnonymousAuthenticationProvider.ID)) {
                anonymousAuthProvider = new ABAnonymousAuthenticationProvider();
                registerAuthenticationProvider(anonymousAuthProvider);
            }

            Map<String, Object> authData = anonymousAuthProvider.getAuthData();
            return AB.UserService.logInAuthenticateSynchronously(anonymousAuthProvider.getId(), authData, options);
/*
            //リクエストBODYの組み立て
            final Map<String, Object> authData = createAuthDataWithUUID(uuid);
            String body = AB.Helper.JSONHelper.toJson(authData);
            //TO DO: CHECK: login_id, password (!pw)

            final boolean isPermanently = options.contains(AB.UserLogInOption.LOGIN_AUTOMATICALLY);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                    AB.baasTokenAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeaders()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            Map<String, Object> json = restResult.getData();
            String token = (String)json.get("_token");
            if (token != null) { //NOTE: リクエストヘッダにストアトークンを指定した場合は _token は返されないみたい
                //セッションにストアトークンを保存 (ログイン状態となる)
                AB.Session.setToken(token, isPermanently);
                json.remove("_token");
            }
            //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
            if (json.containsKey("userId")) {
                json.remove("userId");
            }

            T loggedInUser = AB.Helper.JSONHelper.toObject(json, false);
            ret.setData(loggedInUser);

            if (loggedInUser != null) {
                //AnonymousのauthDataを追加
                //NOTE: 2015.3時点のサーバ実装では複数のauthDataは保持できないので、常に置換する
                //if (loggedInUser.getAuthData() != null) {
                //    Map<String, Object> tempAuthData = new HashMap<String, Object>(loggedInUser.getAuthData());
                //    Map<String, Object> anonymousAuthData = createAnonymousAuthDataWithUUID(uuid);
                //    tempAuthData.put(AB.AUTHENTICATION_PROVIDER_ANONYMOUS_KEY, anonymousAuthData);
                //    loggedInUser.setAuthData(tempAuthData);
                //} else {
                //    loggedInUser.setAuthData(authData);
                //}
                @SuppressWarnings("unchecked") Map<String, Object> newAuthData = (Map<String, Object>)authData.get(AB.JSON_AUTH_DATA_KEY);
                loggedInUser.setAuthData(newAuthData);

                //セッションにログインユーザを保存
                AB.Session.setUser(loggedInUser, isPermanently);
            }

            return ret;
*/
/*
            //匿名認証用UUID(=User.ID)の生成
            //NOTE: 匿名認証用のUUIDは1つの端末で同じUUIDを使い回す仕様(ログアウト後に再度匿名ログインした場合に前回生成したUUIDを使用する)のため、
            //      1度生成したUUIDはSharedPreferencesへ保存し、必要に応じてロードして再利用する。
            //      ATTENTION: ただし、アプリをアンインストールした場合はSharedPreferencesへ保存したデータも消えるため、その際は別のUUIDが生成されることになる。
            String uuid_ = AB.Preference.load(Preference.PREF_KEY_ANONYMOUS_UUID);
            if (uuid_ == null) {
                uuid_ = UUID.randomUUID().toString().toUpperCase();
                AB.Preference.save(Preference.PREF_KEY_ANONYMOUS_UUID, uuid_);
            }
            final String uuid = uuid_;

            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("uuid", uuid);
            validate("logInAsAnonymous", params);

            //リクエストBODYの組み立て
            final Map<String, Object> authData = createAuthDataWithUUID(uuid);
            String body = AB.Helper.JSONHelper.toJson(authData);
            //TO DO: CHECK: login_id, password (!pw)

            final boolean isPermanently = options.contains(AB.UserLogInOption.LOGIN_AUTOMATICALLY);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                    AB.baasTokenAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeaders()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            Map<String, Object> json = restResult.getData();
            String token = (String)json.get("_token");
            if (token != null) { //NOTE: リクエストヘッダにストアトークンを指定した場合は _token は返されないみたい
                //セッションにストアトークンを保存 (ログイン状態となる)
                AB.Session.setToken(token, isPermanently);
                json.remove("_token");
            }
            //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
            if (json.containsKey("userId")) {
                json.remove("userId");
            }

            T loggedInUser = AB.Helper.JSONHelper.toObject(json, false);
            ret.setData(loggedInUser);

            if (loggedInUser != null) {
                //AnonymousのauthDataを追加
                //NOTE: 2015.3時点のサーバ実装では複数のauthDataは保持できないので、常に置換する
                //if (loggedInUser.getAuthData() != null) {
                //    Map<String, Object> tempAuthData = new HashMap<String, Object>(loggedInUser.getAuthData());
                //    Map<String, Object> anonymousAuthData = createAnonymousAuthDataWithUUID(uuid);
                //    tempAuthData.put(AB.AUTHENTICATION_PROVIDER_ANONYMOUS_KEY, anonymousAuthData);
                //    loggedInUser.setAuthData(tempAuthData);
                //} else {
                //    loggedInUser.setAuthData(authData);
                //}
                @SuppressWarnings("unchecked") Map<String, Object> newAuthData = (Map<String, Object>)authData.get(AB.JSON_AUTH_DATA_KEY);
                loggedInUser.setAuthData(newAuthData);

                //セッションにログインユーザを保存
                AB.Session.setUser(loggedInUser, isPermanently);
            }

            return ret;
*/
        }

        /**
         * 非同期モードで匿名ログインします。
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void logInAsAnonymous() {
            logInAsAnonymous(null, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * 非同期モードで匿名ログインします。
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void logInAsAnonymous(final ResultCallback<T> callback) {
            logInAsAnonymous(callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * 非同期モードで匿名ログインします。
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void logInAsAnonymous(final ResultCallback<T> callback, AB.UserLogInOption option) {
            logInAsAnonymous(callback, EnumSet.of(option));
        }

        /**
         * 非同期モードで匿名ログインします。
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
         */
        public static <T extends ABUser> void logInAsAnonymous(final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {

            if (!authProviders.containsKey(ABAnonymousAuthenticationProvider.ID)) {
                anonymousAuthProvider = new ABAnonymousAuthenticationProvider();
                registerAuthenticationProvider(anonymousAuthProvider);
            }

            final String providerId = anonymousAuthProvider.getId();
            AB.UserService.logInAuthenticate(providerId, new ABAuthenticationProvider.AuthenticationCallback() {
                @Override
                public void onSuccess(Map<String, Object> authData) {
                    AB.UserService.logInAuthenticateEnd(providerId, authData, callback, options);
                }

                @Override
                public void onError(Throwable throwable) {
                    /** unused */
                    throw new UnsupportedOperationException();
                }

                @Override
                public void onCancel() {
                    /** unused */
                    throw new UnsupportedOperationException();
                }
            });
/*
            //匿名認証用UUID(=User.ID)の生成
            //NOTE: 匿名認証用のUUIDは1つの端末で同じUUIDを使い回す仕様(ログアウト後に再度匿名ログインした場合に前回生成したUUIDを使用する)のため、
            //      1度生成したUUIDはSharedPreferencesへ保存し、必要に応じてロードして再利用する。
            //      ATTENTION: ただし、アプリをアンインストールした場合はSharedPreferencesへ保存したデータも消えるため、その際は別のUUIDが生成されることになる。
            String uuid_ = AB.Preference.load(Preference.PREF_KEY_ANONYMOUS_UUID);
            if (uuid_ == null) {
                uuid_ = UUID.randomUUID().toString().toUpperCase();
                AB.Preference.save(Preference.PREF_KEY_ANONYMOUS_UUID, uuid_);
            }
            final String uuid = uuid_;

            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("uuid", uuid);
                validate("logInAsAnonymous", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            final Map<String, Object> authData = createAuthDataWithUUID(uuid);
            String body;
            try {
                body = AB.Helper.JSONHelper.toJson(authData);
                //TO DO: CHECK: login_id, password (!pw)
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            final boolean isPermanently = options.contains(AB.UserLogInOption.LOGIN_AUTOMATICALLY);

            //APIの実行
            ABRestClient.POST(
                    AB.baasTokenAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeaders(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                Map<String, Object> json = restResult.getData();
                                String token = (String) json.get("_token");
                                if (token != null) { //NOTE: リクエストヘッダにストアトークンを指定した場合は _token は返されないみたい
                                    //セッションにストアトークンを保存 (ログイン状態となる)
                                    AB.Session.setToken(token, isPermanently);
                                    json.remove("_token");
                                }
                                //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
                                if (json.containsKey("userId")) {
                                    json.remove("userId");
                                }

                                T loggedInUser = AB.Helper.JSONHelper.toObject(json, false);
                                ret.setData(loggedInUser);

                                if (loggedInUser != null) {
                                    //AnonymousのauthDataを追加
                                    //NOTE: 2015.3時点のサーバ実装では複数のauthDataは保持できないので、常に置換する
                                    //if (loggedInUser.getAuthData() != null) {
                                    //    Map<String, Object> tempAuthData = new HashMap<String, Object>(loggedInUser.getAuthData());
                                    //    Map<String, Object> anonymousAuthData = createAnonymousAuthDataWithUUID(uuid);
                                    //    tempAuthData.put(AB.AUTHENTICATION_PROVIDER_ANONYMOUS_KEY, anonymousAuthData);
                                    //    loggedInUser.setAuthData(tempAuthData);
                                    //} else {
                                    //    loggedInUser.setAuthData(authData);
                                    //}
                                    @SuppressWarnings("unchecked") Map<String, Object> newAuthData = (Map<String, Object>) authData.get(AB.JSON_AUTH_DATA_KEY);
                                    loggedInUser.setAuthData(newAuthData);

                                    //セッションにログインユーザを保存
                                    AB.Session.setUser(loggedInUser, isPermanently);
                                }
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
*/
        }

//endregion

//region Log-In With AuthenticationProvider

        //認証プロバイダを登録する
        static void registerAuthenticationProvider(final ABAuthenticationProvider provider) {
            authProviders.put(provider.getId(), provider);
        }

        //認証プロバイダを使用して認証する
        @SafeVarargs
        static <T extends ABUser> ABResult<T> logInAuthenticateSynchronously(final String providerId, final Map<String, Object> authData, final EnumSet<AB.UserLogInOption> options, final Class<T>...type) throws ABException {
            //リクエストBODYの組み立て
            final Map<String, Object> rootAuthData = new HashMap<>();
            rootAuthData.put(AB.JSON_AUTH_DATA_KEY, new HashMap<String, Object>(){{ put(providerId, authData); }});
            String body = Helper.ModelHelper.toJson(rootAuthData);

            final boolean isPermanently = options.contains(AB.UserLogInOption.LOGIN_AUTOMATICALLY);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                    AB.baasTokenAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeaders()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            Map<String, Object> json = restResult.getData();
            String token = (String)json.get("_token");
            if (token != null) { //NOTE: リクエストヘッダにストアトークンを指定した場合は _token は返されないみたい
                //セッションにストアトークンを保存 (ログイン状態となる)
                AB.Session.setToken(token, isPermanently);
                json.remove("_token");
            }
            //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
            if (json.containsKey("userId")) {
                json.remove("userId");
            }

            @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>) type.getClass().getComponentType();
            T loggedInUser = Helper.ModelHelper.toObject(clazz, null, json, false);

            boolean useIncompleteData = options.contains(AB.UserLogInOption.USE_INCOMPLETE_DATA);
            if (useIncompleteData) {
                // AB.Session.getUser() で取得できるオブジェクトには、ID(objectID) しか格納されていない。
                if (loggedInUser != null) {
                    //authDataを追加
                    @SuppressWarnings("unchecked") Map<String, Object> newAuthData = (Map<String, Object>)authData.get(AB.JSON_AUTH_DATA_KEY);
                    loggedInUser.setAuthData(newAuthData);
                    loggedInUser.apply();

                    ret.setData(loggedInUser);

                    //セッションにログインユーザを保存
                    AB.Session.setUser(loggedInUser, isPermanently);
                }
                return ret;
            } else {
                // AB.Session.getUser() で取得できるオブジェクトには、完全なユーザ・オブジェクトが格納される。
                ABResult<T> fetchResult = fetchSynchronously(loggedInUser);
                T fetchedUser = fetchResult.getData();
                if (fetchedUser != null) {
                    //authDataを追加
                    @SuppressWarnings("unchecked") Map<String, Object> newAuthData = (Map<String, Object>)authData.get(AB.JSON_AUTH_DATA_KEY);
                    loggedInUser.setAuthData(newAuthData);

                    ret.setData(fetchedUser);

                    //セッションにログインユーザを保存
                    AB.Session.setUser(fetchedUser, isPermanently);
                }
                return ret;
                //NOTE: code は login 成功時の 201 が引き継がれる点に注意
            }

            /*
            if (!sActivated()) {
                throw new ABException("APIS class not initialized.");
            }

            setAutoLogin(autoLogin);

            HttpPost post = new HttpPost((new StringBuilder(mUserAPIBaseURL)).append("?proc=create&get=true").toString());
            addHttpHeaders4Appiaries(post);
            if (authData == null) {
                throw new APISException(new IllegalArgumentException("Authentication data is empty."));
            }
            boolean hasError = false;
            try {
                String json = "{\"authData\":{\"" + providerId + "\":" + authData.toString() + "}}";
                APISUtil.debug(json);
                post.setEntity(new StringEntity(json, "UTF-8"));
                APISResult response = executeRequest(post);
                if (HttpStatus.SC_CREATED == response.getResponseCode() || HttpStatus.SC_OK == response.getResponseCode()) {
                    setStoreToken((String) response.getResponseData().get("_token")); //note: store_token
                    setUserId((String) response.getResponseData().get("_id"));
                    addAuthData(providerId, authData); //NOTE: 同名プロバイダが既に登録されていた場合は置き換える
                } else {
                    hasError = true;
                }
                APISUtil.debug((new StringBuilder("code:")).append(response.getResponseCode()).append(",res:").append(response.getResponseMessage()).append(",loc:").append(response.getLocation()).toString());
                return response;
            } catch (APISException e) {
                APISUtil.error(e);
                throw e;
            } catch (Exception e) {
                APISUtil.error(e);
                throw new APISException(e);
            } finally {
                //会員登録に失敗した場合はログアウト状態にする
                if (hasError) {
                    setStoreToken("");
                    setUserId("");
                    clearAuthData();
                }
            }
            */
        }

        /**
         * 認証プロバイダを介して認証処理を実行する。
         * <p></p>
         *
         * @param providerId 認証プロバイダID
         * @param callback   コールバック
         * @since 1.3.0
         */
        static void logInAuthenticate(final String providerId, final ABAuthenticationProvider.AuthenticationCallback callback) {
            ABAuthenticationProvider provider = authProviders.get(providerId);
            provider.authenticate(callback);
        }

        /**
         * 認証プロバイダを介した認証処理を完了する。
         * <p></p>
         *
         * @param providerId 認証プロバイダID
         * @param authData   認証データ
         * @param callback   コールバック
         * @param options    オプション
         * @since 1.3.0
         */
        static <T extends ABUser> void logInAuthenticateEnd(final String providerId, final Map<String, Object> authData, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            //リクエストBODYの組み立て
            String body;
            try {
                final Map<String, Object> rootAuthData = new HashMap<>();
                rootAuthData.put(AB.JSON_AUTH_DATA_KEY, new HashMap<String, Object>(){{ put(providerId, authData); }});
                body = Helper.ModelHelper.toJson(rootAuthData);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            final boolean isPermanently = options.contains(AB.UserLogInOption.LOGIN_AUTOMATICALLY);

            //APIの実行
            ABRestClient.POST(
                    //AB.baasUserAPIURL("?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    AB.baasUserAPIURL(null), //FIXME: BUG: path に "?get=true" を指定すると 500 エラーが返される
                    body,
                    ABRestClient.getDefaultHeaders(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            final ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                Map<String, Object> json = restResult.getData();
                                String token = (String) json.get("_token");
                                if (token != null) { //NOTE: リクエストヘッダにストアトークンを指定した場合は _token は返されないみたい
                                    //セッションにストアトークンを保存 (ログイン状態となる)
                                    AB.Session.setToken(token, isPermanently);
                                    json.remove("_token");
                                }
                                //XXX: 将来的にレスポンスから削除される予定の "userId" が含まれていた場合は削除する
                                if (json.containsKey("userId")) {
                                    json.remove("userId");
                                }

                                @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>)AB.Config.getUserClass();
                                T loggedInUser = Helper.ModelHelper.toObject(clazz, null, json, false);

                                boolean useIncompleteData = options.contains(AB.UserLogInOption.USE_INCOMPLETE_DATA);
                                if (useIncompleteData) {
                                    // AB.Session.getUser() で取得できるオブジェクトには、ID(objectID) しか格納されていない。
                                    if (loggedInUser != null) {
                                        //authDataを追加
                                        /* //NOTE: 2015.3時点のサーバ実装では複数のauthDataは保持できないので、常に置換する */
                                        loggedInUser.setAuthData(new HashMap<String, Object>(){{ put(providerId, authData); }});
                                        loggedInUser.apply();

                                        ret.setData(loggedInUser);

                                        //セッションにログインユーザを保存
                                        AB.Session.setUser(loggedInUser, isPermanently);
                                    }
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } else {
                                    // AB.Session.getUser() で取得できるオブジェクトには、完全なユーザ・オブジェクトが格納される。
                                    fetch(loggedInUser, new ResultCallback<T>() {
                                        @Override
                                        public void done(ABResult<T> fetchResult, ABException e) {
                                            if (e == null) {
                                                T fetchedUser = fetchResult.getData();
                                                if (fetchedUser != null) {
                                                    //authDataを追加
                                                    /* //NOTE: 2015.3時点のサーバ実装では複数のauthDataは保持できないので、常に置換する */
                                                    fetchedUser.setAuthData(new HashMap<String, Object>(){{ put(providerId, authData); }});
                                                    fetchedUser.apply();

                                                    ret.setData(fetchedUser);

                                                    //セッションにログインユーザを保存
                                                    AB.Session.setUser(fetchedUser, isPermanently);
                                                }
                                                executeResultCallbackIfNeeded(callback, ret, null);
                                                //NOTE: code は login 成功時の 201 が引き継がれる点に注意
                                            } else {
                                                executeResultCallbackIfNeeded(callback, null, e);
                                            }
                                        }
                                    });
                                }
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Log-Out

        /**
         * 同期モードでログアウトします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
         */
        public static <T extends ABUser> ABResult<Void> logOutSynchronously(final T user) throws ABException {
            return logOutSynchronously(user, EnumSet.of(AB.UserLogOutOption.NONE));
        }

        /**
         * 同期モードでログアウトします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.UserLogOutOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
         */
        public static <T extends ABUser> ABResult<Void> logOutSynchronously(final T user, final AB.UserLogOutOption option) throws ABException {
            return logOutSynchronously(user, EnumSet.of(option));
        }

        /**
         * 同期モードでログアウトします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.UserLogOutOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
         */
        public static <T extends ABUser> ABResult<Void> logOutSynchronously(final T user, final EnumSet<AB.UserLogOutOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("user", user);
            validate("logOut", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasTokenAPIURL("/_self"),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            //セッションのストアトークン/ログインユーザ情報をクリア (非ログイン状態となる)
            AB.Session.invalidateToken();
            AB.Session.invalidateUser();

            //その他ログアウト後処理
            logOutPostProcess();

            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        /**
         * 非同期モードでログアウトします。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
         */
        public static <T extends ABUser> void logOut(final T user) {
            logOut(user, null, EnumSet.of(AB.UserLogOutOption.NONE));
        }

        /**
         * 非同期モードでログアウトします。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
         */
        public static <T extends ABUser> void logOut(final T user, final ResultCallback<Void> callback) {
            logOut(user, callback, EnumSet.of(AB.UserLogOutOption.NONE));
        }

        /**
         * 非同期モードでログアウトします。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogOutOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
         */
        public static <T extends ABUser> void logOut(final T user, final ResultCallback<Void> callback, final AB.UserLogOutOption option) {
            logOut(user, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでログアウトします。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogOutOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
         */
        public static <T extends ABUser> void logOut(final T user, final ResultCallback<Void> callback, final EnumSet<AB.UserLogOutOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("user", user);
                validate("logOut", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasTokenAPIURL("/_self"),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                             //ログアウト成功時
                            //セッションのストアトークン/ログインユーザ情報をクリア (非ログイン状態となる)
                            AB.Session.invalidateToken();
                            AB.Session.invalidateUser();

                            //その他ログアウト後処理
                            logOutPostProcess();

                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

        /*
         * 会員ログアウト後の後処理。
         */
        static void logOutPostProcess() {
            for (Map.Entry<String, ABAuthenticationProvider> entry : authProviders.entrySet()) {
                ABAuthenticationProvider provider = entry.getValue();
                provider.logOut();
            }
        }

//endregion

//region Verify Email

        /**
         * 同期モードで本人確認メール送信をリクエストします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param email 本人確認メールの宛先メールアドレス
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=12891">アピアリーズドキュメント &raquo; メールアドレス確認通知を依頼する</a>
         */
        public static ABResult<Void> verifyEmailSynchronously(final String email) throws ABException {
            return verifyEmailSynchronously(email, EnumSet.of(AB.UserVerifyEmailOption.NONE));
        }

        /**
         * 同期モードで本人確認メール送信をリクエストします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param email 本人確認メールの宛先メールアドレス
         * @param option {@link AB.UserVerifyEmailOption} オプション
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=12891">アピアリーズドキュメント &raquo; メールアドレス確認通知を依頼する</a>
         */
        public static ABResult<Void> verifyEmailSynchronously(final String email, final AB.UserVerifyEmailOption option) throws ABException {
            return verifyEmailSynchronously(email, EnumSet.of(option));
        }

        /**
         * 同期モードで本人確認メール送信をリクエストします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param email 本人確認メールの宛先メールアドレス
         * @param options {@link AB.UserVerifyEmailOption} オプション群
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=12891">アピアリーズドキュメント &raquo; メールアドレス確認通知を依頼する</a>
         */
        public static ABResult<Void> verifyEmailSynchronously(final String email, final EnumSet<AB.UserVerifyEmailOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("email", email);
            validate("verifyEmail", params);

            //リクエストBODYの組み立て
            final Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("email", email); }};
            String body = Helper.ModelHelper.toJson(bodyMap);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                    AB.baasUserAPIURL("/_new/requestEmail"),
                    body,
                    ABRestClient.getDefaultHeaders()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        /**
         * 非同期モードで本人確認メール送信をリクエストします。
         * @param email 本人確認メールの宛先メールアドレス
         * @see <a href="http://docs.appiaries.com/?p=12891">アピアリーズドキュメント &raquo; メールアドレス確認通知を依頼する</a>
         */
        public static void verifyEmail(final String email) {
            verifyEmail(email, null, EnumSet.of(AB.UserVerifyEmailOption.NONE));
        }

        /**
         * 非同期モードで本人確認メール送信をリクエストします。
         * @param email 本人確認メールの宛先メールアドレス
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @see <a href="http://docs.appiaries.com/?p=12891">アピアリーズドキュメント &raquo; メールアドレス確認通知を依頼する</a>
         */
        public static void verifyEmail(final String email, final ResultCallback<Void> callback) {
            verifyEmail(email, callback, EnumSet.of(AB.UserVerifyEmailOption.NONE));
        }

        /**
         * 非同期モードで本人確認メール送信をリクエストします。
         * @param email 本人確認メールの宛先メールアドレス
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserVerifyEmailOption} オプション
         * @see <a href="http://docs.appiaries.com/?p=12891">アピアリーズドキュメント &raquo; メールアドレス確認通知を依頼する</a>
         */
        public static void verifyEmail(final String email, final ResultCallback<Void> callback, final AB.UserVerifyEmailOption option) {
            verifyEmail(email, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードで本人確認メール送信をリクエストします。
         * @param email 本人確認メールの宛先メールアドレス
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserVerifyEmailOption} オプション群
         * @see <a href="http://docs.appiaries.com/?p=12891">アピアリーズドキュメント &raquo; メールアドレス確認通知を依頼する</a>
         */
        public static void verifyEmail(final String email, final ResultCallback<Void> callback, final EnumSet<AB.UserVerifyEmailOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("email", email);
                validate("verifyEmail", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            final Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("email", email); }};
            String body;
            try {
                body = Helper.ModelHelper.toJson(bodyMap);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.POST(
                    AB.baasUserAPIURL("/_new/requestEmail"),
                    body,
                    ABRestClient.getDefaultHeaders(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

//endregion

//region Reset Password

        /**
         * 同期モードでパスワード再設定メール送信をリクエストします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param email パスワード再設定メールの宛先メールアドレス
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> resetPasswordForEmailSynchronously(final String email) throws ABException {
            return resetPasswordForEmailSynchronously(email, EnumSet.of(AB.UserResetPasswordOption.NONE));
        }

        /**
         * 同期モードでパスワード再設定メール送信をリクエストします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param email パスワード再設定メールの宛先メールアドレス
         * @param option {@link AB.UserResetPasswordOption} オプション
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> resetPasswordForEmailSynchronously(final String email, final AB.UserResetPasswordOption option) throws ABException {
            return resetPasswordForEmailSynchronously(email, EnumSet.of(option));
        }

        /**
         * 同期モードでパスワード再設定メール送信をリクエストします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param email パスワード再設定メールの宛先メールアドレス
         * @param options {@link AB.UserResetPasswordOption} オプション群
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> resetPasswordForEmailSynchronously(final String email, final EnumSet<AB.UserResetPasswordOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("email", email);
            validate("resetPassword", params);

            //リクエストBODYの組み立て
            final Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("email", email); }};
            String body = Helper.ModelHelper.toJson(bodyMap);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                    AB.baasUserAPIURL("/_self/resetPassword"),
                    body,
                    ABRestClient.getDefaultHeaders()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        /**
         * 非同期モードでパスワード再設定メール送信をリクエストします。
         * @param email パスワード再設定メールの宛先メールアドレス
         */
        public static void resetPasswordForEmail(final String email) {
            resetPasswordForEmail(email, null, EnumSet.of(AB.UserResetPasswordOption.NONE));
        }

        /**
         * 非同期モードでパスワード再設定メール送信をリクエストします。
         * @param email パスワード再設定メールの宛先メールアドレス
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         */
        public static void resetPasswordForEmail(final String email, final ResultCallback<Void> callback) {
            resetPasswordForEmail(email, callback, EnumSet.of(AB.UserResetPasswordOption.NONE));
        }

        /**
         * 非同期モードでパスワード再設定メール送信をリクエストします。
         * @param email パスワード再設定メールの宛先メールアドレス
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserResetPasswordOption} オプション
         */
        public static void resetPasswordForEmail(final String email, final ResultCallback<Void> callback, final AB.UserResetPasswordOption option) {
            resetPasswordForEmail(email, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでパスワード再設定メール送信をリクエストします。
         * @param email パスワード再設定メールの宛先メールアドレス
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserResetPasswordOption} オプション群
         */
        public static void resetPasswordForEmail(final String email, final ResultCallback<Void> callback, final EnumSet<AB.UserResetPasswordOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("email", email);
                validate("resetPassword", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            final Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("email", email); }};
            String body;
            try {
                body = Helper.ModelHelper.toJson(bodyMap);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.POST(
                    AB.baasUserAPIURL("/_self/resetPassword"),
                    body,
                    ABRestClient.getDefaultHeaders(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

//endregion

//region Save

        /**
         * 同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> ABResult<T> saveSynchronously(final T user) throws ABException {
            return saveSynchronously(user, EnumSet.of(AB.UserSaveOption.NONE));
        }

        /**
         * 同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.UserSaveOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> ABResult<T> saveSynchronously(final T user, final AB.UserSaveOption option) throws ABException {
            return saveSynchronously(user, EnumSet.of(option));
        }

        /**
         * 同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.UserSaveOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> ABResult<T> saveSynchronously(final T user, final EnumSet<AB.UserSaveOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("user", user);
            validate("save", params);

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>)AB.Config.getUserClass();

            //未登録の場合
            if (user.isNew()) {

                //リクエストBODYの組み立て
                bodyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : user.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = user.outputDataFilter(key, val);
                    bodyMap.put(key, fixedVal);
                }
                body = Helper.ModelHelper.toJson(bodyMap);

                //APIの実行
                ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                        AB.baasUserAPIURLWithFormat("/%s?get=true", user.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        body,
                        ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                );
                ABResult<T> ret = new ABResult<>();
                ret.setCode(restResult.getCode());
                ret.setExtra(restResult.getExtra());
                ret.setRawData(restResult.getRawData());
                T obj = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), restResult.getData(), false);
                ret.setData(obj);

                return ret;
            }

            //更新の場合
            if (user.isDirty()) {

                Map<String, Object> addedMap   = user.getAddedKeysAndValues();
                Map<String, Object> removedMap = user.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = user.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                                removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                                removedCount > 1 || //削除対象フィールドが複数の場合
                                updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : user.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        Object val = entry.getValue();
                        Object fixedVal = user.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    body = Helper.ModelHelper.toJson(bodyMap);

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPUT(
                            AB.baasUserAPIURLWithFormat("/%s/%s?get=true", user.getCollectionID(), user.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    T obj = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), restResult.getData(), false);
                    ret.setData(obj);

                    return ret;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasUserAPIURLWithFormat("/%s/%s/%s?get=true", user.getCollectionID(), user.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T deleted = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) user.clone(); //unsafe cast
                            copied.remove(field);
                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        Object fixedVal = user.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    body = Helper.ModelHelper.toJson(bodyMap);

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPATCH(
                            AB.baasUserAPIURLWithFormat("/%s/%s?get=true", user.getCollectionID(), user.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T patched = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) user.clone(); //unsafe cast
                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            return ret;
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void save(final T user) {
            save(user, null, EnumSet.of(AB.UserSaveOption.NONE));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.UserSaveOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void save(final T user, final AB.UserSaveOption option) {
            save(user, EnumSet.of(option));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.UserSaveOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void save(final T user, final EnumSet<AB.UserSaveOption> options) {
            save(user, null, options);
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void save(final T user, final ResultCallback<T> callback) {
            save(user, callback, EnumSet.of(AB.UserSaveOption.NONE));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserSaveOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void save(final T user, final ResultCallback<T> callback, final AB.UserSaveOption option) {
            save(user, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserSaveOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void save(final T user, final ResultCallback<T> callback, final EnumSet<AB.UserSaveOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("user", user);
                validate("save", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>)AB.Config.getUserClass();

            //未登録の場合
            if (user.isNew()) {

                //リクエストBODYの組み立て
                bodyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : user.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = user.outputDataFilter(key, val);
                    bodyMap.put(key, fixedVal);
                }
                try {
                    body = Helper.ModelHelper.toJson(bodyMap);
                } catch (ABException e) {
                    executeResultCallbackIfNeeded(callback, null, e);
                    return;
                }

                //APIの実行
                ABRestClient.POST(
                        AB.baasUserAPIURLWithFormat("/%s?get=true", user.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        body,
                        ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                        new ResultCallback<Map<String, Object>>() {
                            @Override
                            public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                if (e != null) {
                                    executeResultCallbackIfNeeded(callback, null, e);
                                    return;
                                }
                                ABResult<T> ret = new ABResult<>();
                                ret.setCode(restResult.getCode());
                                ret.setExtra(restResult.getExtra());
                                ret.setRawData(restResult.getRawData());
                                try {
                                    T obj = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), restResult.getData(), false);
                                    ret.setData(obj);
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } catch (ABException ie) {
                                    executeResultCallbackIfNeeded(callback, null, ie);
                                }
                            }
                        });
                return;
            }

            //更新の場合
            if (user.isDirty()) {

                Map<String, Object> addedMap   = user.getAddedKeysAndValues();
                Map<String, Object> removedMap = user.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = user.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                                removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                                removedCount > 1 || //削除対象フィールドが複数の場合
                                updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : user.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        if ("_id".equals(key)) { //FIXME: TEMPORARY : patch の場合 _id が含まれるとエラーになる
                            continue;            //FIXME: TEMPORARY
                        }                        //FIXME: TEMPORARY
                        Object val = entry.getValue();
                        Object fixedVal = user.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    try {
                        body = Helper.ModelHelper.toJson(bodyMap);
                    } catch (ABException e) {
                        executeResultCallbackIfNeeded(callback, null, e);
                        return;
                    }

                    //APIの実行
                    /*ABRestClient.PUT(*/ //TODO: API側でPUTはサポートしていない
                    ABRestClient.PATCH(
                            AB.baasUserAPIURLWithFormat("/%s?get=true", user.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    try {
                                        T obj = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), restResult.getData(), false);
                                        ret.setData(obj);
                                        executeResultCallbackIfNeeded(callback, ret, null);
                                    } catch (ABException ie) {
                                        executeResultCallbackIfNeeded(callback, null, ie);
                                    }
                                }
                            });
                    return;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasUserAPIURLWithFormat("/%s/%s?get=true", user.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABRestClient.DELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T deleted = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) user.clone(); //unsafe cast
                                            copied.remove(field);
                                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            });
                    return;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        Object fixedVal = user.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    try {
                        body = Helper.ModelHelper.toJson(bodyMap);
                    } catch (ABException e) {
                        executeResultCallbackIfNeeded(callback, null, e);
                        return;
                    }

                    //APIの実行
                    ABRestClient.PATCH(
                            AB.baasUserAPIURLWithFormat("/%s?get=true", user.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T patched = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) user.clone(); //unsafe cast
                                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            });
                    return;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            executeResultCallbackIfNeeded(callback, ret, null);
        }

//endregion

//region Save (Objects)

        /**
         * 同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> ABResult<List<T>> saveAllSynchronously(final List<T> users) throws ABException {
            return saveAllSynchronously(users, EnumSet.of(AB.UserSaveOption.NONE));
        }

        /**
         * 同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.UserSaveOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> ABResult<List<T>> saveAllSynchronously(final List<T> users, final AB.UserSaveOption option) throws ABException {
            return saveAllSynchronously(users, EnumSet.of(option));
        }

        /**
         * 同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.UserSaveOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> ABResult<List<T>> saveAllSynchronously(final List<T> users, final EnumSet<AB.UserSaveOption> options) throws ABException {
            ABResult<List<T>> ret = new ABResult<>();
            List<T> savedUsers = new ArrayList<>();
            Integer success = 0;
            for (T user : users) {
                ABResult<T> r = saveSynchronously(user, options);
                if (ret.getCode() >= 200 && ret.getCode() <= 399) {
                    T saved = r.getData();
                    savedUsers.add(saved);
                    success++;
                    ret.setCode(r.getCode());
                }
            }
            ret.setTotal(success);
            ret.setData(savedUsers);
            return ret;
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users) {
            saveAll(users, null, null, EnumSet.of(AB.UserSaveOption.NONE));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.UserSaveOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users, final AB.UserSaveOption option) {
            saveAll(users, null, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.UserSaveOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users, final EnumSet<AB.UserSaveOption> options) {
            saveAll(users, null, null, options);
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users, final ResultCallback<List<T>> callback) {
            saveAll(users, callback, null, EnumSet.of(AB.UserSaveOption.NONE));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback) {
            saveAll(users, callback, progressCallback, EnumSet.of(AB.UserSaveOption.NONE));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserSaveOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users, final ResultCallback<List<T>> callback, final AB.UserSaveOption option) {
            saveAll(users, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserSaveOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users, final ResultCallback<List<T>> callback, final EnumSet<AB.UserSaveOption> options) {
            saveAll(users, callback, null, options);
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param option {@link AB.UserSaveOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final AB.UserSaveOption option) {
            saveAll(users, callback, progressCallback, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアへ保存します。
         * <div class="important">現時点では save メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param options {@link AB.UserSaveOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
         */
        public static <T extends ABUser> void saveAll(final List<T> users, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final EnumSet<AB.UserSaveOption> options) {
            new ABAsyncBatchExecutor(users, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T user = (T) target;
                    user.save(new ResultCallback<T>() {
                        @Override
                        public void done(ABResult<T> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }

//endregion

//region Delete

        /**
         * 同期モードでユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> ABResult<Void> deleteSynchronously(final T user) throws ABException {
            return deleteSynchronously(user, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 同期モードでユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.UserDeleteOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> ABResult<Void> deleteSynchronously(final T user, final AB.UserDeleteOption option) throws ABException {
            return deleteSynchronously(user, EnumSet.of(option));
        }

        /**
         * 同期モードでユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.UserDeleteOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> ABResult<Void> deleteSynchronously(final T user, final EnumSet<AB.UserDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("user", user);
            validate("delete", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasUserAPIURLWithFormat("/%s", user.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );

            //削除したユーザとログイン中のユーザが同一であればセッション情報等をクリアする
            if (user.getID().equals(AB.Session.getUser().getID())) {
                //セッションのストアトークン/ログインユーザ情報をクリア (非ログイン状態となる)
                AB.Session.setToken(null);
                AB.Session.setUser(null);
                //自動ログイン・オプションによりストアトークン/ログインユーザ情報が保存されている場合は破棄する
                //NOTE: デバッグ用途に、削除前にユーザ情報を取得してダンプしたいところだが、
                //      デシリアライズに失敗する可能性も考えられるので、無条件に削除している。
                AB.Config.discard(Preference.PREF_KEY_SESSION_TOKEN);
                AB.Config.discard(Preference.PREF_KEY_SESSION_USER);
            }

            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void delete(final T user) {
            delete(user, null, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.UserDeleteOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void delete(final T user, final AB.UserDeleteOption option) {
            delete(user, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.UserDeleteOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void delete(final T user, final EnumSet<AB.UserDeleteOption> options) {
            delete(user, null, options);
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void delete(final T user, final ResultCallback<Void> callback) {
            delete(user, callback, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserDeleteOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void delete(final T user, final ResultCallback<Void> callback, final AB.UserDeleteOption option) {
            delete(user, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserDeleteOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void delete(final T user, final ResultCallback<Void> callback, final EnumSet<AB.UserDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("user", user);
                validate("delete", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasUserAPIURLWithFormat("/%s", user.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            //削除したユーザとログイン中のユーザが同一であればセッション情報等をクリアする
                            if (user.getID().equals(AB.Session.getUser().getID())) {
                                //セッションのストアトークン/ログインユーザ情報をクリア (非ログイン状態となる)
                                AB.Session.setToken(null);
                                AB.Session.setUser(null);
                                //自動ログイン・オプションによりストアトークン/ログインユーザ情報が保存されている場合は破棄する
                                //NOTE: デバッグ用途に、削除前にユーザ情報を取得してダンプしたいところだが、
                                //      デシリアライズに失敗する可能性も考えられるので、無条件に削除している。
                                AB.Config.discard(Preference.PREF_KEY_SESSION_TOKEN);
                                AB.Config.discard(Preference.PREF_KEY_SESSION_USER);
                            }

                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    }
            );
        }

//endregion

//region Delete (Objects)

        /**
         * 同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> ABResult<Void> deleteAllSynchronously(final List<T> users) throws ABException {
            return deleteAllSynchronously(users, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.UserDeleteOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> ABResult<Void> deleteAllSynchronously(final List<T> users, final AB.UserDeleteOption option) throws ABException {
            return deleteAllSynchronously(users, EnumSet.of(option));
        }

        /**
         * 同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.UserDeleteOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> ABResult<Void> deleteAllSynchronously(final List<T> users, final EnumSet<AB.UserDeleteOption> options) throws ABException {
            ABResult<Void> ret = new ABResult<>();
            Integer success = 0;
            for (T user : users) {
                ret = user.deleteSynchronously(options);
                if (ret.getCode() >= 200 && ret.getCode() <= 399) {
                    success++;
                }
            }
            ret.setTotal(success);
            return ret;
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users) {
            deleteAll(users, null, null, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.UserDeleteOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users, final AB.UserDeleteOption option) {
            deleteAll(users, null, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.UserDeleteOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users, final EnumSet<AB.UserDeleteOption> options) {
            deleteAll(users, null, null, options);
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users, final ResultCallback<Void> callback) {
            deleteAll(users, callback, null, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users, final ResultCallback<Void> callback, final ProgressCallback progressCallback) {
            deleteAll(users, callback, progressCallback, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserDeleteOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users, final ResultCallback<Void> callback, final AB.UserDeleteOption option) {
            deleteAll(users, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserDeleteOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users, final ResultCallback<Void> callback, final EnumSet<AB.UserDeleteOption> options) {
            deleteAll(users, callback, null, options);
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param option {@link AB.UserDeleteOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final AB.UserDeleteOption option) {
            deleteAll(users, callback, progressCallback, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">現時点では delete メソッドを連続実行するだけの実装となっており、オブジェクトの数だけAPI呼び出しが発生する点にご注意ください。</div>
         * @param users {@link ABUser} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param options {@link AB.UserDeleteOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
         */
        public static <T extends ABUser> void deleteAll(final List<T> users, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final EnumSet<AB.UserDeleteOption> options) {
            new ABAsyncBatchExecutor(users, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T user = (T) target;
                    user.delete(new ResultCallback<Void>() {
                        @Override
                        public void done(ABResult<Void> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }

//endregion

//region Delete (Query)

        /**
         * 同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @param option {@link AB.UserDeleteOption} オプション
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final AB.UserDeleteOption option) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(option));
        }

        /**
         * 同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @param options {@link AB.UserDeleteOption} オプション群
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.UserDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("deleteWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasUserAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        /**
         * 非同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         */
        public static void deleteWithQuery(final ABQuery query) {
            deleteWithQuery(query, null, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 非同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param option {@link AB.UserDeleteOption} オプション
         */
        public static void deleteWithQuery(final ABQuery query, final AB.UserDeleteOption option) {
            deleteWithQuery(query, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param options {@link AB.UserDeleteOption} オプション群
         */
        public static void deleteWithQuery(final ABQuery query, final EnumSet<AB.UserDeleteOption> options) {
            deleteWithQuery(query, null, options);
        }

        /**
         * 非同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback) {
            deleteWithQuery(query, callback, EnumSet.of(AB.UserDeleteOption.NONE));
        }

        /**
         * 非同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserDeleteOption} オプション
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final AB.UserDeleteOption option) {
            deleteWithQuery(query, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードで検索条件にマッチするユーザ・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserDeleteOption} オプション群
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final EnumSet<AB.UserDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("deleteWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasUserAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            try {
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (Exception ie) {
                                executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                            }
                        }
                    });
        }

//endregion

//region Fetch

        /**
         * 同期モードでユーザ・オブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント » 会員情報を取得する</a>
         */
        public static <T extends ABUser> ABResult<T> fetchSynchronously(final T user) throws ABException {
            return fetchSynchronously(user, EnumSet.of(AB.UserFetchOption.NONE));
        }

        /**
         * 同期モードでユーザ・オブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.UserFetchOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント » 会員情報を取得する</a>
         */
        public static <T extends ABUser> ABResult<T> fetchSynchronously(final T user, final AB.UserFetchOption option) throws ABException {
            return fetchSynchronously(user, EnumSet.of(option));
        }

        /**
         * 同期モードでユーザ・オブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.UserFetchOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント » 会員情報を取得する</a>
         */
        public static <T extends ABUser> ABResult<T> fetchSynchronously(final T user, final EnumSet<AB.UserFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("user", user);
            validate("fetch", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasUserAPIURLWithFormat("/%s", user.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>)AB.Config.getUserClass();
            T obj = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), restResult.getData(), false);
            ret.setData(obj);

            return ret;
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから取得します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void fetch(final T user, final ResultCallback<T> callback) {
            fetch(user, callback, EnumSet.of(AB.UserFetchOption.NONE));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから取得します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserFetchOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void fetch(final T user, final ResultCallback<T> callback, final AB.UserFetchOption option) {
            fetch(user, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでユーザ・オブジェクトをデータストアから取得します。
         * @param user {@link ABUser} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserFetchOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void fetch(final T user, final ResultCallback<T> callback, final EnumSet<AB.UserFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("user", user);
                validate("fetch", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasUserAPIURLWithFormat("/%s", user.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>)AB.Config.getUserClass();
                                T obj = Helper.ModelHelper.toObject(clazz, user.getCollectionID(), restResult.getData(), false);
                                ret.setData(obj);
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

/*
//region Find (Query)
        public static <T extends ABUser> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(AB.UserFetchOption.NONE));
        }
        public static <T extends ABUser> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final AB.UserFetchOption option) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(option));
        }
        public static <T extends ABUser> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.UserFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("findWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasUserAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<List<T>> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query);
            List<T> objects = AB.Helper.JSONHelper.toObjects(clazz, restResult.getData(), false);
            ret.setData(objects);
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        public static <T extends ABUser> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback) {
            findWithQuery(query, callback, EnumSet.of(AB.UserFetchOption.NONE));
        }
        public static <T extends ABUser> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final AB.UserFetchOption option) {
            findWithQuery(query, callback, EnumSet.of(option));
        }
        public static <T extends ABUser> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final EnumSet<AB.UserFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("findWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasUserAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<List<T>> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query);
                                List<T> objects = AB.Helper.JSONHelper.toObjects(clazz, restResult.getData(), false);
                                ret.setData(objects);
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }
//endregion
*/

//region Cancel

        private static Pattern userRequestUrlPattern = Pattern.compile("/usr/");
        private static Pattern tokenRequestUrlPattern = Pattern.compile("/tkn/");

        /**
         * 実行中の API リクエストをキャンセルします。
         * @deprecated use {@link #cancelAll()} instead.
         */
        @Deprecated
        public static void cancel() {
            ABRestClient.cancel(userRequestUrlPattern);
            ABRestClient.cancel(tokenRequestUrlPattern);
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         */
        public static void cancelAll() {
            cancelAll(EnumSet.of(AB.UserCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param option {@link AB.UserCancelOption} オプション
         */
        public static void cancelAll(final AB.UserCancelOption option) {
            cancelAll(EnumSet.of(AB.UserCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param options {@link AB.UserCancelOption} オプション群
         */
        public static void cancelAll(final EnumSet<AB.UserCancelOption> options) {
            ABRestClient.cancel(userRequestUrlPattern);
            ABRestClient.cancel(tokenRequestUrlPattern);
        }

//endregion

//region Miscellaneous

        /**
         * DBオブジェクト検索用のクエリオブジェクトを取得します。
         * @return {@link ABQuery} オブジェクト
         */
        public static ABQuery query() {
            return ABUser.query();
        }

        /**
         * ログイン中かどうかを取得します。
         * @return true: ログイン中
         */
        public static boolean isLoggedIn() {
            ABUser loggedInUser = AB.Session.getUser();
            String token = AB.Session.getToken();
            return loggedInUser != null && loggedInUser.getID().length() > 0 && token != null && token.length() > 0;
        }

        /**
         * 匿名ユーザでログイン中かどうかを取得します。
         * @return true: ログイン中
         */
        public static boolean isLoggedInAsAnonymous() {
            if (!isLoggedIn()) return false;

            Map<String, Object> authData = AB.Session.getUser().getAuthData();
            if (authData == null) return false;

            @SuppressWarnings("unchecked") Map<String, Object> anonymousAuthData = (Map<String, Object>)authData.get(ABAnonymousAuthenticationProvider.ID);
            if (anonymousAuthData == null) return false;

            //XXX: 匿名ログイン時のリクエストで投げる際の "id" は UUID で、そのレスポンスで返却される "_id" は objectID。
            //     user.authData にはリクエストで投げた際の authData をそのまま保存しているので、ログイン時に発行された objectID を後から特定できない。
            //     仕方がないので、とりあえず2015.3時点で authData には同時に複数の認証情報は持たない仕様なので、anonymousが見つかったら
            //     それでログインしていると判断することにしておく。
            /*String anonymousId = (String)anonymousAuthData.get("id");
            return anonymousId != null && anonymousId.equals(AB.Session.getUser().getID());*/
            return true;
        }

        /**
         * Twitter アカウントを利用してログイン中かを取得します。
         * @return true: ログイン中
         */
        public static boolean isLoggedInViaTwitter() {
            if (!isLoggedIn()) return false;

            Map<String, Object> authData = AB.Session.getUser().getAuthData();
            if (authData == null) return false;

            @SuppressWarnings("unchecked") Map<String, Object> twitterAuthData = (Map<String, Object>)authData.get(ABTwitterAuthenticationProvider.ID);
            if (twitterAuthData == null) return false;

            return true;
        }

        /**
         * Facebook アカウントを利用してログイン中かを取得します。
         * @return true: ログイン中
         */
        public static boolean isLoggedInViaFacebook() {
            if (!isLoggedIn()) return false;

            Map<String, Object> authData = AB.Session.getUser().getAuthData();
            if (authData == null) return false;

            @SuppressWarnings("unchecked") Map<String, Object> facebookAuthData = (Map<String, Object>)authData.get(ABFacebookAuthenticationProvider.ID);
            if (facebookAuthData == null) return false;

            return true;
        }

//endregion

//region Private methods

        private static void setPaginationInfo(ABResult<?> result, Map<String, Object> json) {
            int total    = json.containsKey("_total") ? (Integer)json.get("_total") : 0;
            int start    = json.containsKey("_start") ? (Integer)json.get("_start") : 0;
            int end      = json.containsKey("_end")   ? (Integer)json.get("_end")   : 0;
            boolean next = json.containsKey("_next") && (Boolean)json.get("_next");
            boolean prev = json.containsKey("_prev") && (Boolean)json.get("_prev");
            result.setTotal(total);
            result.setStart(start);
            result.setEnd(end);
            result.setNext(next);
            result.setPrevious(prev);
        }

        private static <T> void executeResultCallbackIfNeeded(ResultCallback<T> callback, ABResult<T> result, ABException e) {
            if (callback != null) {
                callback.internalDone(result, e);
            }
        }

        private static void validate(String method, Map<String, Object> params) throws ABException {
            switch (method) {
                case "signUp": {
                    ABUser user = (ABUser) params.get("user");
                    ABValidator.validate("user", user, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user]");
                    }});
                    ABValidator.validate("password", user.getPassword(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user.password]");
                    }});
                    String loginIdOrEmail = user.getLoginId() + user.getEmail();
                    ABValidator.validate("loginId or email", loginIdOrEmail, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user.loginId or user.email]");
                    }});
                    break;
                }
                case "logIn": {
                    ABUser user = (ABUser) params.get("user");
                    ABValidator.validate("user", user, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user]");
                    }});
                    ABValidator.validate("password", user.getPassword(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user.password]");
                    }});
                    String loginIdOrEmail = user.getLoginId() + user.getEmail();
                    ABValidator.validate("loginId or email", loginIdOrEmail, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user.loginId or user.email]");
                    }});
                    break;
                }
                case "logInAsAnonymous": {
                    String uuid = (String) params.get("uuid");
                    ABValidator.validate("uuid", uuid, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: uuid]");
                    }});
                    break;
                }
                case "logOut": {
                    ABUser user = (ABUser) params.get("user");
                    ABValidator.validate("user", user, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user]");
                    }});
                    ABValidator.validate("ID", user.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user.ID]");
                    }});
                    break;
                }
                case "verifyEmail": {
                    String email = (String) params.get("email");
                    ABValidator.validate("email", email, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: email]");
                    }});
                    ABValidator.validate("email", email, ABValidator.ValidationRule.EMAIL, new HashMap<String, Object>() {{
                        put("msg", "メールアドレスの形式に誤りがあります。");
                    }});
                    break;
                }
                case "resetPassword": {
                    String email = (String) params.get("email");
                    ABValidator.validate("email", email, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: email]");
                    }});
                    ABValidator.validate("email", email, ABValidator.ValidationRule.EMAIL, new HashMap<String, Object>() {{
                        put("msg", "メールアドレスの形式に誤りがあります。");
                    }});
                    break;
                }
                case "save": {
                    ABUser user = (ABUser) params.get("user");
                    ABValidator.validate("user", user, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user]");
                    }});
                    break;
                }
                case "delete": {
                    ABUser user = (ABUser) params.get("user");
                    ABValidator.validate("user", user, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user]");
                    }});
                    ABValidator.validate("ID", user.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user.ID]");
                    }});
                    break;
                }
                case "deleteWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
                case "fetch": {
                    ABUser user = (ABUser) params.get("user");
                    ABValidator.validate("user", user, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: user]");
                    }});
                    break;
                }
                case "findWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
            }
        }

        private static Map<String, Object> createAuthDataWithUUID(final String uuid) {
            final Map<String, Object> anonymousAuthData = createAnonymousAuthDataWithUUID(uuid);
            return (anonymousAuthData != null) ? new HashMap<String, Object>(){{ put(AB.JSON_AUTH_DATA_KEY, anonymousAuthData); }} : null;
        }
        private static Map<String, Object> createAnonymousAuthDataWithUUID(final String uuid) {
            return new HashMap<String, Object>(){{
                put(AB.AUTHENTICATION_PROVIDER_ANONYMOUS_KEY,
                        new HashMap<String, String>(){{ put(AB.JSON_AUTH_DATA_ANONYMOUS_ID_KEY, uuid); }});
            }};
        }

//endregion

    }

    /**
     * DB サービス。
     * <p></p>
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=70">アピアリーズドキュメント &raquo; JSONデータを管理する</a>
     * @see <a href="http://tutorial.appiaries.com/v1/tutorial/json/">アピアリーズドキュメント &raquo; JSONについて</a>
     */
    public static class DBService {

        private static final String TAG = DBService.class.getSimpleName();

//region Save

        /**
         * 同期モードでDBオブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> ABResult<T> saveSynchronously(final T object) throws ABException {
            return saveSynchronously(object, EnumSet.of(AB.DBObjectSaveOption.NONE));
        }

        /**
         * 同期モードでDBオブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.DBObjectSaveOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> ABResult<T> saveSynchronously(final T object, final AB.DBObjectSaveOption option) throws ABException {
            return saveSynchronously(object, EnumSet.of(option));
        }

        /**
         * 同期モードでDBオブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.DBObjectSaveOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> ABResult<T> saveSynchronously(final T object, final EnumSet<AB.DBObjectSaveOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("object", object);
            validate("save", params);

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(object);

            //未登録の場合
            if (object.isNew()) {

                //リクエストBODYの組み立て
                bodyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : object.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = object.outputDataFilter(key, val);
                    bodyMap.put(key, fixedVal);
                }
                body = Helper.ModelHelper.toJson(bodyMap);

                //APIの実行
                ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                        AB.baasDatastoreAPIURLWithFormat("/%s?get=true", object.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        body,
                        ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                );
                ABResult<T> ret = new ABResult<>();
                ret.setCode(restResult.getCode());
                ret.setExtra(restResult.getExtra());
                ret.setRawData(restResult.getRawData());
                T obj = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), restResult.getData(), false);
                ret.setData(obj);

                return ret;
            }

            //更新の場合
            if (object.isDirty()) {

                Map<String, Object> addedMap   = object.getAddedKeysAndValues();
                Map<String, Object> removedMap = object.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = object.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                                removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                                removedCount > 1 || //削除対象フィールドが複数の場合
                                updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : object.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        Object val = entry.getValue();
                        Object fixedVal = object.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    body = Helper.ModelHelper.toJson(bodyMap);

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPUT(
                            AB.baasDatastoreAPIURLWithFormat("/%s/%s?get=true", object.getCollectionID(), object.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    T obj = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), restResult.getData(), false);
                    ret.setData(obj);

                    return ret;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasDatastoreAPIURLWithFormat("/%s/%s/%s?get=true", object.getCollectionID(), object.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T deleted = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) object.clone(); //unsafe cast
                            copied.remove(field);
                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        Object fixedVal = object.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    body = Helper.ModelHelper.toJson(bodyMap);

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPATCH(
                            AB.baasDatastoreAPIURLWithFormat("/%s/%s?get=true", object.getCollectionID(), object.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T patched = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) object.clone(); //unsafe cast
                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            return ret;
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアへ保存します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void save(final T object) {
            save(object, null, EnumSet.of(AB.DBObjectSaveOption.NONE));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアへ保存します。
         * @param option {@link AB.DBObjectSaveOption} オプション
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void save(final T object, final AB.DBObjectSaveOption option) {
            save(object, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアへ保存します。
         * @param options {@link AB.DBObjectSaveOption} オプション群
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void save(final T object, final EnumSet<AB.DBObjectSaveOption> options) {
            save(object, null, options);
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアへ保存します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void save(final T object, final ResultCallback<T> callback) {
            save(object, callback, EnumSet.of(AB.DBObjectSaveOption.NONE));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアへ保存します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DBObjectSaveOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void save(final T object, final ResultCallback<T> callback, final AB.DBObjectSaveOption option) {
            save(object, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアへ保存します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DBObjectSaveOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void save(final T object, final ResultCallback<T> callback, final EnumSet<AB.DBObjectSaveOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("object", object);
                validate("save", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(object);

            //未登録の場合
            if (object.isNew()) {

                //リクエストBODYの組み立て
                bodyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : object.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = object.outputDataFilter(key, val);
                    bodyMap.put(key, fixedVal);
                }
                try {
                    body = Helper.ModelHelper.toJson(bodyMap);
                } catch (ABException e) {
                    executeResultCallbackIfNeeded(callback, null, e);
                    return;
                }

                //APIの実行
                ABRestClient.POST(
                        AB.baasDatastoreAPIURLWithFormat("/%s?get=true", object.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        body,
                        ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                        new ResultCallback<Map<String, Object>>() {
                            @Override
                            public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                if (e != null) {
                                    executeResultCallbackIfNeeded(callback, null, e);
                                    return;
                                }
                                ABResult<T> ret = new ABResult<>();
                                ret.setCode(restResult.getCode());
                                ret.setExtra(restResult.getExtra());
                                ret.setRawData(restResult.getRawData());
                                try {
                                    T obj = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), restResult.getData(), false);
                                    ret.setData(obj);
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } catch (ABException ie) {
                                    executeResultCallbackIfNeeded(callback, null, ie);
                                }
                            }
                        });
                return;
            }

            //更新の場合
            if (object.isDirty()) {

                Map<String, Object> addedMap   = object.getAddedKeysAndValues();
                Map<String, Object> removedMap = object.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = object.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                        removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                        removedCount > 1 || //削除対象フィールドが複数の場合
                        updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : object.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        if ("_id".equals(key)) { //FIXME: TEMPORARY : patch の場合 _id が含まれるとエラーになる
                            continue;            //FIXME: TEMPORARY
                        }                        //FIXME: TEMPORARY
                        Object val = entry.getValue();
                        Object fixedVal = object.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    try {
                        body = Helper.ModelHelper.toJson(bodyMap);
                    } catch (ABException e) {
                        executeResultCallbackIfNeeded(callback, null, e);
                        return;
                    }

                    //APIの実行
                    /* FIXME: TEMPORARY: コレクションの権限を"登録:ADMIN"に指定している場合、 PUT だと 403(Forbidden) が発生してしまうので暫定的に POST+proc=patch にする (2015.7.29 - ogawa)
                    ABRestClient.PUT(
                            AB.baasDatastoreAPIURLWithFormat("/%s/%s?get=true", object.getCollectionID(), object.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    */
                    ABRestClient.POST(
                            AB.baasDatastoreAPIURLWithFormat("/%s/%s?proc=patch&get=true", object.getCollectionID(), object.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    try {
                                        T obj = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), restResult.getData(), false);
                                        ret.setData(obj);
                                        executeResultCallbackIfNeeded(callback, ret, null);
                                    } catch (ABException ie) {
                                        executeResultCallbackIfNeeded(callback, null, ie);
                                    }
                                }
                            });
                    return;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasDatastoreAPIURLWithFormat("/%s/%s/%s?get=true", object.getCollectionID(), object.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABRestClient.DELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T deleted = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) object.clone(); //unsafe cast
                                            copied.remove(field);
                                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            });
                    return;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        Object fixedVal = object.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    try {
                        body = Helper.ModelHelper.toJson(bodyMap);
                    } catch (ABException e) {
                        executeResultCallbackIfNeeded(callback, null, e);
                        return;
                    }

                    //APIの実行
                    ABRestClient.PATCH(
                            AB.baasDatastoreAPIURLWithFormat("/%s/%s?get=true", object.getCollectionID(), object.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T patched = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) object.clone(); //unsafe cast
                                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            });
                    return;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            executeResultCallbackIfNeeded(callback, ret, null);
        }
//endregion

//region Save (Objects)

        /**
         * 同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> ABResult<List<T>> saveAllSynchronously(final List<T> objects) throws ABException {
            return saveAllSynchronously(objects, EnumSet.of(AB.DBObjectSaveOption.NONE));
        }

        /**
         * 同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.DBObjectSaveOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> ABResult<List<T>> saveAllSynchronously(final List<T> objects, final AB.DBObjectSaveOption option) throws ABException {
            return saveAllSynchronously(objects, EnumSet.of(option));
        }

        /**
         * 同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.DBObjectSaveOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> ABResult<List<T>> saveAllSynchronously(final List<T> objects, final EnumSet<AB.DBObjectSaveOption> options) throws ABException {
            ABResult<List<T>> ret = new ABResult<>();
            List<T> savedObjects = new ArrayList<>();
            Integer success = 0;
            for (T obj : objects) {
                ABResult<T> r = saveSynchronously(obj, options);
                if (r.getCode() >= 200 && r.getCode() <= 399) {
                    T saved = r.getData();
                    savedObjects.add(saved);
                    success++;
                    ret.setCode(r.getCode());
                }
            }
            ret.setTotal(success);
            ret.setData(savedObjects);
            return ret;
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects) {
            saveAll(objects, null, null, EnumSet.of(AB.DBObjectSaveOption.NONE));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.DBObjectSaveOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects, final AB.DBObjectSaveOption option) {
            saveAll(objects, null, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.DBObjectSaveOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects, final EnumSet<AB.DBObjectSaveOption> options) {
            saveAll(objects, null, null, options);
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects, final ResultCallback<List<T>> callback) {
            saveAll(objects, callback, null, EnumSet.of(AB.DBObjectSaveOption.NONE));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback) {
            saveAll(objects, callback, progressCallback, EnumSet.of(AB.DBObjectSaveOption.NONE));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DBObjectSaveOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects, final ResultCallback<List<T>> callback, final AB.DBObjectSaveOption option) {
            saveAll(objects, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DBObjectSaveOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects, final ResultCallback<List<T>> callback, final EnumSet<AB.DBObjectSaveOption> options) {
            saveAll(objects, callback, null, options);
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param option {@link AB.DBObjectSaveOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final AB.DBObjectSaveOption option) {
            saveAll(objects, callback, progressCallback, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアへ保存します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param options {@link AB.DBObjectSaveOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
         */
        public static <T extends ABDBObject> void saveAll(final List<T> objects, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final EnumSet<AB.DBObjectSaveOption> options) {
            new ABAsyncBatchExecutor(objects, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T obj = (T) target;
                    obj.save(new ResultCallback<T>() {
                        @Override
                        public void done(ABResult<T> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }
//endregion

//region Delete

        /**
         * 同期モードでDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> ABResult<Void> deleteSynchronously(final T object) throws ABException {
            return deleteSynchronously(object, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 同期モードでDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.DBObjectDeleteOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> ABResult<Void> deleteSynchronously(final T object, final AB.DBObjectDeleteOption option) throws ABException {
            return deleteSynchronously(object, EnumSet.of(option));
        }

        /**
         * 同期モードでDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> ABResult<Void> deleteSynchronously(final T object, final EnumSet<AB.DBObjectDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("object", object);
            validate("delete", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasDatastoreAPIURLWithFormat("/%s/%s", object.getCollectionID(), object.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから削除します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void delete(final T object) {
            delete(object, null, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから削除します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.DBObjectDeleteOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void delete(final T object, final AB.DBObjectDeleteOption option) {
            delete(object, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから削除します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void delete(final T object, final EnumSet<AB.DBObjectDeleteOption> options) {
            delete(object, null, options);
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから削除します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void delete(final T object, final ResultCallback<Void> callback) {
            delete(object, callback, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから削除します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DBObjectDeleteOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void delete(final T object, final ResultCallback<Void> callback, final AB.DBObjectDeleteOption option) {
            delete(object, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから削除します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void delete(final T object, final ResultCallback<Void> callback, final EnumSet<AB.DBObjectDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("object", object);
                validate("delete", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasDatastoreAPIURLWithFormat("/%s/%s", object.getCollectionID(), object.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }
//endregion

//region Delete (Objects)

        /**
         * 同期モードで複数のDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> ABResult<Void> deleteAllSynchronously(final List<T> objects) throws ABException {
            return deleteAllSynchronously(objects, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 同期モードで複数のDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.DBObjectDeleteOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> ABResult<Void> deleteAllSynchronously(final List<T> objects, final AB.DBObjectDeleteOption option) throws ABException {
            return deleteAllSynchronously(objects, EnumSet.of(option));
        }

        /**
         * 同期モードで複数のDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> ABResult<Void> deleteAllSynchronously(final List<T> objects, final EnumSet<AB.DBObjectDeleteOption> options) throws ABException {
            ABResult<Void> ret = new ABResult<>();
            Integer success = 0;
            for (T obj : objects) {
                ret = obj.deleteSynchronously(options);
                if (ret.getCode() >= 200 && ret.getCode() <= 399) {
                    success++;
                }
            }
            ret.setTotal(success);
            return ret;
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects) {
            deleteAll(objects, null, null, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.DBObjectDeleteOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects, final AB.DBObjectDeleteOption option) {
            deleteAll(objects, null, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects, final EnumSet<AB.DBObjectDeleteOption> options) {
            deleteAll(objects, null, null, options);
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects, final ResultCallback<Void> callback) {
            deleteAll(objects, callback, null, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects, final ResultCallback<Void> callback, final ProgressCallback progressCallback) {
            deleteAll(objects, callback, progressCallback, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DBObjectDeleteOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects, final ResultCallback<Void> callback, final AB.DBObjectDeleteOption option) {
            deleteAll(objects, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects, final ResultCallback<Void> callback, final EnumSet<AB.DBObjectDeleteOption> options) {
            deleteAll(objects, callback, null, options);
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param option {@link AB.DBObjectDeleteOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final AB.DBObjectDeleteOption option) {
            deleteAll(objects, callback, progressCallback, EnumSet.of(option));
        }

        /**
         * 非同期モードで複数のDBオブジェクトをデータストアから削除します。
         * @param objects {@link ABDBObject} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
         */
        public static <T extends ABDBObject> void deleteAll(final List<T> objects, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final EnumSet<AB.DBObjectDeleteOption> options) {
            new ABAsyncBatchExecutor(objects, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T obj = (T) target;
                    obj.delete(new ResultCallback<Void>() {
                        @Override
                        public void done(ABResult<Void> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }
//endregion

//region Delete (Query)

        /**
         * 同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @param option {@link AB.DBObjectDeleteOption} オプション
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final AB.DBObjectDeleteOption option) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(option));
        }

        /**
         * 同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.DBObjectDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("deleteWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasDatastoreAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         */
        public static void deleteWithQuery(final ABQuery query) {
            deleteWithQuery(query, null, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param option {@link AB.DBObjectDeleteOption} オプション
         */
        public static void deleteWithQuery(final ABQuery query, final AB.DBObjectDeleteOption option) {
            deleteWithQuery(query, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         */
        public static void deleteWithQuery(final ABQuery query, final EnumSet<AB.DBObjectDeleteOption> options) {
            deleteWithQuery(query, null, options);
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback) {
            deleteWithQuery(query, callback, EnumSet.of(AB.DBObjectDeleteOption.NONE));
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DBObjectDeleteOption} オプション
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final AB.DBObjectDeleteOption option) {
            deleteWithQuery(query, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DBObjectDeleteOption} オプション群
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final EnumSet<AB.DBObjectDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("deleteWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasDatastoreAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            try {
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (Exception ie) {
                                executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                            }
                        }
                    });
        }

//endregion

//region Fetch

        /**
         * 同期モードでDBオブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> ABResult<T> fetchSynchronously(final T object) throws ABException {
            return fetchSynchronously(object, EnumSet.of(AB.DBObjectFetchOption.NONE));
        }

        /**
         * 同期モードでDBオブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.DBObjectFetchOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> ABResult<T> fetchSynchronously(final T object, final AB.DBObjectFetchOption option) throws ABException {
            return fetchSynchronously(object, EnumSet.of(option));
        }

        /**
         * 同期モードでDBオブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.DBObjectFetchOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> ABResult<T> fetchSynchronously(final T object, final EnumSet<AB.DBObjectFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("object", object);
            validate("fetch", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasDatastoreAPIURLWithFormat("/%s/%s", object.getCollectionID(), object.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(object);
            T obj = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), restResult.getData(), false);
            ret.setData(obj);

            return ret;
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから取得します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> void fetch(final T object, final ResultCallback<T> callback) {
            fetch(object, callback, EnumSet.of(AB.DBObjectFetchOption.NONE));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから取得します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DBObjectFetchOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> void fetch(final T object, final ResultCallback<T> callback, final AB.DBObjectFetchOption option) {
            fetch(object, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでDBオブジェクトをデータストアから取得します。
         * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DBObjectFetchOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> void fetch(final T object, final ResultCallback<T> callback, final EnumSet<AB.DBObjectFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("object", object);
                validate("fetch", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasDatastoreAPIURLWithFormat("/%s/%s", object.getCollectionID(), object.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(object);
                                T obj = Helper.ModelHelper.toObject(clazz, object.getCollectionID(), restResult.getData(), false);
                                ret.setData(obj);
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Find (Query)

        /**
         * 同期モードで検索条件にマッチするDBオブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(AB.DBObjectFindOption.NONE));
        }

        /**
         * 同期モードで検索条件にマッチするDBオブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param option {@link AB.DBObjectFindOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final AB.DBObjectFindOption option) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(option));
        }

        /**
         * 同期モードで検索条件にマッチするDBオブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param options {@link AB.DBObjectFindOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABDBObject> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.DBObjectFindOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("findWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasDatastoreAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<List<T>> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query, ABDBObject.class);
            List<T> objects = Helper.ModelHelper.toObjects(clazz, query.getCollectionID(), restResult.getData(), false);
            ret.setData(objects);
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         */
        public static <T extends ABDBObject> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback) {
            findWithQuery(query, callback, EnumSet.of(AB.DBObjectFetchOption.NONE));
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DBObjectFindOption} オプション
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         */
        public static <T extends ABDBObject> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final AB.DBObjectFetchOption option) {
            findWithQuery(query, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードで検索条件にマッチするDBオブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DBObjectFindOption} オプション群
         * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
         */
        public static <T extends ABDBObject> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final EnumSet<AB.DBObjectFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("findWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasDatastoreAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<List<T>> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query, ABDBObject.class);
                                List<T> objects = Helper.ModelHelper.toObjects(clazz, query.getCollectionID(), restResult.getData(), false);
                                ret.setData(objects);
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Cancel

        /**
         * 実行中の API リクエストをキャンセルします。
         * @deprecated use {@link #cancelAll()} instead.
         */
        @Deprecated
        public static void cancel() {
            Pattern urlPattern = Pattern.compile("/dat/");
            ABRestClient.cancel(urlPattern);
        }

        // XXX: 個々のリクエストをキャンセルするケースはまずないので実装は見送る
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
        // * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
        // */
        //public static <T extends ABDBObject> void cancel(final T object) {
        //    cancel(object, EnumSet.of(AB.DBObjectCancelOption.NONE));
        //}
        //
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
        // * @param option {@link AB.DBObjectCancelOption} オプション
        // * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
        // */
        //public static <T extends ABDBObject> void cancel(final T object, final AB.DBObjectCancelOption option) {
        //    cancel(object, EnumSet.of(AB.DBObjectCancelOption.NONE));
        //}
        //
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param object {@link ABDBObject} オブジェクト (またはその派生オブジェクト)
        // * @param options {@link AB.DBObjectCancelOption} オプション群
        // * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
        // */
        //public static <T extends ABDBObject> void cancel(final T object, final EnumSet<AB.DBObjectCancelOption> options) {
        //    if (object == null || TextUtils.isEmpty(object.getCollectionID()) || TextUtils.isEmpty(object.getID())) return;
        //    String pattern = String.format("/dat/%s/%s/%s/%s", AB.Config.getDatastoreID(),
        //            AB.Config.getApplicationID(), object.getCollectionID(), object.getID());
        //    Pattern urlPattern = Pattern.compile(pattern);
        //    ABRestClient.cancel(urlPattern);
        //}

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         */
        public static void cancelAll() {
            cancelAll(EnumSet.of(AB.DBObjectCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param option {@link AB.DBObjectCancelOption} オプション
         */
        public static void cancelAll(final AB.DBObjectCancelOption option) {
            cancelAll(EnumSet.of(AB.DBObjectCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param options {@link AB.DBObjectCancelOption} オプション群
         */
        public static void cancelAll(final EnumSet<AB.DBObjectCancelOption> options) {
            String pattern = String.format("/dat/%s/%s", AB.Config.getDatastoreID(), AB.Config.getApplicationID());
            Pattern urlPattern = Pattern.compile(pattern);
            ABRestClient.cancel(urlPattern);
        }

//endregion

//region Miscellaneous

        /**
         * DBオブジェクト検索用のクエリオブジェクトを取得します。
         * @return {@link ABQuery} オブジェクト
         */
        public static ABQuery query() {
            return ABDBObject.query();
        }

//endregion

//region Private methods

        private static void setPaginationInfo(ABResult<?> result, Map<String, Object> json) {
            int total    = json.containsKey("_total") ? (Integer)json.get("_total") : 0;
            int start    = json.containsKey("_start") ? (Integer)json.get("_start") : 0;
            int end      = json.containsKey("_end")   ? (Integer)json.get("_end")   : 0;
            boolean next = json.containsKey("_next") && (Boolean)json.get("_next");
            boolean prev = json.containsKey("_prev") && (Boolean)json.get("_prev");
            result.setTotal(total);
            result.setStart(start);
            result.setEnd(end);
            result.setNext(next);
            result.setPrevious(prev);
        }

        private static <T> void executeResultCallbackIfNeeded(ResultCallback<T> callback, ABResult<T> result, ABException e) {
            if (callback != null) {
                callback.internalDone(result, e);
            }
        }

        private static void validate(String method, Map<String, Object> params) throws ABException {
            switch (method) {
                case "save": {
                    ABDBObject object = (ABDBObject) params.get("object");
                    ABValidator.validate("object", object, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: object]");
                    }});
                    break;
                }
                case "delete": {
                    ABDBObject object = (ABDBObject) params.get("object");
                    ABValidator.validate("object", object, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: object]");
                    }});
                    ABValidator.validate("ID", object.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: object.ID]");
                    }});
                    break;
                }
                case "deleteWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
                case "fetch": {
                    ABDBObject object = (ABDBObject) params.get("object");
                    ABValidator.validate("object", object, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: object]");
                    }});
                    break;
                }
                case "findWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
            }
        }

//endregion

    }

    /**
     * ファイル・サービス。
     * <p></p>
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=80">アピアリーズドキュメント &raquo; ファイルを管理する</a>
     */
    public static class FileService {

        private static final String TAG = FileService.class.getSimpleName();

//region Save

        /**
         * 同期モードでファイル・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> ABResult<T> saveSynchronously(final T file) throws ABException {
            return saveSynchronously(file, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 同期モードでファイル・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.FileSaveOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> ABResult<T> saveSynchronously(final T file, final AB.FileSaveOption option) throws ABException {
            return saveSynchronously(file, EnumSet.of(option));
        }

        /**
         * 同期モードでファイル・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.FileSaveOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> ABResult<T> saveSynchronously(final T file, final EnumSet<AB.FileSaveOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("file", file);
            validate("save", params);

            //もし contentType が指定されていなければ、ついでなのでファイル名から推測してあげる
            if (file.getContentType() == null && file.getName() != null) {
                file.setContentType(getMimeTypeFromFilename(file.getName()));
            }

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(file);

            //未登録の場合
            if (file.isNew()) {

                //リクエストBODYの組み立て
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                ContentType mimeType = ContentType.create("text/plain", "UTF-8");
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.setCharset(Charset.forName("UTF-8"));
                builder.setBoundary(ABRestClient.BOUNDARY);
                for (Map.Entry<String, Object> entry : file.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    if (ABFile.Field.DATA.getKey().equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = file.outputDataFilter(key, val);
                    builder.addTextBody(key, (String)fixedVal); //FIXME: 数値型も文字列になってしまう
                }
                builder.addBinaryBody(ABFile.Field.DATA.getKey(), file.getData());
                HttpEntity entity = builder.build();

                //APIの実行
                ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                        AB.baasFileAPIURLWithFormat("/%s?get=true", file.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        entity,
                        ABRestClient.getDefaultMultipartHeadersWithStoreTokenIfPossible()
                );
                ABResult<T> ret = new ABResult<>();
                ret.setCode(restResult.getCode());
                ret.setExtra(restResult.getExtra());
                ret.setRawData(restResult.getRawData());
                T obj = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), restResult.getData(), false);
                ret.setData(obj);

                return ret;
            }

            //更新の場合
            if (file.isDirty()) {

                Map<String, Object> addedMap   = file.getAddedKeysAndValues();
                Map<String, Object> removedMap = file.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = file.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                        removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                        removedCount > 1 || //削除対象フィールドが複数の場合
                        updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    ContentType mimeType = ContentType.create("text/plain", "UTF-8");
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.setCharset(Charset.forName("UTF-8"));
                    builder.setBoundary(ABRestClient.BOUNDARY);
                    for (Map.Entry<String, Object> entry : file.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        if (ABFile.Field.DATA.getKey().equals(key)) {
                            continue;
                        }
                        Object val = entry.getValue();
                        Object fixedVal = file.outputDataFilter(key, val);
                        builder.addTextBody(key, (String)fixedVal); //FIXME: 数値型も文字列になってしまう
                    }
                    builder.addBinaryBody(ABFile.Field.DATA.getKey(), file.getData());
                    HttpEntity entity = builder.build();

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPUT(
                            AB.baasFileAPIURLWithFormat("/%s/%s?get=true", file.getCollectionID(), file.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            entity,
                            ABRestClient.getDefaultMultipartHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    T obj = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), restResult.getData(), false);
                    ret.setData(obj);

                    return ret;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasFileAPIURLWithFormat("/%s/%s/%s?get=true", file.getCollectionID(), file.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T deleted = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) file.clone(); //unsafe cast
                            copied.remove(field);
                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    ContentType mimeType = ContentType.create("text/plain", "UTF-8");
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.setCharset(Charset.forName("UTF-8"));
                    builder.setBoundary(ABRestClient.BOUNDARY);
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        if (ABFile.Field.DATA.getKey().equals(key)) {
                            continue;
                        }
                        Object val = entry.getValue();
                        Object fixedVal = file.outputDataFilter(key, val);
                        builder.addTextBody(key, (String)fixedVal); //FIXME: 数値型も文字列になってしまう
                    }
                    builder.addBinaryBody(ABFile.Field.DATA.getKey(), file.getData());
                    HttpEntity entity = builder.build();

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPATCH(
                            AB.baasFileAPIURLWithFormat("/%s/%s?get=true", file.getCollectionID(), file.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            entity,
                            ABRestClient.getDefaultMultipartHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T patched = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) file.clone(); //unsafe cast
                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            return ret;
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file) {
            save(file, null, null, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.FileSaveOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file, final AB.FileSaveOption option) {
            save(file, null, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.FileSaveOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file, final EnumSet<AB.FileSaveOption> options) {
            save(file, null, null, options);
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file, final ResultCallback<T> callback) {
            save(file, callback, null, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file, final ResultCallback<T> callback, final ProgressCallback progressCallback) {
            save(file, callback, progressCallback, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.FileSaveOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file, final ResultCallback<T> callback, final AB.FileSaveOption option) {
            save(file, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.FileSaveOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file, final ResultCallback<T> callback, final EnumSet<AB.FileSaveOption> options) {
            save(file, callback, null, options);
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param option {@link AB.FileSaveOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file, final ResultCallback<T> callback, final ProgressCallback progressCallback, final AB.FileSaveOption option) {
            save(file, callback, progressCallback, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param options {@link AB.FileSaveOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void save(final T file, final ResultCallback<T> callback, final ProgressCallback progressCallback, final EnumSet<AB.FileSaveOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("file", file);
                validate("save", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //もし contentType が指定されていなければ、ついでなのでファイル名から推測してあげる
            if (file.getContentType() == null && file.getName() != null) {
                file.setContentType(getMimeTypeFromFilename(file.getName()));
            }

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(file);

            //未登録の場合
            if (file.isNew()) {

                //リクエストBODYの組み立て
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                ContentType mimeType = ContentType.create("text/plain", "UTF-8");
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.setCharset(Charset.forName("UTF-8"));
                builder.setBoundary(ABRestClient.BOUNDARY);
                for (Map.Entry<String, Object> entry : file.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    if (ABFile.Field.DATA.getKey().equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = file.outputDataFilter(key, val);
                    builder.addTextBody(key, (String)fixedVal); //FIXME: 数値型も文字列になってしまう
                }
                builder.addBinaryBody(ABFile.Field.DATA.getKey(), file.getData());
                HttpEntity entity = builder.build();

                //APIの実行
                ABRestClient.POST(
                        AB.baasFileAPIURLWithFormat("/%s?get=true", file.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        entity,
                        ABRestClient.getDefaultMultipartHeadersWithStoreTokenIfPossible(),
                        new ResultCallback<Map<String, Object>>() {
                            @Override
                            public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                if (e != null) {
                                    executeResultCallbackIfNeeded(callback, null, e);
                                    return;
                                }
                                ABResult<T> ret = new ABResult<>();
                                ret.setCode(restResult.getCode());
                                ret.setExtra(restResult.getExtra());
                                ret.setRawData(restResult.getRawData());
                                try {
                                    T obj = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), restResult.getData(), false);
                                    ret.setData(obj);
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } catch (ABException ie) {
                                    executeResultCallbackIfNeeded(callback, null, ie);
                                }
                            }
                        }, progressCallback);
                return;
            }

            //更新の場合
            if (file.isDirty()) {

                Map<String, Object> addedMap   = file.getAddedKeysAndValues();
                Map<String, Object> removedMap = file.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = file.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                        removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                        removedCount > 1 || //削除対象フィールドが複数の場合
                        updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    ContentType mimeType = ContentType.create("text/plain", "UTF-8");
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.setCharset(Charset.forName("UTF-8"));
                    builder.setBoundary(ABRestClient.BOUNDARY);
                    for (Map.Entry<String, Object> entry : file.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        if (ABFile.Field.DATA.getKey().equals(key)) {
                            continue;
                        }
                        if ("_id".equals(key)) { //FIXME: TEMPORARY : patch の場合 _id が含まれるとエラーになる
                            continue;            //FIXME: TEMPORARY
                        }                        //FIXME: TEMPORARY
                        Object val = entry.getValue();
                        Object fixedVal = file.outputDataFilter(key, val);
                        builder.addTextBody(key, (String)fixedVal); //FIXME: 数値型も文字列になってしまう
                    }
                    builder.addBinaryBody(ABFile.Field.DATA.getKey(), file.getData());
                    HttpEntity entity = builder.build();

                    //APIの実行
                    /* FIXME: TEMPORARY: コレクションの権限を"登録:ADMIN"に指定している場合、 PUT だと 403(Forbidden) が発生してしまうので暫定的に POST+proc=patch にする (2015.7.29 - ogawa)
                    ABRestClient.PUT(
                            AB.baasFileAPIURLWithFormat("/%s/%s?get=true", file.getCollectionID(), file.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    */
                    ABRestClient.POST(
                            AB.baasFileAPIURLWithFormat("/%s/%s?proc=patch&get=true", file.getCollectionID(), file.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            entity,
                            ABRestClient.getDefaultMultipartHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    try {
                                        T obj = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), restResult.getData(), false);
                                        ret.setData(obj);
                                        executeResultCallbackIfNeeded(callback, ret, null);
                                    } catch (ABException ie) {
                                        executeResultCallbackIfNeeded(callback, null, ie);
                                    }
                                }
                            }, progressCallback);
                    return;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasFileAPIURLWithFormat("/%s/%s/%s?get=true", file.getCollectionID(), file.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABRestClient.DELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T deleted = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) file.clone(); //unsafe cast
                                            copied.remove(field);
                                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            }, progressCallback);
                    return;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    ContentType mimeType = ContentType.create("text/plain", "UTF-8");
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.setCharset(Charset.forName("UTF-8"));
                    builder.setBoundary(ABRestClient.BOUNDARY);
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        if (ABFile.Field.DATA.getKey().equals(key)) {
                            continue;
                        }
                        Object val = entry.getValue();
                        Object fixedVal = file.outputDataFilter(key, val);
                        builder.addTextBody(key, (String)fixedVal); //FIXME: 数値型も文字列になってしまう
                    }
                    builder.addBinaryBody(ABFile.Field.DATA.getKey(), file.getData());
                    HttpEntity entity = builder.build();

                    //APIの実行
                    ABRestClient.PATCH(
                            AB.baasFileAPIURLWithFormat("/%s/%s?get=true", file.getCollectionID(), file.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            entity,
                            ABRestClient.getDefaultMultipartHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T patched = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) file.clone(); //unsafe cast
                                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            }, progressCallback);
                    return;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            executeResultCallbackIfNeeded(callback, ret, null);
        }

//endregion

//region Save (Objects)

        /**
         * 同期モードで複数のファイル・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> ABResult<List<T>> saveAllSynchronously(final List<T> files) throws ABException {
            return saveAllSynchronously(files, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 同期モードで複数のファイル・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.FileSaveOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> ABResult<List<T>> saveAllSynchronously(final List<T> files, final AB.FileSaveOption option) throws ABException {
            return saveAllSynchronously(files, EnumSet.of(option));
        }

        /**
         * 同期モードで複数のファイル・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.FileSaveOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> ABResult<List<T>> saveAllSynchronously(final List<T> files, final EnumSet<AB.FileSaveOption> options) throws ABException {
            ABResult<List<T>> ret = new ABResult<>();
            List<T> savedFiles = new ArrayList<>();
            Integer success = 0;
            for (T file : files) {
                ABResult<T> r = file.saveSynchronously();
                if (r.getCode() >= 200 && r.getCode() <= 399) {
                    T saved = r.getData();
                    savedFiles.add(saved);
                    success++;
                    ret.setCode(r.getCode());
                }
            }
            ret.setTotal(success);
            ret.setData(savedFiles);
            return ret;
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files) {
            saveAll(files, null, null, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.FileSaveOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files, final AB.FileSaveOption option) {
            saveAll(files, null, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.FileSaveOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files, final EnumSet<AB.FileSaveOption> options) {
            saveAll(files, null, null, options);
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files, final ResultCallback<List<T>> callback) {
            saveAll(files, callback, null, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback) {
            saveAll(files, callback, progressCallback, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.FileSaveOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files, final ResultCallback<List<T>> callback, final AB.FileSaveOption option) {
            saveAll(files, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.FileSaveOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files, final ResultCallback<List<T>> callback, final EnumSet<AB.FileSaveOption> options) {
            saveAll(files, callback, null, EnumSet.of(AB.FileSaveOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param option {@link AB.FileSaveOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final AB.FileSaveOption option) {
            saveAll(files, callback, progressCallback, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param options {@link AB.FileSaveOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
         * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
         */
        public static <T extends ABFile> void saveAll(final List<T> files, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final EnumSet<AB.FileSaveOption> options) {
            new ABAsyncBatchExecutor(files, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T file = (T) target;
                    file.save(new ResultCallback<T>() {
                        @Override
                        public void done(ABResult<T> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }

//endregion

//region Delete

        /**
         * 同期モードでファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> ABResult<Void> deleteSynchronously(final T file) throws ABException {
            return deleteSynchronously(file, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 同期モードでファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.FileDeleteOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> ABResult<Void> deleteSynchronously(final T file, final AB.FileDeleteOption option) throws ABException {
            return deleteSynchronously(file, EnumSet.of(option));
        }

        /**
         * 同期モードでファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.FileDeleteOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> ABResult<Void> deleteSynchronously(final T file, final EnumSet<AB.FileDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("file", file);
            validate("delete", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasFileAPIURLWithFormat("/%s/%s", file.getCollectionID(), file.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void delete(final T file) {
            delete(file, null, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.FileDeleteOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void delete(final T file, final AB.FileDeleteOption option) {
            delete(file, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.FileDeleteOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void delete(final T file, final EnumSet<AB.FileDeleteOption> options) {
            delete(file, null, options);
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void delete(final T file, final ResultCallback<Void> callback) {
            delete(file, callback, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.FileDeleteOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void delete(final T file, final ResultCallback<Void> callback, final AB.FileDeleteOption option) {
            delete(file, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.FileDeleteOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void delete(final T file, final ResultCallback<Void> callback, final EnumSet<AB.FileDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("file", file);
                validate("delete", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasFileAPIURLWithFormat("/%s/%s", file.getCollectionID(), file.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

//endregion

//region Delete (Objects)

        /**
         * 同期モードで複数のファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> ABResult<Void> deleteAllSynchronously(final List<T> files) throws ABException {
            return deleteAllSynchronously(files, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 同期モードで複数のファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.FileDeleteOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> ABResult<Void> deleteAllSynchronously(final List<T> files, final AB.FileDeleteOption option) throws ABException {
            return deleteAllSynchronously(files, EnumSet.of(option));
        }

        /**
         * 同期モードで複数のファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.FileDeleteOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> ABResult<Void> deleteAllSynchronously(final List<T> files, final EnumSet<AB.FileDeleteOption> options) throws ABException {
            ABResult<Void> ret = new ABResult<>();
            Integer success = 0;
            for (T file : files) {
                ret = file.deleteSynchronously(options);
                if (ret.getCode() >= 200 && ret.getCode() <= 399) {
                    success++;
                }
            }
            ret.setTotal(success);
            return ret;
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files) {
            deleteAll(files, null, null, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param option {@link AB.FileDeleteOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files, final AB.FileDeleteOption option) {
            deleteAll(files, null, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param options {@link AB.FileDeleteOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files, final EnumSet<AB.FileDeleteOption> options) {
            deleteAll(files, null, null, options);
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files, final ResultCallback<Void> callback) {
            deleteAll(files, callback, null, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files, final ResultCallback<Void> callback, final ProgressCallback progressCallback) {
            deleteAll(files, callback, progressCallback, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.FileDeleteOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files, final ResultCallback<Void> callback, final AB.FileDeleteOption option) {
            deleteAll(files, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.FileDeleteOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files, final ResultCallback<Void> callback, final EnumSet<AB.FileDeleteOption> options) {
            deleteAll(files, callback, null, options);
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param option {@link AB.FileDeleteOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final AB.FileDeleteOption option) {
            deleteAll(files, callback, progressCallback, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから削除します。
         * @param files {@link ABFile} オブジェクト群 (またはその派生オブジェクト群)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param options {@link AB.FileDeleteOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
         */
        public static <T extends ABFile> void deleteAll(final List<T> files, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final EnumSet<AB.FileDeleteOption> options) {
            new ABAsyncBatchExecutor(files, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T file = (T) target;
                    file.delete(new ResultCallback<Void>() {
                        @Override
                        public void done(ABResult<Void> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }

//endregion

//region Delete (Query)

        /**
         * 同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @param option {@link AB.FileDeleteOption} オプション
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final AB.FileDeleteOption option) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(option));
        }

        /**
         * 同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param query クエリ・オブジェクト
         * @param options {@link AB.FileDeleteOption} オプション群
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.FileDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("deleteWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasFileAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         */
        public static void deleteWithQuery(final ABQuery query) {
            deleteWithQuery(query, null, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param option {@link AB.FileDeleteOption} オプション
         */
        public static void deleteWithQuery(final ABQuery query, final AB.FileDeleteOption option) {
            deleteWithQuery(query, null, EnumSet.of(option));
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param options {@link AB.FileDeleteOption} オプション群
         */
        public static void deleteWithQuery(final ABQuery query, final EnumSet<AB.FileDeleteOption> options) {
            deleteWithQuery(query, null, options);
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback) {
            deleteWithQuery(query, callback, EnumSet.of(AB.FileDeleteOption.NONE));
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.FileDeleteOption} オプション
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final AB.FileDeleteOption option) {
            deleteWithQuery(query, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから削除します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.FileDeleteOption} オプション群
         */
        public static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final EnumSet<AB.FileDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("deleteWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasFileAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            try {
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (Exception ie) {
                                executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                            }
                        }
                    });
        }

//endregion

//region Fetch

        /**
         * 同期モードでファイル・オブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
         */
        public static <T extends ABFile> ABResult<T> fetchSynchronously(final T file) throws ABException {
            return fetchSynchronously(file, EnumSet.of(AB.FileFetchOption.NONE));
        }

        /**
         * 同期モードでファイル・オブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.FileFetchOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
         */
        public static <T extends ABFile> ABResult<T> fetchSynchronously(final T file, final AB.FileFetchOption option) throws ABException {
            return fetchSynchronously(file, EnumSet.of(option));
        }

        /**
         * 同期モードでファイル・オブジェクトをデータストアから取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.FileFetchOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
         */
        public static <T extends ABFile> ABResult<T> fetchSynchronously(final T file, final EnumSet<AB.FileFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("file", file);
            validate("fetch", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasFileAPIURLWithFormat("/%s/%s", file.getCollectionID(), file.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(file);
            T obj = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), restResult.getData(), false);
            ret.setData(obj);

            return ret;
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから取得します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
         */
        public static <T extends ABFile> void fetch(final T file, final ResultCallback<T> callback) {
            fetch(file, callback, EnumSet.of(AB.FileFetchOption.NONE));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから取得します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.FileFetchOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
         */
        public static <T extends ABFile> void fetch(final T file, final ResultCallback<T> callback, final AB.FileFetchOption option) {
            fetch(file, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル・オブジェクトをデータストアから取得します。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.FileFetchOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
         */
        public static <T extends ABFile> void fetch(final T file, final ResultCallback<T> callback, final EnumSet<AB.FileFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("file", file);
                validate("fetch", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasFileAPIURLWithFormat("/%s/%s", file.getCollectionID(), file.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(file);
                                T obj = Helper.ModelHelper.toObject(clazz, file.getCollectionID(), restResult.getData(), false);
                                ret.setData(obj);
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Find (Query)

        /**
         * 同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABFile> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(AB.FileFindOption.NONE));
        }

        /**
         * 同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param option {@link AB.FileFindOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABFile> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final AB.FileFindOption option) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(option));
        }

        /**
         * 同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param options {@link AB.FileFindOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
         */
        public static <T extends ABFile> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.FileFindOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("findWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasFileAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<List<T>> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query, ABFile.class);
            List<T> objects = Helper.ModelHelper.toObjects(clazz, query.getCollectionID(), restResult.getData(), false);
            ret.setData(objects);
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         */
        public static <T extends ABFile> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback) {
            findWithQuery(query, callback, EnumSet.of(AB.FileFindOption.NONE));
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.FileFindOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         */
        public static <T extends ABFile> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final AB.FileFindOption option) {
            findWithQuery(query, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードで検索条件にマッチするファイル・オブジェクトをデータストアから取得します。
         * @param query クエリ・オブジェクト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.FileFindOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         */
        public static <T extends ABFile> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final EnumSet<AB.FileFindOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("findWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasFileAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<List<T>> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query, ABFile.class);
                                List<T> objects = Helper.ModelHelper.toObjects(clazz, query.getCollectionID(), restResult.getData(), false);
                                ret.setData(objects);
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Download

        /**
         * 同期モードでファイル実体（バイナリ）をダウンロードします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> ABResult<Void> downloadSynchronously(final T file) throws ABException {
            return downloadSynchronously(file, EnumSet.of(AB.FileDownloadOption.NONE));
        }

        /**
         * 同期モードでファイル実体（バイナリ）をダウンロードします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.FileDownloadOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> ABResult<Void> downloadSynchronously(final T file, final AB.FileDownloadOption option) throws ABException {
            return downloadSynchronously(file, EnumSet.of(option));
        }

        /**
         * 同期モードでファイル実体（バイナリ）をダウンロードします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.FileDownloadOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> ABResult<Void> downloadSynchronously(final T file, final EnumSet<AB.FileDownloadOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("file", file);
            validate("download", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasFileAPIURLWithFormat("/%s/%s/_bin", file.getCollectionID(), file.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());

            return ret;
        }

        /**
         * 非同期モードでファイル実体（バイナリ）をダウンロードします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> void download(final T file, final ResultCallback<Void> callback) {
            download(file, callback, null, EnumSet.of(AB.FileDownloadOption.NONE));
        }

        /**
         * 非同期モードでファイル実体（バイナリ）をダウンロードします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.FileDownloadOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> void download(final T file, final ResultCallback<Void> callback, final AB.FileDownloadOption option) {
            download(file, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル実体（バイナリ）をダウンロードします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.FileDownloadOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> void download(final T file, final ResultCallback<Void> callback, final EnumSet<AB.FileDownloadOption> options) {
            download(file, callback, null, options);
        }

        /**
         * 非同期モードでファイル実体（バイナリ）をダウンロードします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> void download(final T file, final ResultCallback<Void> callback, final ProgressCallback progressCallback) {
            download(file, callback, null, EnumSet.of(AB.FileDownloadOption.NONE));
        }

        /**
         * 非同期モードでファイル実体（バイナリ）をダウンロードします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param option {@link AB.FileDownloadOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> void download(final T file, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final AB.FileDownloadOption option) {
            download(file, callback, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでファイル実体（バイナリ）をダウンロードします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param progressCallback 進捗取得コールバック
         * @param options {@link AB.FileDownloadOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
         */
        public static <T extends ABFile> void download(final T file, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final EnumSet<AB.FileDownloadOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("file", file);
                validate("download", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasFileAPIURLWithFormat("/%s/%s/_bin", file.getCollectionID(), file.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    }, progressCallback);
        }

//endregion

//region Cancel

        /**
         * 実行中の API リクエストをキャンセルします。
         * @deprecated use {@link #cancel(ABFile)} instead.
         */
        @Deprecated
        public static void cancel() {
            Pattern urlPattern = Pattern.compile("/bin/");
            ABRestClient.cancel(urlPattern);
        }

        /**
         * 実行中の API リクエストをキャンセルします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         */
        public static <T extends ABFile> void cancel(final T file) {
            cancel(file, EnumSet.of(AB.FileCancelOption.NONE));
        }

        /**
         * 実行中の API リクエストをキャンセルします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.FileCancelOption} オプション
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         */
        public static <T extends ABFile> void cancel(final T file, final AB.FileCancelOption option) {
            cancel(file, EnumSet.of(AB.FileCancelOption.NONE));
        }

        /**
         * 実行中の API リクエストをキャンセルします。
         * @param file {@link ABFile} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.FileCancelOption} オプション群
         * @param <T> {@link ABFile} クラス (またはその派生クラス)
         */
        public static <T extends ABFile> void cancel(final T file, final EnumSet<AB.FileCancelOption> options) {
            if (file == null || TextUtils.isEmpty(file.getCollectionID()) || TextUtils.isEmpty(file.getID())) return;
            String pattern = String.format("/bin/%s/%s/%s/%s", AB.Config.getDatastoreID(),
                    AB.Config.getApplicationID(), file.getCollectionID(), file.getID());
            Pattern urlPattern = Pattern.compile(pattern);
            ABRestClient.cancel(urlPattern);
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         */
        public static void cancelAll() {
            cancelAll(EnumSet.of(AB.FileCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param option {@link AB.FileCancelOption} オプション
         */
        public static void cancelAll(final AB.FileCancelOption option) {
            cancelAll(EnumSet.of(AB.FileCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param options {@link AB.FileCancelOption} オプション群
         */
        public static void cancelAll(final EnumSet<AB.FileCancelOption> options) {
            String pattern = String.format("/bin/%s/%s", AB.Config.getDatastoreID(), AB.Config.getApplicationID());
            Pattern urlPattern = Pattern.compile(pattern);
            ABRestClient.cancel(urlPattern);
        }

//endregion

//region Miscellaneous

        /**
         * ファイル・オブジェクト検索用のクエリオブジェクトを取得します。
         * @return {@link ABQuery} オブジェクト
         */
        public static ABQuery query() {
            return ABFile.query();
        }

//endregion

//region Private methods

        static String getMimeTypeFromFilename(String filename) {
            if (filename == null) return null;
            int ch = filename.lastIndexOf('.');
            if ((ch >= 0)) {
                String ext = filename.substring(ch + 1).toLowerCase();
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            } else {
                return null;
            }
        }

        static byte[] getFileData(String filePath) {
            byte[] data = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FileInputStream in = null;
            try {
                in = new FileInputStream(filePath);
                byte[] buff = new byte[1024];
                while (in.read(buff) > 0) {
                    out.write(buff);
                }
                data = out.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return data;
        }

        static byte[] getFileData(AssetFileDescriptor fd) {
            byte[] data = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FileInputStream in = null;
            try {
                in = fd.createInputStream();
                byte[] buff = new byte[1024];
                while (in.read(buff) > 0) {
                    out.write(buff);
                }
                data = out.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return data;
        }

        private static void setPaginationInfo(ABResult<?> result, Map<String, Object> json) {
            int total    = json.containsKey("_total") ? (Integer)json.get("_total") : 0;
            int start    = json.containsKey("_start") ? (Integer)json.get("_start") : 0;
            int end      = json.containsKey("_end")   ? (Integer)json.get("_end")   : 0;
            boolean next = json.containsKey("_next") && (Boolean)json.get("_next");
            boolean prev = json.containsKey("_prev") && (Boolean)json.get("_prev");
            result.setTotal(total);
            result.setStart(start);
            result.setEnd(end);
            result.setNext(next);
            result.setPrevious(prev);
        }

        private static <T> void executeResultCallbackIfNeeded(ResultCallback<T> callback, ABResult<T> result, ABException e) {
            if (callback != null) {
                callback.internalDone(result, e);
            }
        }

        private static void executeProgressCallbackIfNeeded(ProgressCallback progressCallback, int progress) {
            float p = (float)progress / 100;
            if (p <= 1.0 && progressCallback != null) {
                progressCallback.internalUpdateProgress(p);
            }
        }

        private static void validate(String method, Map<String, Object> params) throws ABException {
            switch (method) {
                case "save": {
                    ABFile file = (ABFile) params.get("file");
                    ABValidator.validate("file", file, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: file]");
                    }});
                    break;
                }
                case "delete": {
                    ABFile file = (ABFile) params.get("file");
                    ABValidator.validate("file", file, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: file]");
                    }});
                    ABValidator.validate("ID", file.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: file.ID]");
                    }});
                    break;
                }
                case "deleteWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
                case "fetch": {
                    ABFile file = (ABFile) params.get("file");
                    ABValidator.validate("file", file, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: file]");
                    }});
                    break;
                }
                case "findWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
                case "download": {
                    ABFile file = (ABFile) params.get("file");
                    ABValidator.validate("file", file, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: file]");
                    }});
                    ABValidator.validate("ID", file.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: file.ID]");
                    }});
                    break;
                }
            }
        }

//endregion

    }

    /**
     * シーケンス・サービス。
     * <p></p>
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=90">アピアリーズドキュメント » シーケンス値を管理する</a>
     */
    public static class SequenceService {

        private static final String TAG = SequenceService.class.getSimpleName();

//region Get Current Value

        /**
         * 同期モードでシーケンスの現在値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> long getCurrentValueSynchronously(final T sequence) throws ABException {
            return getCurrentValueSynchronously(sequence, EnumSet.of(AB.SequenceFetchOption.NONE));
        }

        /**
         * 同期モードでシーケンスの現在値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceFetchOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> long getCurrentValueSynchronously(final T sequence, final AB.SequenceFetchOption option) throws ABException {
            return getCurrentValueSynchronously(sequence, EnumSet.of(AB.SequenceFetchOption.NONE));
        }

        /**
         * 同期モードでシーケンスの現在値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceFetchOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> long getCurrentValueSynchronously(final T sequence, final EnumSet<AB.SequenceFetchOption> options) throws ABException {
            ABResult<T> ret = fetchSynchronously(sequence, options);
            T seq = ret.getData();
            return (seq != null) ? seq.getValue() : 0;
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> void getCurrentValue(final T sequence, final SequenceCallback callback) {
            getCurrentValue(sequence, callback, EnumSet.of(AB.SequenceFetchOption.NONE));
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param option {@link AB.SequenceFetchOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> void getCurrentValue(final T sequence, final SequenceCallback callback, final AB.SequenceFetchOption option) {
            getCurrentValue(sequence, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param options {@link AB.SequenceFetchOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> void getCurrentValue(final T sequence, final SequenceCallback callback, final EnumSet<AB.SequenceFetchOption> options) {
            fetch(sequence, new ResultCallback<T>() {
                @Override
                public void done(ABResult<T> result, ABException e) {
                    if (e != null) {
                        executeSequenceCallbackIfNeeded(callback, 0, e);
                        return;
                    }
                    T seq = result.getData();
                    executeSequenceCallbackIfNeeded(callback, seq.getValue(), null);
                }
            });
        }

//endregion

//region Increment

        /**
         * 同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> incrementSynchronously(final T sequence) throws ABException {
            return addSynchronously(sequence, 1, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceFetchOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> incrementSynchronously(final T sequence, AB.SequenceAddOption option) throws ABException {
            return addSynchronously(sequence, 1, EnumSet.of(option));
        }

        /**
         * 同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceFetchOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> incrementSynchronously(final T sequence, EnumSet<AB.SequenceAddOption> options) throws ABException {
            return addSynchronously(sequence, 1, options);
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void increment(final T sequence) {
            add(sequence, 1, null, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceFetchOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void increment(final T sequence, final AB.SequenceAddOption option) {
            add(sequence, 1, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceFetchOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void increment(final T sequence, final EnumSet<AB.SequenceAddOption> options) {
            add(sequence, 1, null, options);
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void increment(final T sequence, final ResultCallback<T> callback) {
            add(sequence, 1, callback, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.SequenceFetchOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void increment(final T sequence, final ResultCallback<T> callback, final AB.SequenceAddOption option) {
            add(sequence, 1, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスの現在値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.SequenceFetchOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void increment(final T sequence, final ResultCallback<T> callback, final EnumSet<AB.SequenceAddOption> options) {
            add(sequence, 1, callback, options);
        }

//endregion

//region Get Next Value

        /**
         * 同期モードでシーケンスの現在値に+1した値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値に+1した値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getNextValueSynchronously(final T sequence) throws ABException {
            return getValueSynchronously(sequence, 1, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 同期モードでシーケンスの現在値に+1した値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値に+1した値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getNextValueSynchronously(final T sequence, final AB.SequenceAddOption option) throws ABException {
            return getValueSynchronously(sequence, 1, EnumSet.of(option));
        }

        /**
         * 同期モードでシーケンスの現在値に+1した値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値に+1した値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getNextValueSynchronously(final T sequence, final EnumSet<AB.SequenceAddOption> options) throws ABException {
            return getValueSynchronously(sequence, 1, options);
        }

        /**
         * 非同期モードでシーケンスの現在値に+1した値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getNextValue(final T sequence, final SequenceCallback callback) {
            getValue(sequence, 1, callback, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスの現在値に+1した値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getNextValue(final T sequence, final SequenceCallback callback, final AB.SequenceAddOption option) {
            getValue(sequence, 1, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスの現在値に+1した値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getNextValue(final T sequence, final SequenceCallback callback, final EnumSet<AB.SequenceAddOption> options) {
            getValue(sequence, 1, callback, options);
        }

//endregion

//region Decrement

        /**
         * 同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> decrementSynchronously(final T sequence) throws ABException {
            return addSynchronously(sequence, -1, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> decrementSynchronously(final T sequence, AB.SequenceAddOption option) throws ABException {
            return addSynchronously(sequence, -1, EnumSet.of(option));
        }

        /**
         * 同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> decrementSynchronously(final T sequence, EnumSet<AB.SequenceAddOption> options) throws ABException {
            return addSynchronously(sequence, -1, options);
        }

        /**
         * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void decrement(final T sequence) {
            add(sequence, -1, null, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void decrement(final T sequence, final AB.SequenceAddOption option) {
            add(sequence, -1, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void decrement(final T sequence, final EnumSet<AB.SequenceAddOption> options) {
            add(sequence, -1, null, options);
        }

        /**
         * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void decrement(final T sequence, final ResultCallback<T> callback) {
            add(sequence, -1, callback, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void decrement(final T sequence, final ResultCallback<T> callback, final AB.SequenceAddOption option) {
            add(sequence, -1, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void decrement(final T sequence, final ResultCallback<T> callback, final EnumSet<AB.SequenceAddOption> options) {
            add(sequence, -1, callback, options);
        }

//endregion

//region Get Previous Value

        /**
         * 同期モードでシーケンスの現在値を-1した値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値を-1した値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getPreviousValueSynchronously(final T sequence) throws ABException {
            return getValueSynchronously(sequence, -1, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 同期モードでシーケンスの現在値を-1した値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値を-1した値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getPreviousValueSynchronously(final T sequence, final AB.SequenceAddOption option) throws ABException {
            return getValueSynchronously(sequence, -1, EnumSet.of(option));
        }

        /**
         * 同期モードでシーケンスの現在値を-1した値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスの現在値を-1した値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getPreviousValueSynchronously(final T sequence, final EnumSet<AB.SequenceAddOption> options) throws ABException {
            return getValueSynchronously(sequence, -1, options);
        }

        /**
         * 非同期モードでシーケンスの現在値を-1した値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getPreviousValue(final T sequence, final SequenceCallback callback) {
            getValue(sequence, -1, callback, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスの現在値を-1した値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getPreviousValue(final T sequence, final SequenceCallback callback, final AB.SequenceAddOption option) {
            getValue(sequence, -1, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスの現在値を-1した値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getPreviousValue(final T sequence, final SequenceCallback callback, final EnumSet<AB.SequenceAddOption> options) {
            getValue(sequence, -1, callback, options);
        }

//endregion

//region Get Value With Adding Amount

        /**
         * 同期モードでシーケンスに値を加算（減算）した計算後の値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param addingAmount シーケンスの現在値に対して加算（減算）する値
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスに値を加算（減算）した計算後の値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getValueSynchronously(final T sequence, final long addingAmount) throws ABException {
            return getValueSynchronously(sequence, addingAmount, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 同期モードでシーケンスに値を加算（減算）した計算後の値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param addingAmount シーケンスの現在値に対して加算（減算）する値
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスに値を加算（減算）した計算後の値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getValueSynchronously(final T sequence, final long addingAmount, final AB.SequenceAddOption option) throws ABException {
            return getValueSynchronously(sequence, addingAmount, EnumSet.of(option));
        }

        /**
         * 同期モードでシーケンスに値を加算（減算）した計算後の値を取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param addingAmount シーケンスの現在値に対して加算（減算）する値
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return シーケンスに値を加算（減算）した計算後の値
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> long getValueSynchronously(final T sequence, final long addingAmount, final EnumSet<AB.SequenceAddOption> options) throws ABException {
            ABResult<T> ret = addSynchronously(sequence, addingAmount, options);
            T seq = ret.getData();
            return (seq != null) ? seq.getValue() : 0;
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）した後の計算後の値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param addingAmount シーケンスの現在値に対して加算（減算）する値
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getValue(final T sequence, final long addingAmount, final SequenceCallback callback) {
            getValue(sequence, addingAmount, callback, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）した後の計算後の値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param addingAmount シーケンスの現在値に対して加算（減算）する値
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getValue(final T sequence, final long addingAmount, final SequenceCallback callback, final AB.SequenceAddOption option) {
            getValue(sequence, addingAmount, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）した後の計算後の値を取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param addingAmount シーケンスの現在値に対して加算（減算）する値
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void getValue(final T sequence, final long addingAmount, final SequenceCallback callback, final EnumSet<AB.SequenceAddOption> options) {
            add(sequence, addingAmount, new ResultCallback<T>() {
                @Override
                public void done(ABResult<T> result, ABException e) {
                    if (e == null && result.getData() != null) {
                        T seq = result.getData();
                        executeSequenceCallbackIfNeeded(callback, seq.getValue(), null);
                    } else {
                        executeSequenceCallbackIfNeeded(callback, 0, e);
                    }
                }
            });
        }

//endregion

//region Add

        /**
         * 同期モードでシーケンスに値を加算（減算）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> addSynchronously(final T sequence, final long amount) throws ABException {
            return addSynchronously(sequence, amount, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 同期モードでシーケンスに値を加算（減算）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> addSynchronously(final T sequence, final long amount, final AB.SequenceAddOption option) throws ABException {
            return addSynchronously(sequence, amount, EnumSet.of(option));
        }

        /**
         * 同期モードでシーケンスに値を加算（減算）します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> ABResult<T> addSynchronously(final T sequence, final long amount, final EnumSet<AB.SequenceAddOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("sequence", sequence);
            params.put("amount", amount);
            validate("add", params);

            //リクエストBODYの組み立て
            final Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("inc", amount); }};
            String body = Helper.ModelHelper.toJson(bodyMap);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPATCH(
                    AB.baasSequenceAPIURLWithFormat("/%s/_issue", sequence.getCollectionID()),
                    body,
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            Map<String, Object> json = restResult.getData();
            try {
                T seq = (T) sequence.clone(); //FIXME: singleton想定なのでどーするか？
                Object valueObj = json.get("seq");
                if (valueObj instanceof String) {
                    seq.setValue(Long.parseLong((String) valueObj));
                } else if (valueObj instanceof Integer) {
                    seq.setValue((int)valueObj);
                } else if (valueObj instanceof Long) {
                    seq.setValue((long)valueObj);
                } else {
                    ABLog.e(TAG, "Unexpected sequence value. [value=" + valueObj.toString() + "]");
                }
                ret.setData(seq);
            } catch (CloneNotSupportedException e) {
                throw new ABException(e);
            }

            return ret;
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void add(final T sequence, final long amount) {
            add(sequence, amount, null, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void add(final T sequence, final long amount, final AB.SequenceAddOption option) {
            add(sequence, amount, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void add(final T sequence, final long amount, final EnumSet<AB.SequenceAddOption> options) {
            add(sequence, amount, null, options);
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void add(final T sequence, final long amount, final ResultCallback<T> callback) {
            add(sequence, amount, callback, EnumSet.of(AB.SequenceAddOption.NONE));
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.SequenceAddOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void add(final T sequence, final long amount, final ResultCallback<T> callback, final AB.SequenceAddOption option) {
            add(sequence, amount, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスに値を加算（減算）します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param amount シーケンスの現在値に対して加算（減算）する値
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.SequenceAddOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
         */
        public static <T extends ABSequence> void add(final T sequence, final long amount, final ResultCallback<T> callback, final EnumSet<AB.SequenceAddOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("sequence", sequence);
                params.put("amount", amount);
                validate("add", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            final Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("inc", amount); }};
            String body;
            try {
                body = Helper.ModelHelper.toJson(bodyMap);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.PATCH(
                    AB.baasSequenceAPIURLWithFormat("/%s/_issue", sequence.getCollectionID()),
                    body,
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                Map<String, Object> json = restResult.getData();
                                @SuppressWarnings("unchecked") T seq = (T) sequence.clone(); //FIXME: singleton想定なのでどーするか？
                                Object valueObj = json.get("seq");
                                if (valueObj instanceof String) {
                                    seq.setValue(Long.parseLong((String) valueObj));
                                } else if (valueObj instanceof Integer) {
                                    seq.setValue((int)valueObj);
                                } else if (valueObj instanceof Long) {
                                    seq.setValue((long)valueObj);
                                } else {
                                    ABLog.e(TAG, "Unexpected sequence value. [value=" + valueObj.toString() + "]");
                                }
                                ret.setData(seq);
                            } catch (CloneNotSupportedException ie) {
                                executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                return;
                            }
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

//endregion

//region Fetch

        /**
         * 同期モードでシーケンスを取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> ABResult<T> fetchSynchronously(final T sequence) throws ABException {
            return fetchSynchronously(sequence, EnumSet.of(AB.SequenceFetchOption.NONE));
        }

        /**
         * 同期モードでシーケンスを取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceFetchOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> ABResult<T> fetchSynchronously(final T sequence, final AB.SequenceFetchOption option) throws ABException {
            return fetchSynchronously(sequence, EnumSet.of(option));
        }

        /**
         * 同期モードでシーケンスを取得します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceFetchOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> ABResult<T> fetchSynchronously(final T sequence, final EnumSet<AB.SequenceFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("sequence", sequence);
            validate("fetch", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasSequenceAPIURLWithFormat("/%s", sequence.getCollectionID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(sequence);
            T obj = Helper.ModelHelper.toObject(clazz, sequence.getCollectionID(), restResult.getData(), false); //TODO: singleton想定なのでどーするか？
            ret.setData(obj);

            return ret;
        }

        /**
         * 非同期モードでシーケンスを取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> void fetch(final T sequence, final ResultCallback<T> callback) {
            fetch(sequence, callback, EnumSet.of(AB.SequenceFetchOption.NONE));
        }

        /**
         * 非同期モードでシーケンスを取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param option {@link AB.SequenceFetchOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> void fetch(final T sequence, final ResultCallback<T> callback, final AB.SequenceFetchOption option) {
            fetch(sequence, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンスを取得します。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に long 値を返すコールバックハンドラ
         * @param options {@link AB.SequenceFetchOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
         */
        public static <T extends ABSequence> void fetch(final T sequence, final ResultCallback<T> callback, final EnumSet<AB.SequenceFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("sequence", sequence);
                validate("fetch", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasSequenceAPIURLWithFormat("/%s", sequence.getCollectionID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(sequence);
                                T obj = Helper.ModelHelper.toObject(clazz, sequence.getCollectionID(), restResult.getData(), false); //TODO: singleton想定なのでどーするか？
                                ret.setData(obj);
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Reset

        /**
         * 同期モードでシーケンス・オブジェクトをリセットします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static <T extends ABSequence> ABResult<T> resetSynchronously(final T sequence) throws ABException {
            return resetSynchronously(sequence, EnumSet.of(AB.SequenceResetOption.NONE));
        }

        /**
         * 同期モードでシーケンス・オブジェクトをリセットします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceResetOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static <T extends ABSequence> ABResult<T> resetSynchronously(final T sequence, final AB.SequenceResetOption option) throws ABException {
            return resetSynchronously(sequence, EnumSet.of(option));
        }

        /**
         * 同期モードでシーケンス・オブジェクトをリセットします。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceResetOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         */
        public static <T extends ABSequence> ABResult<T> resetSynchronously(final T sequence, final EnumSet<AB.SequenceResetOption> options) throws ABException {
            ABResult<T> ret;
            long currentVal = getCurrentValueSynchronously(sequence);
            long initialVal = sequence.getInitialValue();
            long delta;
            if (currentVal < initialVal) {
                delta = initialVal - currentVal;
            } else if (currentVal > initialVal) {
                delta = currentVal - initialVal;
            } else {
                ret = new ABResult<>();
                ret.setCode(304);
                ret.setData(sequence);
                return ret;
            }

            ret = addSynchronously(sequence, delta);

            return ret;
        }

        /**
         * 非同期モードでシーケンス・オブジェクトをリセットします。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         */
        public static <T extends ABSequence> void reset(final T sequence) {
            reset(sequence, null, EnumSet.of(AB.SequenceResetOption.NONE));
        }

        /**
         * 非同期モードでシーケンス・オブジェクトをリセットします。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.SequenceResetOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         */
        public static <T extends ABSequence> void reset(final T sequence, final AB.SequenceResetOption option) {
            reset(sequence, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンス・オブジェクトをリセットします。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.SequenceResetOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         */
        public static <T extends ABSequence> void reset(final T sequence, final EnumSet<AB.SequenceResetOption> options) {
            reset(sequence, null, options);
        }

        /**
         * 非同期モードでシーケンス・オブジェクトをリセットします。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         */
        public static <T extends ABSequence> void reset(final T sequence, final ResultCallback<T> callback) {
            reset(sequence, callback, EnumSet.of(AB.SequenceResetOption.NONE));
        }

        /**
         * 非同期モードでシーケンス・オブジェクトをリセットします。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.SequenceResetOption} オプション
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         */
        public static <T extends ABSequence> void reset(final T sequence, final ResultCallback<T> callback, final AB.SequenceResetOption option) {
            reset(sequence, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでシーケンス・オブジェクトをリセットします。
         * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.SequenceResetOption} オプション群
         * @param <T> {@link ABSequence} クラス (またはその派生クラス)
         */
        public static <T extends ABSequence> void reset(final T sequence, final ResultCallback<T> callback, final EnumSet<AB.SequenceResetOption> options) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ABResult<T> ret = null;
                    ABException rex = null;
                    try {
                        ret = resetSynchronously(sequence, options);
                    } catch (ABException e) {
                        rex = e;
                    } finally {
                        executeResultCallbackIfNeeded(callback, ret, rex);
                    }
                    return null;
                }
            }.execute();
        }

//endregion

//region Cancel

        /**
         * 実行中の API リクエストをキャンセルします。
         * @deprecated use {@link #cancelAll()} instead.
         */
        @Deprecated
        public static void cancel() {
            Pattern urlPattern = Pattern.compile("/seq/");
            ABRestClient.cancel(urlPattern);
        }

        // XXX: 個々のリクエストをキャンセルするケースはまずないので実装は見送る
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
        // * @param <T> {@link ABSequence} クラス (またはその派生クラス)
        // */
        //public static <T extends ABSequence> void cancel(final T sequence) {
        //    cancel(sequence, EnumSet.of(AB.SequenceCancelOption.NONE));
        //}
        //
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
        // * @param option {@link AB.SequenceCancelOption} オプション
        // * @param <T> {@link ABSequence} クラス (またはその派生クラス)
        // */
        //public static <T extends ABSequence> void cancel(final T sequence, final AB.SequenceCancelOption option) {
        //    cancel(sequence, EnumSet.of(AB.SequenceCancelOption.NONE));
        //}
        //
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param sequence {@link ABSequence} オブジェクト (またはその派生オブジェクト)
        // * @param options {@link AB.SequenceCancelOption} オプション群
        // * @param <T> {@link ABSequence} クラス (またはその派生クラス)
        // */
        //public static <T extends ABSequence> void cancel(final T sequence, final EnumSet<AB.SequenceCancelOption> options) {
        //    if (sequence == null || TextUtils.isEmpty(sequence.getCollectionID()) || TextUtils.isEmpty(sequence.getID())) return;
        //    String pattern = String.format("/seq/%s/%s/%s/%s", AB.Config.getDatastoreID(),
        //            AB.Config.getApplicationID(), sequence.getCollectionID(), sequence.getID());
        //    Pattern urlPattern = Pattern.compile(pattern);
        //    ABRestClient.cancel(urlPattern);
        //}

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         */
        public static void cancelAll() {
            cancelAll(EnumSet.of(AB.SequenceCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param option {@link AB.SequenceCancelOption} オプション
         */
        public static void cancelAll(final AB.SequenceCancelOption option) {
            cancelAll(EnumSet.of(AB.SequenceCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param options {@link AB.SequenceCancelOption} オプション群
         */
        public static void cancelAll(final EnumSet<AB.SequenceCancelOption> options) {
            String pattern = String.format("/seq/%s/%s", AB.Config.getDatastoreID(), AB.Config.getApplicationID());
            Pattern urlPattern = Pattern.compile(pattern);
            ABRestClient.cancel(urlPattern);
        }

//endregion

//region Miscellaneous

        /**
         * シーケンス・オブジェクト検索用のクエリオブジェクトを取得します。
         * @return {@link ABQuery} オブジェクト
         */
        public static ABQuery query() {
            return ABSequence.query();
        }

//endregion

//region Non-Public methods

        private static <T> void executeResultCallbackIfNeeded(ResultCallback<T> callback, ABResult<T> result, ABException e) {
            if (callback != null) {
                callback.internalDone(result, e);
            }
        }

        private static <T> void executeSequenceCallbackIfNeeded(SequenceCallback callback, long value, ABException e) {
            if (callback != null) {
                callback.internalDone(value, e);
            }
        }

        private static void validate(String method, Map<String, Object> params) throws ABException {
            switch (method) {
                case "add": {
                    ABSequence sequence = (ABSequence) params.get("sequence");
                    ABValidator.validate("sequence", sequence, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: sequence]");
                    }});
                    /*
                    ABValidator.validate("ID", sequence.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: sequence.ID]");
                    }});
                    */
                    long amount = (Long)params.get("amount");
                    ABValidator.validate("amount", amount, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: amount]");
                    }});
                    break;
                }
                case "fetch": {
                    ABSequence sequence = (ABSequence) params.get("sequence");
                    ABValidator.validate("sequence", sequence, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: sequence]");
                    }});
                    break;
                }
            }
        }

//endregion

    }

    /**
     * デバイス・サービス。
     * <p></p>
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=130">アピアリーズドキュメント &raquo; プッシュ通知</a>
     */
    public static class DeviceService {

        private static final String TAG = DeviceService.class.getSimpleName();

//region getRegistrationID

        /**
         * レジストレーションIDを取得します。
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         */
        public static void getRegistrationID(final ResultCallback<String> callback) {
            //Check Play Services Availability
            final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
            Context context = AB.sApplicationContext;
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
            if (resultCode != ConnectionResult.SUCCESS) {
                ABLog.i(TAG, "No valid Google Play Services APK found.");
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    ABException e = new ABException(412, String.format("Google Play Services: Recoverable Error [code=%d]", resultCode)); //TODO: APIコールじゃないけどAPIのエラーコードを返してる
                    executeResultCallbackIfNeeded(callback, null, e);
                } else {
                    ABException e = new ABException(412, String.format("Google Play Services: Unrecoverable Error [code=%d]", resultCode)); //TODO: APIコールじゃないけどAPIのエラーコードを返してる
                    executeResultCallbackIfNeeded(callback, null, e);
                }
                return;
            }

            //バリデーション
            try {
                String senderID = AB.Config.Push.getSenderID();
                Map<String, Object> params = new HashMap<>();
                params.put("senderID", senderID);
                validate("getRegistrationID", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            String storedRegId = AB.Preference.load(Preference.PREF_KEY_SESSION_REG_ID);
            if (storedRegId == null || storedRegId.isEmpty()) { //XXX: isEmpty() は冗長
                //RegistrationID 発行要求
                new AsyncTask<Void, Void, Void>() {
                    String regId = null;
                    ABException ex = null;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(AB.sApplicationContext);
                            regId = gcm.register(AB.Config.Push.getSenderID());
                        } catch (IOException e) {
                            ex = new ABException(e);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        if (ex == null) {
                            ABResult<String> ret = new ABResult<>();
                            if (regId != null && regId.length() > 0) {
                                AB.Preference.save(Preference.PREF_KEY_SESSION_REG_ID, regId);
                                //NOTE: レジストレーションIDを最後に取得したアプリバージョンを保存しておく
                                int currentVersion = AB.Preference.load(Preference.PREF_KEY_APP_VERSION, getAppVersion());
                                AB.Preference.save(Preference.PREF_KEY_APP_VERSION_WHEN_REG_ID_FETCHED, currentVersion);
                                ret.setData(regId);
                            }
                            executeResultCallbackIfNeeded(callback, ret, null);
                        } else {
                            executeResultCallbackIfNeeded(callback, null, ex);
                        }
                    }
                }.execute(null, null, null);
            } else {
                //バージョンチェック (NOTE: アプリのバージョンが上がるとレジストレーションIDを再取得する必要があるため)
                final int registeredVersion = AB.Preference.load(Preference.PREF_KEY_APP_VERSION_WHEN_REG_ID_FETCHED, 0);
                //int currentVersion = getAppVersion();
                final int currentVersion = AB.Preference.load(Preference.PREF_KEY_APP_VERSION, getAppVersion());

                if (registeredVersion != currentVersion) {
                    ABLog.i(TAG, "App version changed.");
                    //RegistrationID 発行要求
                    new AsyncTask<Void, Void, Void>() {
                        String regId = null;
                        ABException ex = null;

                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(AB.sApplicationContext);
                                regId = gcm.register(AB.Config.Push.getSenderID());
                            } catch (IOException e) {
                                ex = new ABException(e);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if (ex == null) {
                                ABResult<String> ret = new ABResult<>();
                                if (regId != null && regId.length() > 0) {
                                    AB.Preference.save(Preference.PREF_KEY_SESSION_REG_ID, regId);
                                    //NOTE: レジストレーションIDを最後に取得したアプリバージョンを保存しておく
                                    AB.Preference.save(Preference.PREF_KEY_APP_VERSION_WHEN_REG_ID_FETCHED, currentVersion);
                                    ret.setData(regId);
                                }
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } else {
                                executeResultCallbackIfNeeded(callback, null, ex);
                            }
                        }
                    }.execute(null, null, null);
                } else {
                    ABLog.d(TAG, "Already stored valid registration id. [regId:" + storedRegId + "]");
                    ABResult<String> ret = new ABResult<>();
                    ret.setData(storedRegId);
                    executeResultCallbackIfNeeded(callback, ret, null);
                }
            }
        }

//endregion

//region Register

        /**
         * 同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> ABResult<T> registerSynchronously(final T device) throws ABException {
            return registerSynchronously(device, EnumSet.of(AB.DeviceRegistrationOption.NONE));
        }

        /**
         * 同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.DeviceRegistrationOption} オプション
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> ABResult<T> registerSynchronously(final T device, final AB.DeviceRegistrationOption option) throws ABException {
            return registerSynchronously(device, EnumSet.of(option));
        }

        /**
         * 同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.DeviceRegistrationOption} オプション群
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> ABResult<T> registerSynchronously(final T device, final EnumSet<AB.DeviceRegistrationOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("device", device);
            validate("register", params);

            //リクエストBODYの組み立て
            String body = Helper.ModelHelper.toJson(device);

            boolean isPermanently = !options.contains(AB.DeviceRegistrationOption.WITHOUT_SAVING);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPUT(
                    AB.baasPushAPIURL(AB.Platform.ANDROID, "/_target?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(device);
            T registered = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), restResult.getData(), false);
/* fetch API がない :(
             boolean useIncompleteData = options.contains(DeviceRegistrationOption.USE_INCOMPLETE_DATA);
            if (useIncompleteData) {
                // AB.Session.getDevice() で取得できるオブジェクトには、ID(objectID) しか格納されていない。
                if (registered != null) {
                    ret.setData(registered);
                    //Sessionにdeviceをセットする
                    AB.Session.setDevice(registered, isPermanently);
                }
            } else {
                // AB.Session.getDevice() で取得できるオブジェクトには、完全なデバイス・オブジェクトが格納される。
                ABResult<T> fetchResult = fetchSynchronously(registered);
                T fetched = fetchResult.getData();
                if (fetched != null) {
                    ret.setData(fetched);
                    //Sessionにdeviceをセットする
                    AB.Session.setDevice(fetched, isPermanently);
                }
            }
*/
            if (registered != null) {
                ret.setData(registered);
                //Sessionにdeviceをセットする
                AB.Session.setDevice(registered, isPermanently);
            }
            return ret;
        }

        /**
         * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> void register(final T device) {
            register(device, null, EnumSet.of(AB.DeviceRegistrationOption.NONE));
        }

        /**
         * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.DeviceRegistrationOption} オプション
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> void register(final T device, final AB.DeviceRegistrationOption option) {
            register(device, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.DeviceRegistrationOption} オプション群
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> void register(final T device, final EnumSet<AB.DeviceRegistrationOption> options) {
            register(device, null, options);
        }

        /**
         * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> void register(final T device, final ResultCallback<T> callback) {
            register(device, callback, EnumSet.of(AB.DeviceRegistrationOption.NONE));
        }

        /**
         * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DeviceRegistrationOption} オプション
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> void register(final T device, final ResultCallback<T> callback, final AB.DeviceRegistrationOption option) {
            register(device, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DeviceRegistrationOption} オプション群
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
         */
        public static <T extends ABDevice> void register(final T device, final ResultCallback<T> callback, final EnumSet<AB.DeviceRegistrationOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("device", device);
                validate("register", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("regid", device.getRegistrationID());
            bodyMap.put("attr", device.getAttributes());
            String body;
            try {
                body = Helper.ModelHelper.toJson(bodyMap);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            final boolean isPermanently = !options.contains(AB.DeviceRegistrationOption.WITHOUT_SAVING);

            //APIの実行
            ABRestClient.PUT(
                    AB.baasPushAPIURL(AB.Platform.ANDROID, "/_target?get=true"), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    body,
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            final ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(device);
                                T registered = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), restResult.getData(), false);
/* fetch API ない :(
                                boolean useIncompleteData = options.contains(AB.DeviceRegistrationOption.USE_INCOMPLETE_DATA);
                                if (useIncompleteData) {
                                    // AB.Session.getDevice() で取得できるオブジェクトには、ID(objectID) しか格納されていない。
                                    if (registered != null) {
                                        ret.setData(registered);
                                        //Sessionにdeviceをセットする
                                        AB.Session.setDevice(registered, isPermanently);
                                    }
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } else {
                                    // AB.Session.getDevice() で取得できるオブジェクトには、完全なデバイス・オブジェクトが格納される。
                                    fetch(registered, new ResultCallback<T>() {
                                        @Override
                                        public void done(ABResult<T> fetchResult, ABException e) {
                                            if (e == null) {
                                                T fetched = fetchResult.getData();
                                                if (fetched != null) {
                                                    ret.setData(fetched);
                                                    //Sessionにdeviceをセットする
                                                    AB.Session.setDevice(fetched, isPermanently);
                                                }
                                                executeResultCallbackIfNeeded(callback, ret, null);
                                                //NOTE: code は login 成功時の 201 が引き継がれる点に注意
                                            } else {
                                                executeResultCallbackIfNeeded(callback, null, e);
                                            }
                                        }
                                    });
                                }
*/
                                if (registered != null) {
                                    ret.setData(registered);
                                    //Sessionにdeviceをセットする
                                    AB.Session.setDevice(registered, isPermanently);
                                }
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Unregister

        /**
         * 同期モードでデバイス・オブジェクトの登録を解除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> ABResult<Void> unregisterSynchronously(final T device) throws ABException {
            return unregisterSynchronously(device, EnumSet.of(AB.DeviceUnregistrationOption.NONE));
        }

        /**
         * 同期モードでデバイス・オブジェクトの登録を解除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.DeviceUnregistrationOption} オプション
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> ABResult<Void> unregisterSynchronously(final T device, final AB.DeviceUnregistrationOption option) throws ABException {
            return unregisterSynchronously(device, EnumSet.of(option));
        }

        /**
         * 同期モードでデバイス・オブジェクトの登録を解除します。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.DeviceUnregistrationOption} オプション群
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> ABResult<Void> unregisterSynchronously(final T device, final EnumSet<AB.DeviceUnregistrationOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("device", device);
            validate("unregister", params);

            //リクエストBODYの組み立て
            Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("regid", device.getRegistrationID()); }};
            String body = Helper.ModelHelper.toJson(bodyMap);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                    AB.baasPushAPIURL(AB.Platform.ANDROID, "/_target?proc=delete"),
                    body,
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );

            //Sessionからdevice情報を削除する
            AB.Session.invalidateDevice();

            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        /**
         * 非同期モードでデバイス・オブジェクトの登録を解除します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> void unregister(final T device) {
            unregister(device, null, EnumSet.of(AB.DeviceUnregistrationOption.NONE));
        }

        /**
         * 非同期モードでデバイス・オブジェクトの登録を解除します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.DeviceUnregistrationOption} オプション
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> void unregister(final T device, final AB.DeviceUnregistrationOption option) {
            unregister(device, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでデバイス・オブジェクトの登録を解除します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.DeviceUnregistrationOption} オプション群
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> void unregister(final T device, final EnumSet<AB.DeviceUnregistrationOption> options) {
            unregister(device, null, options);
        }

        /**
         * 非同期モードでデバイス・オブジェクトの登録を解除します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> void unregister(final T device, final ResultCallback<Void> callback) {
            unregister(device, callback, EnumSet.of(AB.DeviceUnregistrationOption.NONE));
        }

        /**
         * 非同期モードでデバイス・オブジェクトの登録を解除します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.DeviceUnregistrationOption} オプション
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> void unregister(final T device, final ResultCallback<Void> callback, final AB.DeviceUnregistrationOption option) {
            unregister(device, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでデバイス・オブジェクトの登録を解除します。
         * @param device {@link ABDevice} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.DeviceUnregistrationOption} オプション群
         * @param <T> {@link ABDevice} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションを削除する</a>
         */
        public static <T extends ABDevice> void unregister(final T device, final ResultCallback<Void> callback, final EnumSet<AB.DeviceUnregistrationOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("device", device);
                validate("unregister", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("regid", device.getID()); }};
            String body;
            try {
                body = Helper.ModelHelper.toJson(bodyMap);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.POST(
                    AB.baasPushAPIURL(AB.Platform.ANDROID, "/_target?proc=delete"),
                    body,
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            //Sessionからdevice情報を削除する
                            AB.Session.invalidateDevice();
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

//endregion

//region Save ** 非公開 **

        static <T extends ABDevice> ABResult<T> saveSynchronously(final T device) throws ABException {
            return saveSynchronously(device, EnumSet.of(AB.DeviceSaveOption.NONE));
        }

        static <T extends ABDevice> ABResult<T> saveSynchronously(final T device, final AB.DeviceSaveOption option) throws ABException {
            return saveSynchronously(device, EnumSet.of(option));
        }

        static <T extends ABDevice> ABResult<T> saveSynchronously(final T device, final EnumSet<AB.DeviceSaveOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("device", device);
            validate("save", params);

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(device);

            //未登録の場合
            if (device.isNew()) {

                //リクエストBODYの組み立て
                bodyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : device.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = device.outputDataFilter(key, val);
                    bodyMap.put(key, fixedVal);
                }
                body = Helper.ModelHelper.toJson(bodyMap);

                //APIの実行
                ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                        AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s?get=true", device.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        body,
                        ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                );
                ABResult<T> ret = new ABResult<>();
                ret.setCode(restResult.getCode());
                ret.setExtra(restResult.getExtra());
                ret.setRawData(restResult.getRawData());
                T obj = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), restResult.getData(), false);
                ret.setData(obj);

                return ret;
            }

            //更新の場合
            if (device.isDirty()) {

                Map<String, Object> addedMap   = device.getAddedKeysAndValues();
                Map<String, Object> removedMap = device.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = device.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                                removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                                removedCount > 1 || //削除対象フィールドが複数の場合
                                updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : device.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        Object val = entry.getValue();
                        Object fixedVal = device.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    body = Helper.ModelHelper.toJson(bodyMap);

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPUT(
                            AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s?get=true", device.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    T obj = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), restResult.getData(), false);
                    ret.setData(obj);

                    return ret;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s/%s?get=true", device.getCollectionID(), device.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T deleted = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) device.clone(); //unsafe cast
                            copied.remove(field);
                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        Object fixedVal = device.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    body = Helper.ModelHelper.toJson(bodyMap);

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPATCH(
                            AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s?get=true", device.getCollectionID(), device.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T patched = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) device.clone(); //unsafe cast
                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            return ret;
        }

        static <T extends ABDevice> void save(final T device) {
            save(device, null, EnumSet.of(AB.DeviceSaveOption.NONE));
        }

        static <T extends ABDevice> void save(final T device, final AB.DeviceSaveOption option) {
            save(device, null, EnumSet.of(option));
        }

        static <T extends ABDevice> void save(final T device, final EnumSet<AB.DeviceSaveOption> options) {
            save(device, null, options);
        }

        static <T extends ABDevice> void save(final T device, final ResultCallback<T> callback) {
            save(device, callback, EnumSet.of(AB.DeviceSaveOption.NONE));
        }

        static <T extends ABDevice> void save(final T device, final ResultCallback<T> callback, final AB.DeviceSaveOption option) {
            save(device, callback, EnumSet.of(option));
        }

        static <T extends ABDevice> void save(final T device, final ResultCallback<T> callback, final EnumSet<AB.DeviceSaveOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("device", device);
                validate("save", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(device);

            //未登録の場合
            if (device.isNew()) {

                //リクエストBODYの組み立て
                bodyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : device.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = device.outputDataFilter(key, val);
                    bodyMap.put(key, fixedVal);
                }
                try {
                    body = Helper.ModelHelper.toJson(bodyMap);
                } catch (ABException e) {
                    executeResultCallbackIfNeeded(callback, null, e);
                    return;
                }

                //APIの実行
                ABRestClient.POST(
                        AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s?get=true", device.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        body,
                        ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                        new ResultCallback<Map<String, Object>>() {
                            @Override
                            public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                if (e != null) {
                                    executeResultCallbackIfNeeded(callback, null, e);
                                    return;
                                }
                                ABResult<T> ret = new ABResult<>();
                                ret.setCode(restResult.getCode());
                                ret.setExtra(restResult.getExtra());
                                ret.setRawData(restResult.getRawData());
                                try {
                                    T obj = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), restResult.getData(), false);
                                    ret.setData(obj);
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } catch (ABException ie) {
                                    executeResultCallbackIfNeeded(callback, null, ie);
                                }
                            }
                        });
                return;
            }

            //更新の場合
            if (device.isDirty()) {

                Map<String, Object> addedMap   = device.getAddedKeysAndValues();
                Map<String, Object> removedMap = device.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = device.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                                removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                                removedCount > 1 || //削除対象フィールドが複数の場合
                                updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : device.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        if ("_id".equals(key)) { //FIXME: TEMPORARY : patch の場合 _id が含まれるとエラーになる
                            continue;            //FIXME: TEMPORARY
                        }                        //FIXME: TEMPORARY
                        Object val = entry.getValue();
                        Object fixedVal = device.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    try {
                        body = Helper.ModelHelper.toJson(bodyMap);
                    } catch (ABException e) {
                        executeResultCallbackIfNeeded(callback, null, e);
                        return;
                    }

                    //APIの実行
                    /* FIXME: TEMPORARY: コレクションの権限を"登録:ADMIN"に指定している場合、 PUT だと 403(Forbidden) が発生してしまうので暫定的に POST+proc=patch にする (2015.7.29 - ogawa)
                    ABRestClient.PUT(
                            AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s?get=true", device.getCollectionID(), device.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    */
                    ABRestClient.POST(
                            AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s?proc=patch&get=true", device.getCollectionID(), device.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    try {
                                        T obj = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), restResult.getData(), false);
                                        ret.setData(obj);
                                        executeResultCallbackIfNeeded(callback, ret, null);
                                    } catch (ABException ie) {
                                        executeResultCallbackIfNeeded(callback, null, ie);
                                    }
                                }
                            });
                    return;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s/%s?get=true", device.getCollectionID(), device.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABRestClient.DELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T deleted = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) device.clone(); //unsafe cast
                                            copied.remove(field);
                                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            });
                    return;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        Object fixedVal = device.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    try {
                        body = Helper.ModelHelper.toJson(bodyMap);
                    } catch (ABException e) {
                        executeResultCallbackIfNeeded(callback, null, e);
                        return;
                    }

                    //APIの実行
                    ABRestClient.PATCH(
                            AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s?get=true", device.getCollectionID(), device.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T patched = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) device.clone(); //unsafe cast
                                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            });
                    return;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            executeResultCallbackIfNeeded(callback, ret, null);
        }

//endregion

//region Save (Objects) ** 非公開 **

        static <T extends ABDevice> ABResult<List<T>> saveAllSynchronously(final List<T> devices) throws ABException {
            return saveAllSynchronously(devices, EnumSet.of(AB.DeviceSaveOption.NONE));
        }

        static <T extends ABDevice> ABResult<List<T>> saveAllSynchronously(final List<T> devices, final AB.DeviceSaveOption option) throws ABException {
            return saveAllSynchronously(devices, EnumSet.of(option));
        }

        static <T extends ABDevice> ABResult<List<T>> saveAllSynchronously(final List<T> devices, final EnumSet<AB.DeviceSaveOption> options) throws ABException {
            ABResult<List<T>> ret = new ABResult<>();
            List<T> savedObjects = new ArrayList<>();
            Integer success = 0;
            for (T device : devices) {
                ABResult<T> r = saveSynchronously(device, options);
                if (r.getCode() >= 200 && r.getCode() <= 399) {
                    T saved = r.getData();
                    savedObjects.add(saved);
                    success++;
                    ret.setCode(r.getCode());
                }
            }
            ret.setTotal(success);
            ret.setData(savedObjects);
            return ret;
        }

        static <T extends ABDevice> void saveAll(final List<T> devices) {
            saveAll(devices, null, null, EnumSet.of(AB.DeviceSaveOption.NONE));
        }

        static <T extends ABDevice> void saveAll(final List<T> devices, final AB.DeviceSaveOption option) {
            saveAll(devices, null, null, EnumSet.of(option));
        }

        static <T extends ABDevice> void saveAll(final List<T> devices, final EnumSet<AB.DeviceSaveOption> options) {
            saveAll(devices, null, null, options);
        }

        static <T extends ABDevice> void saveAll(final List<T> devices, final ResultCallback<List<T>> callback) {
            saveAll(devices, callback, null, EnumSet.of(AB.DeviceSaveOption.NONE));
        }

        static <T extends ABDevice> void saveAll(final List<T> devices, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback) {
            saveAll(devices, callback, progressCallback, EnumSet.of(AB.DeviceSaveOption.NONE));
        }

        static <T extends ABDevice> void saveAll(final List<T> devices, final ResultCallback<List<T>> callback, final AB.DeviceSaveOption option) {
            saveAll(devices, callback, null, EnumSet.of(option));
        }

        static <T extends ABDevice> void saveAll(final List<T> devices, final ResultCallback<List<T>> callback, final EnumSet<AB.DeviceSaveOption> options) {
            saveAll(devices, callback, null, options);
        }

        static <T extends ABDevice> void saveAll(final List<T> devices, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final AB.DeviceSaveOption option) {
            saveAll(devices, callback, progressCallback, EnumSet.of(option));
        }

        static <T extends ABDevice> void saveAll(final List<T> devices, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final EnumSet<AB.DeviceSaveOption> options) {
            new ABAsyncBatchExecutor(devices, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T device = (T) target;
                    device.save(new ResultCallback<T>() {
                        @Override
                        public void done(ABResult<T> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }

//endregion

//region Delete ** 非公開 **

        static <T extends ABDevice> ABResult<Void> deleteSynchronously(final T device) throws ABException {
            return deleteSynchronously(device, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> ABResult<Void> deleteSynchronously(final T device, final AB.DeviceDeleteOption option) throws ABException {
            return deleteSynchronously(device, EnumSet.of(option));
        }

        static <T extends ABDevice> ABResult<Void> deleteSynchronously(final T device, final EnumSet<AB.DeviceDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("device", device);
            validate("delete", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s", device.getCollectionID(), device.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        static <T extends ABDevice> void delete(final T device) {
            delete(device, null, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> void delete(final T device, final AB.DeviceDeleteOption option) {
            delete(device, null, EnumSet.of(option));
        }

        static <T extends ABDevice> void delete(final T device, final EnumSet<AB.DeviceDeleteOption> options) {
            delete(device, null, options);
        }

        static <T extends ABDevice> void delete(final T device, final ResultCallback<Void> callback) {
            delete(device, callback, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> void delete(final T device, final ResultCallback<Void> callback, final AB.DeviceDeleteOption option) {
            delete(device, callback, EnumSet.of(option));
        }

        static <T extends ABDevice> void delete(final T device, final ResultCallback<Void> callback, final EnumSet<AB.DeviceDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("device", device);
                validate("delete", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s", device.getCollectionID(), device.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

//endregion

//region Delete (Objects) ** 非公開 **

        static <T extends ABDevice> ABResult<Void> deleteAllSynchronously(final List<T> devices) throws ABException {
            return deleteAllSynchronously(devices, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> ABResult<Void> deleteAllSynchronously(final List<T> devices, final AB.DeviceDeleteOption option) throws ABException {
            return deleteAllSynchronously(devices, EnumSet.of(option));
        }

        static <T extends ABDevice> ABResult<Void> deleteAllSynchronously(final List<T> devices, final EnumSet<AB.DeviceDeleteOption> options) throws ABException {
            ABResult<Void> ret = new ABResult<>();
            Integer success = 0;
            for (T device : devices) {
                ret = device.deleteSynchronously(options);
                if (ret.getCode() >= 200 && ret.getCode() <= 399) {
                    success++;
                }
            }
            ret.setTotal(success);
            return ret;
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices) {
            deleteAll(devices, null, null, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices, final AB.DeviceDeleteOption option) {
            deleteAll(devices, null, null, EnumSet.of(option));
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices, final EnumSet<AB.DeviceDeleteOption> options) {
            deleteAll(devices, null, null, options);
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices, final ResultCallback<Void> callback) {
            deleteAll(devices, callback, null, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices, final ResultCallback<Void> callback, final ProgressCallback progressCallback) {
            deleteAll(devices, callback, progressCallback, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices, final ResultCallback<Void> callback, final AB.DeviceDeleteOption option) {
            deleteAll(devices, callback, null, EnumSet.of(option));
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices, final ResultCallback<Void> callback, final EnumSet<AB.DeviceDeleteOption> options) {
            deleteAll(devices, callback, null, options);
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final AB.DeviceDeleteOption option) {
            deleteAll(devices, callback, progressCallback, EnumSet.of(option));
        }

        static <T extends ABDevice> void deleteAll(final List<T> devices, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final EnumSet<AB.DeviceDeleteOption> options) {
            new ABAsyncBatchExecutor(devices, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T devices = (T) target;
                    devices.delete(new ResultCallback<Void>() {
                        @Override
                        public void done(ABResult<Void> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }

//endregion

//region Delete (Query) ** 非公開 **

        static <T extends ABDevice> ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final AB.DeviceDeleteOption option) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(option));
        }

        static <T extends ABDevice> ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.DeviceDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("deleteWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasPushAPIURL(AB.Platform.ANDROID, query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        static <T extends ABDevice> void deleteWithQuery(final ABQuery query) {
            deleteWithQuery(query, null, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> void deleteWithQuery(final ABQuery query, final AB.DeviceDeleteOption option) {
            deleteWithQuery(query, null, EnumSet.of(option));
        }

        static <T extends ABDevice> void deleteWithQuery(final ABQuery query, final EnumSet<AB.DeviceDeleteOption> options) {
            deleteWithQuery(query, null, options);
        }

        static <T extends ABDevice> void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback) {
            deleteWithQuery(query, callback, EnumSet.of(AB.DeviceDeleteOption.NONE));
        }

        static <T extends ABDevice> void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final AB.DeviceDeleteOption option) {
            deleteWithQuery(query, callback, EnumSet.of(option));
        }

        static <T extends ABDevice> void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final EnumSet<AB.DeviceDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("deleteWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasPushAPIURL(AB.Platform.ANDROID, query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            try {
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (Exception ie) {
                                executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                            }
                        }
                    });
        }

//endregion

//region Fetch ** 非公開 **

        static <T extends ABDevice> ABResult<T> fetchSynchronously(final T device) throws ABException {
            return fetchSynchronously(device, EnumSet.of(AB.DeviceFetchOption.NONE));
        }

        static <T extends ABDevice> ABResult<T> fetchSynchronously(final T device, final AB.DeviceFetchOption option) throws ABException {
            return fetchSynchronously(device, EnumSet.of(option));
        }

        static <T extends ABDevice> ABResult<T> fetchSynchronously(final T device, final EnumSet<AB.DeviceFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("device", device);
            validate("fetch", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s", device.getCollectionID(), device.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(device);
            T obj = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), restResult.getData(), false);
            ret.setData(obj);

            return ret;
        }

        static <T extends ABDevice> void fetch(final T device, final ResultCallback<T> callback) {
            fetch(device, callback, EnumSet.of(AB.DeviceFetchOption.NONE));
        }

        static <T extends ABDevice> void fetch(final T device, final ResultCallback<T> callback, final AB.DeviceFetchOption option) {
            fetch(device, callback, EnumSet.of(option));
        }

        static <T extends ABDevice> void fetch(final T device, final ResultCallback<T> callback, final EnumSet<AB.DeviceFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("device", device);
                validate("fetch", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/%s/%s", device.getCollectionID(), device.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(device);
                                T obj = Helper.ModelHelper.toObject(clazz, device.getCollectionID(), restResult.getData(), false);
                                ret.setData(obj);
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Find (Query) ** 非公開 **

        static <T extends ABDevice> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(AB.DeviceFetchOption.NONE));
        }

        static <T extends ABDevice> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final AB.DeviceFetchOption option) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(option));
        }

        static <T extends ABDevice> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.DeviceFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("findWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasPushAPIURL(AB.Platform.ANDROID, query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<List<T>> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query, ABDevice.class);
            List<T> objects = Helper.ModelHelper.toObjects(clazz, query.getCollectionID(), restResult.getData(), false);
            ret.setData(objects);
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        static <T extends ABDevice> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback) {
            findWithQuery(query, callback, EnumSet.of(AB.DeviceFetchOption.NONE));
        }

        static <T extends ABDevice> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final AB.DeviceFetchOption option) {
            findWithQuery(query, callback, EnumSet.of(option));
        }

        static <T extends ABDevice> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final EnumSet<AB.DeviceFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("findWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasPushAPIURL(AB.Platform.ANDROID, query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<List<T>> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query, ABDevice.class);
                                List<T> objects = Helper.ModelHelper.toObjects(clazz, query.getCollectionID(), restResult.getData(), false);
                                ret.setData(objects);
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Cancel

        private static Pattern deviceRequestPattern = Pattern.compile("/push/.+/_target");

        /**
         * 実行中の API リクエストをキャンセルします。
         * @deprecated use {@link #cancelAll()} instead.
         */
        @Deprecated
        public static void cancel() {
            ABRestClient.cancel(deviceRequestPattern);
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         */
        public static void cancelAll() {
            cancelAll(EnumSet.of(AB.DeviceCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param option {@link AB.DeviceCancelOption} オプション
         */
        public static void cancelAll(final AB.DeviceCancelOption option) {
            cancelAll(EnumSet.of(AB.DeviceCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param options {@link AB.DeviceCancelOption} オプション群
         */
        public static void cancelAll(final EnumSet<AB.DeviceCancelOption> options) {
            ABRestClient.cancel(deviceRequestPattern);
        }

//endregion

//region Miscellaneous

        /**
         * デバイス・オブジェクト検索用のクエリオブジェクトを取得します。
         * @return {@link ABQuery} オブジェクト
         */
        public static ABQuery query() {
            return ABDevice.query();
        }

//endregion

//region Non-Public methods

        private static void setPaginationInfo(ABResult<?> result, Map<String, Object> json) {
            int total    = json.containsKey("_total") ? (Integer)json.get("_total") : 0;
            int start    = json.containsKey("_start") ? (Integer)json.get("_start") : 0;
            int end      = json.containsKey("_end")   ? (Integer)json.get("_end")   : 0;
            boolean next = json.containsKey("_next") && (Boolean)json.get("_next");
            boolean prev = json.containsKey("_prev") && (Boolean)json.get("_prev");
            result.setTotal(total);
            result.setStart(start);
            result.setEnd(end);
            result.setNext(next);
            result.setPrevious(prev);
        }

        private static <T> void executeResultCallbackIfNeeded(ResultCallback<T> callback, ABResult<T> result, ABException e) {
            if (callback != null) {
                callback.internalDone(result, e);
            }
        }

        private static void executeProgressCallbackIfNeeded(ProgressCallback progressCallback, int progress) {
            float p = (float)progress / 100;
            if (p <= 1.0 && progressCallback != null) {
                progressCallback.internalUpdateProgress(p);
            }
        }

        private static void validate(String method, Map<String, Object> params) throws ABException {
            switch (method) {
                case "getRegistrationID": {
                    String regId = (String) params.get("senderID");
                    ABValidator.validate("senderID", regId, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: senderID]");
                    }});
                    break;
                }
                case "register": {
                    ABDevice device = (ABDevice) params.get("device");
                    ABValidator.validate("device", device, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device]");
                    }});
                    ABValidator.validate("registrationID", device.getRegistrationID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device.registrationID]");
                    }});
                    break;
                }
                case "unregister": {
                    ABDevice device = (ABDevice) params.get("device");
                    ABValidator.validate("device", device, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device]");
                    }});
                    ABValidator.validate("ID", device.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device.ID]");
                    }});
                    break;
                }
                case "save": {
                    ABDevice device = (ABDevice) params.get("device");
                    ABValidator.validate("device", device, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device]");
                    }});
                    break;
                }
                case "delete": {
                    ABDevice device = (ABDevice) params.get("device");
                    ABValidator.validate("device", device, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device]");
                    }});
                    ABValidator.validate("ID", device.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device.ID]");
                    }});
                    break;
                }
                case "deleteWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
                case "fetch": {
                    ABDevice device = (ABDevice) params.get("device");
                    ABValidator.validate("device", device, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device]");
                    }});
                    break;
                }
                case "findWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
            }
        }

//endregion

    }

    /**
     * プッシュ通知サービス。
     * <p></p>
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=130">アピアリーズドキュメント &raquo; プッシュ通知</a>
     */
    public static class PushService {

        private static final String TAG = PushService.class.getSimpleName();

//region Open Message

        /**
         * 同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> ABResult<Void> openMessageSynchronously(final T message) throws ABException {
            return openMessageSynchronously(message, EnumSet.of(AB.PushMessageOpenOption.NONE));
        }

        /**
         * 同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.PushMessageOpenOption} オプション
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> ABResult<Void> openMessageSynchronously(final T message, final AB.PushMessageOpenOption option) throws ABException {
            return openMessageSynchronously(message, EnumSet.of(option));
        }

        /**
         * 同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.PushMessageOpenOption} オプション群
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @return {@link ABResult} オブジェクト
         * @throws ABException 処理中にエラーが発生した場合にスロー
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> ABResult<Void> openMessageSynchronously(final T message, final EnumSet<AB.PushMessageOpenOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("message", message);
            validate("openMessage", params);

            //リクエストBODYの組み立て
            final String deviceId = message.getDevice().getID();
            final long pushId = message.getPushId();
            final String deviceType = message.getDevice().getType();
            final Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("deviceId", deviceId);put("pushId", pushId); put("deviceType", deviceType); }};
            String body = Helper.ModelHelper.toJson(bodyMap);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncPUT(
                    AB.baasPushAnalyticsAPIURL("/_open"),
                    body,
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());

            return ret;
        }

        /**
         * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> void openMessage(final T message) {
            openMessage(message, null, EnumSet.of(AB.PushMessageOpenOption.NONE));
        }

        /**
         * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param option {@link AB.PushMessageOpenOption} オプション
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> void openMessage(final T message, final AB.PushMessageOpenOption option) {
            openMessage(message, null, EnumSet.of(option));
        }

        /**
         * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param options {@link AB.PushMessageOpenOption} オプション群
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> void openMessage(final T message, final EnumSet<AB.PushMessageOpenOption> options) {
            openMessage(message, null, options);
        }

        /**
         * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> void openMessage(final T message, final ResultCallback<Void> callback) {
            openMessage(message, callback, EnumSet.of(AB.PushMessageOpenOption.NONE));
        }

        /**
         * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.PushMessageOpenOption} オプション
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> void openMessage(final T message, final ResultCallback<Void> callback, final AB.PushMessageOpenOption option) {
            openMessage(message, callback, EnumSet.of(option));
        }

        /**
         * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
         * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.PushMessageOpenOption} オプション群
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
         */
        public static <T extends ABPushMessage> void openMessage(final T message, final ResultCallback<Void> callback, final EnumSet<AB.PushMessageOpenOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("message", message);
                validate("openMessage", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //リクエストBODYの組み立て
            final String deviceId = message.getDevice().getID();
            final long pushId = message.getPushId();
            final String deviceType = message.getDevice().getType();
            final Map<String, Object> bodyMap = new HashMap<String, Object>(){{ put("deviceId", deviceId);put("pushId", pushId); put("deviceType", deviceType); }};
            String body;
            try {
                body = Helper.ModelHelper.toJson(bodyMap);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.PUT(
                    AB.baasPushAnalyticsAPIURL("/_open"),
                    body,
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

//endregion

//region HandlePushIfNeeded

        /**
         * アプリ内でプッシュ通知を捕捉し、適切な方法でアプリ利用者に通知メッセージを表示します。
         * @param context コンテキスト
         * @param intent インテント
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @return true: 引数 Intent にアピアリーズ BaaS で管理するプッシュ通知情報が含まれており、かつ、それを表示する方法が見つかった場合
         * @see <a href="http://docs.appiaries.com/?p=40">アピアリーズドキュメント &raquo; プッシュ通知</a>
         */
        public static <T extends ABPushDialogActivity> boolean handlePushIfNeeded(final Context context, final Intent intent) {
            return handlePushIfNeeded(context, intent, AB.Config.Push.getDefaultConfiguration());
        }

        /**
         * アプリ内でプッシュ通知を捕捉し、適切な方法でアプリ利用者に通知メッセージを表示します。
         * @param context コンテキスト
         * @param intent インテント
         * @param configuration プッシュ通知設定
         * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
         * @return true: 引数 Intent にアピアリーズ BaaS で管理するプッシュ通知情報が含まれており、かつ、それを表示する方法が見つかった場合
         * @see <a href="http://docs.appiaries.com/?p=40">アピアリーズドキュメント &raquo; プッシュ通知</a>
         */
        public static <T extends ABPushDialogActivity> boolean handlePushIfNeeded(final Context context, final Intent intent, final ABPushConfiguration configuration) {

//            ABPushConfiguration config = (ABPushConfiguration)intent.getSerializableExtra(AB.EXTRA_KEY_CONFIG);
//            if (config == null) {
//                //NOTE: intent に config が渡されなかった場合は、現在 AB.Config.Push に設定されている値を使用する
//                //      アプリが起動していないタイミング(MainActivityが起動してない状態)でプッシュ通知を ABGCMBroadcastReceiver で受けた場合の措置。
//                config = AB.Config.Push.getDefaultConfiguration();
//            }

            //NOTE: intent からメッセージオブジェクトを生成する（メッセージではない、または無効なメッセージの場合はそのまま処理を抜ける）
            ABPushMessage message = new ABPushMessage(intent);
            if (!message.isValid()) {
                return false;
            }

            ABDevice device = AB.Session.getDevice();
            assert device != null; // メッセージを受信したということは AB.Session.getDevice() != null のはず
            message.setDevice(device);

            boolean opened = intent.getBooleanExtra("opened", false); //NOTE: このメソッドは onResume() から呼ばれる想定なので、デバイス回転時やbackボタン押下時に複数回コールされないようにするための措置
            if (!opened) {
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancelAll();
/*
                Intent openDialogIntent = new Intent(context, ABPushDialogActivity.class);
                openDialogIntent.setAction(Intent.ACTION_VIEW);
                openDialogIntent.putExtra(AB.EXTRA_KEY_CONFIG, config);
                openDialogIntent.putExtra(AB.EXTRA_KEY_PUSH_MESSAGE, message);
                context.startActivity(openDialogIntent);
                intent.putExtra("opened", true);
*/
                ABPushDialogFragment dialog = ABPushDialogFragment.newInstance(message, configuration);
                dialog.setCancelable(false);
                dialog.show(((Activity) context).getFragmentManager(), "pushDialog");

                if (!intent.getBooleanExtra("via_notification", false)) { //NOTE: 通知バー経由の場合にバイブレーションが重複実行されないようブロックする
                    if (configuration.getVibratePattern() != null) {
                        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(configuration.getVibratePattern(), -1);
                    }
                    if (configuration.getSound() != null) {
                        Uri uri = Uri.parse(configuration.getSound());
                        MediaPlayer mp = MediaPlayer.create(sApplicationContext, uri);
                        mp.start();
                    }
                }

                intent.putExtra("opened", true);
/*
                switch (config.getMode()) {
                    case NOTIFICATION: {
                        Intent newIntent = new Intent();
                        String url = message.getUrl();
                        if (url != null) {
                            if (config.isUsingImplicitIntentWhenOpenUrl()) {
                                Uri uri = Uri.parse(url);
                                newIntent.setAction(Intent.ACTION_VIEW);
                                newIntent.setData(uri);
                            } else {
                                String launchActivityClass = config.getLaunchActivityClass();
                                newIntent.setAction(Intent.ACTION_VIEW);
                                newIntent.setClassName(AB.sApplicationContext, launchActivityClass);
                                newIntent.putExtra(AB.EXTRA_KEY_URL, url);
                            }
                        }
                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                        int iconId;
                        String drawableName = config.getIconName();
                        if (drawableName != null && !drawableName.isEmpty()) {
                            iconId = context.getResources().getIdentifier(config.getIconName(), "drawable", context.getPackageName());
                        } else {
                            iconId = getAppIconID(context);
                        }

                        long when = System.currentTimeMillis();
                        String title = message.getTitle();
                        String msg = message.getMessage();

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                                .setSmallIcon(iconId)
                                .setContentTitle(title)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                                .setContentText(msg)
                                .setTicker(title) //5.0からstatusBar表示には影響しなくなったので冗長か
                                .setFullScreenIntent(contentIntent, true)
                                .setAutoCancel(true)
                                .setVibrate(config.getVibratePattern())
                                .setWhen(when);
                        //builder.setLights(int, int, int);//TODO:
                        //setSound()
                        //builder.setSubText();
                        builder.setContentIntent(contentIntent);

                        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.notify((int)when, builder.build()); //unsafe cast
                        intent.putExtra("opened", true);
                        //TODO: 開封通知をどうするか
                        break;
                    }
                    case DIALOG:
                    default: {
                        Intent openDialogIntent = new Intent(context, ABPushDialogActivity.class);
                        openDialogIntent.setAction(Intent.ACTION_VIEW);
                        openDialogIntent.putExtra(AB.EXTRA_KEY_CONFIG, config);
                        openDialogIntent.putExtra(AB.EXTRA_KEY_PUSH_MESSAGE, message);
                        context.startActivity(openDialogIntent);
                        intent.putExtra("opened", true);
                        break;
                    }
                }
*/
            }
            return true;
        }

//endregion

//region Save ** 非公開 **

        static <T extends ABPushMessage> ABResult<T> saveSynchronously(final T message) throws ABException {
            return saveSynchronously(message, EnumSet.of(AB.PushMessageSaveOption.NONE));
        }

        static <T extends ABPushMessage> ABResult<T> saveSynchronously(final T message, final AB.PushMessageSaveOption option) throws ABException {
            return saveSynchronously(message, EnumSet.of(option));
        }

        static <T extends ABPushMessage> ABResult<T> saveSynchronously(final T message, final EnumSet<AB.PushMessageSaveOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("message", message);
            validate("save", params);

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(message);

            //未登録の場合
            if (message.isNew()) {

                //リクエストBODYの組み立て
                bodyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : message.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = message.outputDataFilter(key, val);
                    bodyMap.put(key, fixedVal);
                }
                body = Helper.ModelHelper.toJson(bodyMap);

                //APIの実行
                ABResult<Map<String, Object>> restResult = ABRestClient.syncPOST(
                        AB.baasPushAnalyticsAPIURLWithFormat("/%s?get=true", message.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        body,
                        ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                );
                ABResult<T> ret = new ABResult<>();
                ret.setCode(restResult.getCode());
                ret.setExtra(restResult.getExtra());
                ret.setRawData(restResult.getRawData());
                T obj = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), restResult.getData(), false);
                ret.setData(obj);

                return ret;
            }

            //更新の場合
            if (message.isDirty()) {

                Map<String, Object> addedMap   = message.getAddedKeysAndValues();
                Map<String, Object> removedMap = message.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = message.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                                removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                                removedCount > 1 || //削除対象フィールドが複数の場合
                                updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : message.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        Object val = entry.getValue();
                        Object fixedVal = message.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    body = Helper.ModelHelper.toJson(bodyMap);

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPUT(
                            AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s?get=true", message.getCollectionID(), message.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    T obj = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), restResult.getData(), false);
                    ret.setData(obj);

                    return ret;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s/%s?get=true", message.getCollectionID(), message.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T deleted = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) message.clone(); //unsafe cast
                            copied.remove(field);
                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        Object fixedVal = message.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    body = Helper.ModelHelper.toJson(bodyMap);

                    //APIの実行
                    ABResult<Map<String, Object>> restResult = ABRestClient.syncPATCH(
                            AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s?get=true", message.getCollectionID(), message.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
                    );
                    ABResult<T> ret = new ABResult<>();
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    Map<String, Object> json = restResult.getData();
                    if (json != null) {
                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                        try {
                            T patched = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), json, false);
                            @SuppressWarnings("unchecked") T copied = (T) message.clone(); //unsafe cast
                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                copied.put(entry.getKey(), entry.getValue());
                            }
                            copied.apply();
                            ret.setData(copied);
                        } catch (CloneNotSupportedException ie) {
                            throw new ABException(ie);
                        }
                    }
                    return ret;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            return ret;
        }

        static <T extends ABPushMessage> void save(final T message) {
            save(message, null, EnumSet.of(AB.PushMessageSaveOption.NONE));
        }

        static <T extends ABPushMessage> void save(final T message, final AB.PushMessageSaveOption option) {
            save(message, null, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void save(final T message, final EnumSet<AB.PushMessageSaveOption> options) {
            save(message, null, options);
        }

        static <T extends ABPushMessage> void save(final T message, final ResultCallback<T> callback) {
            save(message, callback, EnumSet.of(AB.PushMessageSaveOption.NONE));
        }

        static <T extends ABPushMessage> void save(final T message, final ResultCallback<T> callback, final AB.PushMessageSaveOption option) {
            save(message, callback, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void save(final T message, final ResultCallback<T> callback, final EnumSet<AB.PushMessageSaveOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("message", message);
                validate("save", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            String body;
            Map<String, Object> bodyMap;
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(message);

            //未登録の場合
            if (message.isNew()) {

                //リクエストBODYの組み立て
                bodyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : message.entrySet()) {
                    String key = entry.getKey();
                    if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                        continue;
                    }
                    Object val = entry.getValue();
                    Object fixedVal = message.outputDataFilter(key, val);
                    bodyMap.put(key, fixedVal);
                }
                try {
                    body = Helper.ModelHelper.toJson(bodyMap);
                } catch (ABException e) {
                    executeResultCallbackIfNeeded(callback, null, e);
                    return;
                }

                //APIの実行
                ABRestClient.POST(
                        AB.baasPushAnalyticsAPIURLWithFormat("/%s?get=true", message.getCollectionID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                        body,
                        ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                        new ResultCallback<Map<String, Object>>() {
                            @Override
                            public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                if (e != null) {
                                    executeResultCallbackIfNeeded(callback, null, e);
                                    return;
                                }
                                ABResult<T> ret = new ABResult<>();
                                ret.setCode(restResult.getCode());
                                ret.setExtra(restResult.getExtra());
                                ret.setRawData(restResult.getRawData());
                                try {
                                    T obj = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), restResult.getData(), false);
                                    ret.setData(obj);
                                    executeResultCallbackIfNeeded(callback, ret, null);
                                } catch (ABException ie) {
                                    executeResultCallbackIfNeeded(callback, null, ie);
                                }
                            }
                        });
                return;
            }

            //更新の場合
            if (message.isDirty()) {

                Map<String, Object> addedMap   = message.getAddedKeysAndValues();
                Map<String, Object> removedMap = message.getRemovedKeysAndValues();
                Map<String, Object> updatedMap = message.getUpdatedKeysAndValues();
                int addedCount = addedMap.size();
                int removedCount = removedMap.size();
                int updatedCount = updatedMap.size();
                int totalCount = addedCount + removedCount + updatedCount;
                boolean isComplex = //複雑な更新かどうか (複雑な場合は置換(PUT)APIを選択する
                        addedCount > 0 || //追加フィールドが存在する場合
                                removedCount > totalCount || //フィールド削除以外の更新(追加/更新)が混在している場合
                                removedCount > 1 || //削除対象フィールドが複数の場合
                                updatedCount > totalCount; //フィールド更新以外の更新(追加/削除)が混在している場合

                //追加、削除、更新などが入り混じっている場合は置換(PUT)を使用する
                if (isComplex) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : message.entrySet()) {
                        String key = entry.getKey();
                        if ("_cts".equals(key) || "_cby".equals(key) || "_uts".equals(key) || "_uby".equals(key) || "_owner".equals(key)) {
                            continue;
                        }
                        if ("_id".equals(key)) { //FIXME: TEMPORARY : patch の場合 _id が含まれるとエラーになる
                            continue;            //FIXME: TEMPORARY
                        }                        //FIXME: TEMPORARY
                        Object val = entry.getValue();
                        Object fixedVal = message.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    try {
                        body = Helper.ModelHelper.toJson(bodyMap);
                    } catch (ABException e) {
                        executeResultCallbackIfNeeded(callback, null, e);
                        return;
                    }

                    //APIの実行
                    /* FIXME: TEMPORARY: コレクションの権限を"登録:ADMIN"に指定している場合、 PUT だと 403(Forbidden) が発生してしまうので暫定的に POST+proc=patch にする (2015.7.29 - ogawa)
                    ABRestClient.PUT(
                            AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s?get=true", message.getCollectionID(), message.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                    */
                    ABRestClient.POST(
                            AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s?proc=patch&get=true", message.getCollectionID(), message.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    try {
                                        T obj = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), restResult.getData(), false);
                                        ret.setData(obj);
                                        executeResultCallbackIfNeeded(callback, ret, null);
                                    } catch (ABException ie) {
                                        executeResultCallbackIfNeeded(callback, null, ie);
                                    }
                                }
                            });
                    return;
                }

                //値の削除だけの場合は DELETE を使用する
                if (removedCount == 1) {

                    //リクエストURLの組み立て
                    final String field = removedMap.entrySet().iterator().next().getKey();
                    String url = AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s/%s?get=true", message.getCollectionID(), message.getID(), field); //登録日時, 更新日時を取得したいので常にget=trueを指定する

                    //APIの実行
                    ABRestClient.DELETE(
                            url,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、_uby, _uts が返却される。 (※削除されたフィールドは返されない)
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T deleted = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) message.clone(); //unsafe cast
                                            copied.remove(field);
                                            for (Map.Entry<String, Object> entry : deleted.entrySet()) { //expect _uby, _uts
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            });
                    return;
                }

                //値の更新だけの場合は PATCH を使用する
                if (updatedCount > 0) {

                    //リクエストBODYの組み立て
                    bodyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : updatedMap.entrySet()) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        Object fixedVal = message.outputDataFilter(key, val);
                        bodyMap.put(key, fixedVal);
                    }
                    try {
                        body = Helper.ModelHelper.toJson(bodyMap);
                    } catch (ABException e) {
                        executeResultCallbackIfNeeded(callback, null, e);
                        return;
                    }

                    //APIの実行
                    ABRestClient.PATCH(
                            AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s?get=true", message.getCollectionID(), message.getID()), //登録日時, 更新日時を取得したいので常にget=trueを指定する
                            body,
                            ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                            new ResultCallback<Map<String, Object>>() {
                                @Override
                                public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                                    if (e != null) {
                                        executeResultCallbackIfNeeded(callback, null, e);
                                        return;
                                    }
                                    ABResult<T> ret = new ABResult<>();
                                    ret.setCode(restResult.getCode());
                                    ret.setExtra(restResult.getExtra());
                                    ret.setRawData(restResult.getRawData());
                                    Map<String, Object> json = restResult.getData();
                                    if (json != null) {
                                        //NOTE: get=true を指定しているので、更新されたフィールド群と _uby, _uts が返却される。
                                        //      差分のみレスポンスとして返されるので、元オブジェクトをコピー、差分を適用したオブジェクトをコールバックへ渡す。
                                        try {
                                            T patched = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), json, false);
                                            @SuppressWarnings("unchecked") T copied = (T) message.clone(); //unsafe cast
                                            for (Map.Entry<String, Object> entry : patched.entrySet()) {
                                                copied.put(entry.getKey(), entry.getValue());
                                            }
                                            copied.apply();
                                            ret.setData(copied);
                                            executeResultCallbackIfNeeded(callback, ret, null);
                                        } catch (ABException ie) {
                                            executeResultCallbackIfNeeded(callback, null, ie);
                                        } catch (CloneNotSupportedException ie) {
                                            executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                                        }
                                    }
                                }
                            });
                    return;
                }
            }

            //更新がない場合(total == 0)は 304 を返す
            ABResult<T> ret = new ABResult<>();
            ret.setCode(304); //(304: Not Modified)
            executeResultCallbackIfNeeded(callback, ret, null);
        }

//endregion

//region Save (Objects) ** 非公開 **

        static <T extends ABPushMessage> ABResult<List<T>> saveAllSynchronously(final List<T> messages) throws ABException {
            return saveAllSynchronously(messages, EnumSet.of(AB.PushMessageSaveOption.NONE));
        }

        static <T extends ABPushMessage> ABResult<List<T>> saveAllSynchronously(final List<T> messages, final AB.PushMessageSaveOption option) throws ABException {
            return saveAllSynchronously(messages, EnumSet.of(option));
        }

        static <T extends ABPushMessage> ABResult<List<T>> saveAllSynchronously(final List<T> messages, final EnumSet<AB.PushMessageSaveOption> options) throws ABException {
            ABResult<List<T>> ret = new ABResult<>();
            List<T> savedObjects = new ArrayList<>();
            Integer success = 0;
            for (T msg : messages) {
                ABResult<T> r = saveSynchronously(msg, options);
                if (r.getCode() >= 200 && r.getCode() <= 399) {
                    T saved = r.getData();
                    savedObjects.add(saved);
                    success++;
                    ret.setCode(r.getCode());
                }
            }
            ret.setTotal(success);
            ret.setData(savedObjects);
            return ret;
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages) {
            saveAll(messages, null, null, EnumSet.of(AB.PushMessageSaveOption.NONE));
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages, final AB.PushMessageSaveOption option) {
            saveAll(messages, null, null, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages, final EnumSet<AB.PushMessageSaveOption> options) {
            saveAll(messages, null, null, options);
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages, final ResultCallback<List<T>> callback) {
            saveAll(messages, callback, null, EnumSet.of(AB.PushMessageSaveOption.NONE));
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback) {
            saveAll(messages, callback, progressCallback, EnumSet.of(AB.PushMessageSaveOption.NONE));
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages, final ResultCallback<List<T>> callback, final AB.PushMessageSaveOption option) {
            saveAll(messages, callback, null, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages, final ResultCallback<List<T>> callback, final EnumSet<AB.PushMessageSaveOption> options) {
            saveAll(messages, callback, null, options);
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final AB.PushMessageSaveOption option) {
            saveAll(messages, callback, progressCallback, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void saveAll(final List<T> messages, final ResultCallback<List<T>> callback, final ProgressCallback progressCallback, final EnumSet<AB.PushMessageSaveOption> options) {
            new ABAsyncBatchExecutor(messages, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T msg = (T) target;
                    msg.save(new ResultCallback<T>() {
                        @Override
                        public void done(ABResult<T> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }

//endregion

//region Delete ** 非公開 **

        static <T extends ABPushMessage> ABResult<Void> deleteSynchronously(final T message) throws ABException {
            return deleteSynchronously(message, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static <T extends ABPushMessage> ABResult<Void> deleteSynchronously(final T message, final AB.PushMessageDeleteOption option) throws ABException {
            return deleteSynchronously(message, EnumSet.of(option));
        }

        static <T extends ABPushMessage> ABResult<Void> deleteSynchronously(final T message, final EnumSet<AB.PushMessageDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("message", message);
            validate("delete", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s", message.getCollectionID(), message.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());

            return ret;
        }

        static <T extends ABPushMessage> void delete(final T message) {
            delete(message, null, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static <T extends ABPushMessage> void delete(final T message, final AB.PushMessageDeleteOption option) {
            delete(message, null, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void delete(final T message, final EnumSet<AB.PushMessageDeleteOption> options) {
            delete(message, null, options);
        }

        static <T extends ABPushMessage> void delete(final T message, final ResultCallback<Void> callback) {
            delete(message, callback, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static <T extends ABPushMessage> void delete(final T message, final ResultCallback<Void> callback, final AB.PushMessageDeleteOption option) {
            delete(message, callback, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void delete(final T message, final ResultCallback<Void> callback, final EnumSet<AB.PushMessageDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("message", message);
                validate("delete", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s", message.getCollectionID(), message.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            executeResultCallbackIfNeeded(callback, ret, null);
                        }
                    });
        }

//endregion

//region Delete (Objects) ** 非公開 **

        static <T extends ABPushMessage> ABResult<Void> deleteAllSynchronously(final List<T> messages) throws ABException {
            return deleteAllSynchronously(messages, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static <T extends ABPushMessage> ABResult<Void> deleteAllSynchronously(final List<T> messages, final AB.PushMessageDeleteOption option) throws ABException {
            return deleteAllSynchronously(messages, EnumSet.of(option));
        }

        static <T extends ABPushMessage> ABResult<Void> deleteAllSynchronously(final List<T> messages, final EnumSet<AB.PushMessageDeleteOption> options) throws ABException {
            ABResult<Void> ret = new ABResult<>();
            Integer success = 0;
            for (T msg : messages) {
                ret = msg.deleteSynchronously(options);
                if (ret.getCode() >= 200 && ret.getCode() <= 399) {
                    success++;
                }
            }
            ret.setTotal(success);
            return ret;
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages) {
            deleteAll(messages, null, null, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages, final AB.PushMessageDeleteOption option) {
            deleteAll(messages, null, null, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages, final EnumSet<AB.PushMessageDeleteOption> options) {
            deleteAll(messages, null, null, options);
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages, final ResultCallback<Void> callback) {
            deleteAll(messages, callback, null, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages, final ResultCallback<Void> callback, final ProgressCallback progressCallback) {
            deleteAll(messages, callback, progressCallback, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages, final ResultCallback<Void> callback, final AB.PushMessageDeleteOption option) {
            deleteAll(messages, callback, null, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages, final ResultCallback<Void> callback, final EnumSet<AB.PushMessageDeleteOption> options) {
            deleteAll(messages, callback, null, options);
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final AB.PushMessageDeleteOption option) {
            deleteAll(messages, callback, progressCallback, EnumSet.of(option));
        }

        static <T extends ABPushMessage> void deleteAll(final List<T> messages, final ResultCallback<Void> callback, final ProgressCallback progressCallback, final EnumSet<AB.PushMessageDeleteOption> options) {
            new ABAsyncBatchExecutor(messages, callback, progressCallback){
                @Override
                void onProcess(ABModel target) {
                    T msg = (T) target;
                    msg.delete(new ResultCallback<Void>() {
                        @Override
                        public void done(ABResult<Void> result, ABException e) {
                            postProcess(result, e);
                        }
                    });
                }
            }.execute();
        }

//endregion

//region Delete (Query) ** 非公開 **

        static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final AB.PushMessageDeleteOption option) throws ABException {
            return deleteSynchronouslyWithQuery(query, EnumSet.of(option));
        }

        static ABResult<Void> deleteSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.PushMessageDeleteOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("deleteWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncDELETE(
                    AB.baasPushAnalyticsAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<Void> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }

        static void deleteWithQuery(final ABQuery query) {
            deleteWithQuery(query, null, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static void deleteWithQuery(final ABQuery query, final AB.PushMessageDeleteOption option) {
            deleteWithQuery(query, null, EnumSet.of(option));
        }

        static void deleteWithQuery(final ABQuery query, final EnumSet<AB.PushMessageDeleteOption> options) {
            deleteWithQuery(query, null, options);
        }

        static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback) {
            deleteWithQuery(query, callback, EnumSet.of(AB.PushMessageDeleteOption.NONE));
        }

        static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final AB.PushMessageDeleteOption option) {
            deleteWithQuery(query, callback, EnumSet.of(option));
        }

        static void deleteWithQuery(final ABQuery query, final ResultCallback<Void> callback, final EnumSet<AB.PushMessageDeleteOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("deleteWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.DELETE(
                    AB.baasPushAnalyticsAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<Void> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            try {
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (Exception ie) {
                                executeResultCallbackIfNeeded(callback, null, new ABException(ie));
                            }
                        }
                    });
        }

//endregion

//region Fetch ** 非公開 **

        static <T extends ABPushMessage> ABResult<T> fetchSynchronously(final T message) throws ABException {
            return fetchSynchronously(message, EnumSet.of(AB.PushMessageFetchOption.NONE));
        }

        static <T extends ABPushMessage> ABResult<T> fetchSynchronously(final T message, final AB.PushMessageFetchOption option) throws ABException {
            return fetchSynchronously(message, EnumSet.of(option));
        }

        static <T extends ABPushMessage> ABResult<T> fetchSynchronously(final T message, final EnumSet<AB.PushMessageFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("message", message);
            validate("fetch", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s", message.getCollectionID(), message.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<T> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(message);
            T obj = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), restResult.getData(), false);
            ret.setData(obj);

            return ret;
        }
        static <T extends ABPushMessage> void fetch(final T message, final ResultCallback<T> callback) {
            fetch(message, callback, EnumSet.of(AB.PushMessageFetchOption.NONE));
        }
        static <T extends ABPushMessage> void fetch(final T message, final ResultCallback<T> callback, final AB.PushMessageFetchOption option) {
            fetch(message, callback, EnumSet.of(option));
        }
        static <T extends ABPushMessage> void fetch(final T message, final ResultCallback<T> callback, final EnumSet<AB.PushMessageFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("message", message);
                validate("fetch", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasPushAnalyticsAPIURLWithFormat("/%s/%s", message.getCollectionID(), message.getID()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<T> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(message);
                                T obj = Helper.ModelHelper.toObject(clazz, message.getCollectionID(), restResult.getData(), false);
                                ret.setData(obj);
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Find (Query) ** 非公開 **

        static <T extends ABPushMessage> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query) throws ABException {
            return findSynchronouslyWithQuery(query, EnumSet.of(AB.PushMessageFetchOption.NONE));
        }

        static <T extends ABPushMessage> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final AB.PushMessageFetchOption option) throws ABException {
            return findSynchronouslyWithQuery(query ,EnumSet.of(option));
        }

        static <T extends ABPushMessage> ABResult<List<T>> findSynchronouslyWithQuery(final ABQuery query, final EnumSet<AB.PushMessageFetchOption> options) throws ABException {
            //バリデーション
            Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
            params.put("query", query);
            validate("findWithQuery", params);

            //APIの実行
            ABResult<Map<String, Object>> restResult = ABRestClient.syncGET(
                    AB.baasPushAnalyticsAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible()
            );
            ABResult<List<T>> ret = new ABResult<>();
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query, ABPushMessage.class);
            List<T> objects = Helper.ModelHelper.toObjects(clazz, query.getCollectionID(), restResult.getData(), false);
            ret.setData(objects);
            setPaginationInfo(ret, restResult.getData());

            return ret;
        }
        static <T extends ABPushMessage> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback) {
            findWithQuery(query, callback, EnumSet.of(AB.PushMessageFetchOption.NONE));
        }
        static <T extends ABPushMessage> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final AB.PushMessageFetchOption option) {
            findWithQuery(query, callback, EnumSet.of(option));
        }
        static <T extends ABPushMessage> void findWithQuery(final ABQuery query, final ResultCallback<List<T>> callback, final EnumSet<AB.PushMessageFetchOption> options) {
            //バリデーション
            try {
                Map<String, Object> params = new HashMap<String, Object>(){{ put("opts", options); }};
                params.put("query", query);
                validate("findWithQuery", params);
            } catch (ABException e) {
                executeResultCallbackIfNeeded(callback, null, e);
                return;
            }

            //APIの実行
            ABRestClient.GET(
                    AB.baasPushAnalyticsAPIURL(query.toString()),
                    ABRestClient.getDefaultHeadersWithStoreTokenIfPossible(),
                    new ResultCallback<Map<String, Object>>() {
                        @Override
                        public void done(ABResult<Map<String, Object>> restResult, ABException e) {
                            if (e != null) {
                                executeResultCallbackIfNeeded(callback, null, e);
                                return;
                            }
                            ABResult<List<T>> ret = new ABResult<>();
                            ret.setCode(restResult.getCode());
                            ret.setExtra(restResult.getExtra());
                            ret.setRawData(restResult.getRawData());
                            try {
                                @SuppressWarnings("unchecked") final Class<T> clazz = AB.ClassRepository.getBaaSClass(query, ABPushMessage.class);
                                List<T> objects = Helper.ModelHelper.toObjects(clazz, query.getCollectionID(), restResult.getData(), false);
                                ret.setData(objects);
                                setPaginationInfo(ret, restResult.getData());
                                executeResultCallbackIfNeeded(callback, ret, null);
                            } catch (ABException ie) {
                                executeResultCallbackIfNeeded(callback, null, ie);
                            }
                        }
                    });
        }

//endregion

//region Cancel

        /**
         * 実行中の API リクエストをキャンセルします。
         * @deprecated use {@link #cancelAll()} instead.
         */
        @Deprecated
        public static void cancel() {
            Pattern urlPattern = Pattern.compile("/push/");
            ABRestClient.cancel(urlPattern);
        }

        // XXX: 個々のリクエストをキャンセルするケースはまずないので実装は見送る
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
        // * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
        // */
        //public static <T extends ABPushMessage> void cancel(final T message) {
        //    cancel(message, EnumSet.of(AB.PushMessageCancelOption.NONE));
        //}
        //
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param message {@link ABDPushMessage} オブジェクト (またはその派生オブジェクト)
        // * @param option {@link AB.PushMessageCancelOption} オプション
        // * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
        // */
        //public static <T extends ABPushMessage> void cancel(final T message, final AB.PushMessageCancelOption option) {
        //    cancel(message, EnumSet.of(AB.PushMessageCancelOption.NONE));
        //}
        //
        ///**
        // * 実行中の API リクエストをキャンセルします。
        // * @param message {@link ABPushMessage} オブジェクト (またはその派生オブジェクト)
        // * @param options {@link AB.PushMessageCancelOption} オプション群
        // * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
        // */
        //public static <T extends ABPushMessage> void cancel(final T message, final EnumSet<AB.PushMessageCancelOption> options) {
        //    if (message == null || TextUtils.isEmpty(message.getCollectionID()) || TextUtils.isEmpty(message.getID())) return;
        //    String pattern = String.format("/push/%s/%s/%s/%s", AB.Config.getDatastoreID(),
        //            AB.Config.getApplicationID(), object.getCollectionID(), object.getID());
        //    Pattern urlPattern = Pattern.compile(pattern);
        //    ABRestClient.cancel(urlPattern);
        //}

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         */
        public static void cancelAll() {
            cancelAll(EnumSet.of(AB.PushMessageCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param option {@link AB.PushMessageCancelOption} オプション
         */
        public static void cancelAll(final AB.PushMessageCancelOption option) {
            cancelAll(EnumSet.of(AB.PushMessageCancelOption.NONE));
        }

        /**
         * 実行中のすべての API リクエストをキャンセルします。
         * @param options {@link AB.PushMessageCancelOption} オプション群
         */
        public static void cancelAll(final EnumSet<AB.PushMessageCancelOption> options) {
            String pattern = String.format("/push/%s/%s", AB.Config.getDatastoreID(), AB.Config.getApplicationID());
            Pattern urlPattern = Pattern.compile(pattern);
            ABRestClient.cancel(urlPattern);
        }

//endregion

//region Miscellaneous

        /**
         * プッシュ通知メッセージ・オブジェクト検索用のクエリオブジェクトを取得します。
         * @return {@link ABQuery} オブジェクト
         */
        public static ABQuery query() {
            return ABPushMessage.query();
        }

//endregion

//region Non-Public methods

        private static void setPaginationInfo(ABResult<?> result, Map<String, Object> json) {
            int total    = json.containsKey("_total") ? (Integer)json.get("_total") : 0;
            int start    = json.containsKey("_start") ? (Integer)json.get("_start") : 0;
            int end      = json.containsKey("_end")   ? (Integer)json.get("_end")   : 0;
            boolean next = json.containsKey("_next") && (Boolean)json.get("_next");
            boolean prev = json.containsKey("_prev") && (Boolean)json.get("_prev");
            result.setTotal(total);
            result.setStart(start);
            result.setEnd(end);
            result.setNext(next);
            result.setPrevious(prev);
        }

        private static <T> void executeResultCallbackIfNeeded(ResultCallback<T> callback, ABResult<T> result, ABException e) {
            if (callback != null) {
                callback.internalDone(result, e);
            }
        }

        private static void validate(String method, Map<String, Object> params) throws ABException {
            switch (method) {
                case "openMessage": {
                    ABPushMessage message = (ABPushMessage) params.get("message");
                    ABValidator.validate("message", message, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: message]");
                    }});
                    ABValidator.validate("pushId", message.getPushId(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: message.pushId]");
                    }});
                    ABDevice device = message.getDevice();
                    ABValidator.validate("device", device, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: device]");
                    }});
                    if (device != null) {
                        ABValidator.validate("deviceId", device.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                            put("msg", "パラメータが不足しています。 [不足パラメータ: device.ID]");
                        }});
                    }
                    break;
                }
                case "save": {
                    ABPushMessage message = (ABPushMessage) params.get("message");
                    ABValidator.validate("message", message, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: message]");
                    }});
                    break;
                }
                case "delete": {
                    ABPushMessage message = (ABPushMessage) params.get("message");
                    ABValidator.validate("message", message, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: message]");
                    }});
                    ABValidator.validate("ID", message.getID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: message.ID]");
                    }});
                    break;
                }
                case "deleteWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
                case "fetch": {
                    ABPushMessage message = (ABPushMessage) params.get("message");
                    ABValidator.validate("message", message, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: message]");
                    }});
                    break;
                }
                case "findWithQuery": {
                    ABQuery query = (ABQuery) params.get("query");
                    ABValidator.validate("query", query, ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query]");
                    }});
                    ABValidator.validate("from", query.getCollectionID(), ABValidator.ValidationRule.REQUIRED, new HashMap<String, Object>() {{
                        put("msg", "パラメータが不足しています。 [不足パラメータ: query.from]");
                    }});
                    break;
                }
            }
        }

        private static int getAppIconID(Context context) {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getApplicationContext().getPackageName();
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                return appInfo.icon;
            } catch (PackageManager.NameNotFoundException e) {
                return -1;
            }
        }

//endregion

    }

    /**
     * Twitter サービス。
     *
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=11373">アピアリーズドキュメント &raquo; SNS連携</a>
     */
    public static class TwitterService {

        private static String TAG = TwitterService.class.getSimpleName();

        private static ABTwitterAuthenticationProvider sProvider;
        private static boolean sActivated;
        private static ABTwitter sTwitter;

//region Initialization

        /*
         * TwitterService を初期化します。
         * <div class="important">本クラスの各メソッドを使用する前に必ず実行してください。</div>
         */
        static boolean activate() {
            verifySdkActivation();
            sTwitter = new ABTwitter(AB.Config.Twitter.getConsumerKey(), AB.Config.Twitter.getConsumerSecret());
            sProvider = new ABTwitterAuthenticationProvider(AB.sApplicationContext, sTwitter);
            AB.UserService.registerAuthenticationProvider(sProvider);
            sActivated = true;
            return sActivated;
        }

//endregion

//region Log-In

        /**
         * Twitterアカウントを使用してアプリにログインします。
         * <p>{@link AB.Config.Twitter} の設定情報を使用して Twitter へ認証を行い、アプリにログインします。</p>
         * @param context コンテキスト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Context context, final ResultCallback<T> callback) {
            logIn(context, callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * Twitterアカウントを使用してアプリにログインします。
         * <p>{@link AB.Config.Twitter} の設定情報を使用して Twitter へ認証を行い、アプリにログインします。</p>
         * @param context コンテキスト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Context context, final ResultCallback<T> callback, final AB.UserLogInOption option) {
            logIn(context, callback, EnumSet.of(option));
        }

        /**
         * Twitterアカウントを使用してアプリにログインします。
         * <p>{@link AB.Config.Twitter} の設定情報を使用して Twitter へ認証を行い、アプリにログインします。</p>
         * @param context コンテキスト
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Context context, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            verifyClassInitialization();

            sProvider.setContext(context);

            AB.UserService.logInAuthenticate(sProvider.getId(), new ABAuthenticationProvider.AuthenticationCallback() {

                @Override
                public void onSuccess(Map<String, Object> authData) {
                    AB.UserService.logInAuthenticateEnd(sProvider.getId(), authData, callback, options);
                }

                @Override
                public void onError(Throwable e) {
                    if (callback != null) {
                        callback.internalDone(null, new ABException(e));
                    }
                }

                @Override
                public void onCancel() {
                    ABLog.w("TwitterLogInConnection", "Authentication Canceled.");
                    if (callback != null) {
                        callback.internalDone(null, new ABException(ABStatus.OPERATION_CANCELLED));
                    }
                }
            });
        }

        /**
         * Twitterアカウントを使用してアプリにログインします。
         * <p>Twitter アカウントの各種情報を使用してアプリにログインします。</p>
         * @param twitterId       Twitter ID
         * @param screenName      スクリーンネーム
         * @param authToken       OAuth認証トークン
         * @param authTokenSecret OAuth認証トークン・シークレット
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final String twitterId, final String screenName, final String authToken, final String authTokenSecret, final ResultCallback<T> callback) {
            logIn(twitterId, screenName, authToken, authTokenSecret, callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * Twitterアカウントを使用してアプリにログインします。
         * <p>Twitter アカウントの各種情報を使用してアプリにログインします。</p>
         * @param twitterId       Twitter ID
         * @param screenName      スクリーンネーム
         * @param authToken       OAuth認証トークン
         * @param authTokenSecret OAuth認証トークン・シークレット
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final String twitterId, final String screenName, final String authToken, final String authTokenSecret, final ResultCallback<T> callback, final AB.UserLogInOption option) {
            logIn(twitterId, screenName, authToken, authTokenSecret, callback, EnumSet.of(option));
        }

        /**
         * Twitterアカウントを使用してアプリにログインします。
         * <p>Twitter アカウントの各種情報を使用してアプリにログインします。</p>
         * @param twitterId       Twitter ID
         * @param screenName      スクリーンネーム
         * @param authToken       OAuth認証トークン
         * @param authTokenSecret OAuth認証トークン・シークレット
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final String twitterId, final String screenName, final String authToken, final String authTokenSecret, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            try {
                Map<String, Object> authData = sProvider.getAuthData(twitterId, screenName, authToken, authTokenSecret);
                AB.UserService.logInAuthenticateEnd(sProvider.getId(), authData, callback, options);
            } catch (JSONException e) {
                if (callback != null) {
                    callback.internalDone(null, new ABException(e));
                }
            }
        }

//endregion

//region Non-Public methods

        /*
         * AB#activate(context) が実行済みかどうかを検証する。
         * @throws IllegalStateException AB#activate(context) が未実行の場合にスロー
         */
        private static void verifySdkActivation() throws IllegalStateException {
            if (AB.sApplicationContext == null) {
                throw new IllegalStateException("APIS is not yet initialized.");
            }
        }

        /*
         * AB.TwitterService#activate(context) が実行済みかどうかを検証する。
         * @throws IllegalStateException AB.TwitterService#activate(context) が未実行の場合にスロー
         */
        private static void verifyClassInitialization() throws IllegalStateException {
            if (!sActivated) {
                throw new IllegalStateException("TwitterService is not yet initialized.");
            }
        }

//endregion

    }

    /**
     * Facebook サービス。
     *
     * @version 2.0.0
     * @since 2.0.0
     * @see <a href="http://docs.appiaries.com/?p=11373">アピアリーズドキュメント &raquo; SNS連携</a>
     */
    public static class FacebookService {

        private static String TAG = FacebookService.class.getSimpleName();

        private static ABFacebookAuthenticationProvider sProvider;
        private static boolean sActivated;

//region Initialization

        /*
         * FacebookService を初期化します。
         * <div class="important">本クラスの各メソッドを使用する前に必ず実行してください。</div>
         */
        static boolean activate() {
            verifySdkActivation();
            Context context = AB.sApplicationContext;
            try {
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = appInfo.metaData;
                String metaKey = "com.facebook.sdk.ApplicationId";
                String facebookAppId = bundle.getString(metaKey);
                if (facebookAppId == null) {
                    throw new IllegalStateException("meta-data definition (name='" + metaKey + "') not found in AndroidManifest.xml. (see https://developers.facebook.com/docs/android/getting-started)");
                }
                sProvider = new ABFacebookAuthenticationProvider(context, facebookAppId);
                AB.UserService.registerAuthenticationProvider(sProvider);
                sActivated = true;
            } catch (Exception e) {
                ABLog.e(TAG, "Failed to activate 'AB.FacebookService'.", e);
                throw new RuntimeException(e);
            }
            return sActivated;
        }

//endregion

//region Log-In

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param activity アクティビティ
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Activity activity, final ResultCallback<T> callback) {
            logIn(activity, callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param activity アクティビティ
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Activity activity, final ResultCallback<T> callback, final AB.UserLogInOption option) {
            logIn(activity, callback, EnumSet.of(option));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param activity アクティビティ
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Activity activity, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            Collection<String> permissions = Collections.emptyList();
            logIn(permissions, activity, com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE, callback, options);
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param activity アクティビティ
         * @param activityCode アクティビティを識別するための識別コード（デフォルト値は com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE）
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Activity activity, final int activityCode, final ResultCallback<T> callback) {
            logIn(activity, activityCode, callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param activity アクティビティ
         * @param activityCode アクティビティを識別するための識別コード（デフォルト値は com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE）
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Activity activity, final int activityCode, final ResultCallback<T> callback, final AB.UserLogInOption option) {
            logIn(activity, activityCode, callback, EnumSet.of(option));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param activity アクティビティ
         * @param activityCode アクティビティを識別するための識別コード（デフォルト値は com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE）
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Activity activity, final int activityCode, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            Collection<String> permissions = Collections.emptyList();
            logIn(permissions, activity, activityCode, callback, options);
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param permissions Facebookログイン時に、Facebookアカウントに対してアクセス許可を求めるパーミッションのリスト
         * @param activity アクティビティ
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Collection<String> permissions, final Activity activity, final ResultCallback<T> callback) {
            logIn(permissions, activity, callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param permissions Facebookログイン時に、Facebookアカウントに対してアクセス許可を求めるパーミッションのリスト
         * @param activity アクティビティ
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Collection<String> permissions, final Activity activity, final ResultCallback<T> callback, final AB.UserLogInOption option) {
            logIn(permissions, activity, callback, EnumSet.of(option));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param permissions Facebookログイン時に、Facebookアカウントに対してアクセス許可を求めるパーミッションのリスト
         * @param activity アクティビティ
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Collection<String> permissions, final Activity activity, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            logIn(permissions, activity, com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE, callback, options);
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param permissions Facebookログイン時に、Facebookアカウントに対してアクセス許可を求めるパーミッションのリスト
         * @param activity アクティビティ
         * @param activityCode アクティビティを識別するための識別コード（デフォルト値は com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE）
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Collection<String> permissions, final Activity activity, final int activityCode, final ResultCallback<T> callback) {
            logIn(permissions, activity, activityCode, callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param permissions Facebookログイン時に、Facebookアカウントに対してアクセス許可を求めるパーミッションのリスト
         * @param activity アクティビティ
         * @param activityCode アクティビティを識別するための識別コード（デフォルト値は com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE）
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Collection<String> permissions, final Activity activity, final int activityCode, final ResultCallback<T> callback, final AB.UserLogInOption option) {
            logIn(permissions, activity, activityCode, callback, EnumSet.of(option));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param permissions Facebookログイン時に、Facebookアカウントに対してアクセス許可を求めるパーミッションのリスト
         * @param activity アクティビティ
         * @param activityCode アクティビティを識別するための識別コード（デフォルト値は com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE）
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final Collection<String> permissions, final Activity activity, final int activityCode, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            verifyClassInitialization();
            sProvider.setActivity(activity);
            sProvider.setActivityCode(activityCode);
            if (permissions == null) {
                sProvider.setPermissions(Collections.<String>emptyList());
            } else {
                sProvider.setPermissions(permissions);
            }

            FacebookLogInConnection loginConnection = new FacebookLogInConnection();
            loginConnection.setCallback(callback, null, options);
            loginConnection.execute();
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param facebookId     FacebookアカウントのID
         * @param accessToken    Facebookから発行されたアクセストークン
         * @param expirationDate Facebookから発行されたアクセストークンの有効期限
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final String facebookId, final String accessToken, final Date expirationDate, final ResultCallback<T> callback) {
            logIn(facebookId, accessToken, expirationDate, callback, EnumSet.of(AB.UserLogInOption.NONE));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param facebookId     FacebookアカウントのID
         * @param accessToken    Facebookから発行されたアクセストークン
         * @param expirationDate Facebookから発行されたアクセストークンの有効期限
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param option {@link AB.UserLogInOption} オプション
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final String facebookId, final String accessToken, final Date expirationDate, final ResultCallback<T> callback, final AB.UserLogInOption option) {
            logIn(facebookId, accessToken, expirationDate, callback, EnumSet.of(option));
        }

        /**
         * Facebookアカウントを使用してアプリにログインします。
         * @param facebookId     FacebookアカウントのID
         * @param accessToken    Facebookから発行されたアクセストークン
         * @param expirationDate Facebookから発行されたアクセストークンの有効期限
         * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
         * @param options {@link AB.UserLogInOption} オプション群
         * @param <T> {@link ABUser} クラス (またはその派生クラス)
         */
        public static <T extends ABUser> void logIn(final String facebookId, final String accessToken, final Date expirationDate, final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
            verifyClassInitialization();
            Map<String, Object> authData = new HashMap<String, Object>();
            try {
                authData = sProvider.getAuthData(facebookId, accessToken, expirationDate);
            } catch (JSONException e) {
                ABLog.w(TAG, e);
            }

            FacebookLogInConnection logInConnection = new FacebookLogInConnection();
            logInConnection.setCallback(callback, authData, options);
            logInConnection.execute();
        }

        /**
         * Facebook認証完了後処理を実行します。
         * <p>呼び出し元 Activity の onActivityResult() 内で実行し、Facebook との認証処理を完了させます。</p>
         * @param requestCode リクエストコード
         * @param resultCode  結果コード
         * @param intent      インテント
         * @see <a href="https://developers.facebook.com/docs/reference/android/3.5/class/Session/#onActivityResult">Facebook Developers - Sesion#onActivityResult(currentActivity, requestCode, resultCode, data)</a>
         */
        public static void finishAuthentication(final int requestCode, final int resultCode, final Intent intent) {
            if (sProvider != null) {
                sProvider.onActivityResult(requestCode, resultCode, intent);
            }
        }

//endregion

//region Miscellaneous

        /**
         * Facebook SDK の Session を取得します。
         * @return com.facebook.Session
         */
        public static com.facebook.Session getSession() {
            verifyClassInitialization();
            return sProvider.getSession();
        }

//endregion

//region Inner Class

        private static class FacebookLogInConnection<T extends ABUser> extends AsyncTask<Void, Void, Void> {

            private ResultCallback<T> callback;
            private Map<String, Object> authData;
            private EnumSet<AB.UserLogInOption> options;

            @Override
            protected Void doInBackground(Void... params) {
                if (authData == null) {
                    ABAuthenticationProvider.AuthenticationCallback authCallback =
                            new ABAuthenticationProvider.AuthenticationCallback() {
                                @Override
                                public void onSuccess(Map<String, Object> pAuthData) {
                                    AB.UserService.logInAuthenticateEnd(sProvider.getId(), pAuthData, callback, options);
                                }

                                @Override
                                public void onCancel() {
                                    ABLog.w("FacebookLogInConnection", "Authentication Canceled.");
                                }

                                @Override
                                public void onError(Throwable paramThrowable) {
                                    if ("Operation canceled".equals(paramThrowable.getMessage())) { //XXX:
                                        ABLog.w("FacebookLogInConnection", "Authentication Canceled.");
                                        return;
                                    }
                                    if (callback != null) {
                                        callback.internalDone(null, new ABException(paramThrowable));
                                    }
                                }
                            };
                    AB.UserService.logInAuthenticate(sProvider.getId(), authCallback);
                } else {
                    AB.UserService.logInAuthenticateEnd(sProvider.getId(), authData, callback, options);
                }
                return null;
            }

            //see http://stackoverflow.com/questions/20455644/object-cannot-be-cast-to-void-in-asynctask
            public void execute() {
                super.execute();
            }

            private void setCallback(ResultCallback<T> callback, Map<String, Object> authData, EnumSet<AB.UserLogInOption> options) {
                this.callback = callback;
                this.authData = authData;
                this.options  = options;
            }
        }

//endregion

//region Non-Public methods

        /*
         * AB#activate(context) が実行済みであるかどうかを検証する。
         * @throws IllegalStateException AB#activate(context) が未実行の場合にスロー
         */
        private static void verifySdkActivation() throws IllegalStateException {
            if (AB.sApplicationContext == null) {
                throw new IllegalStateException("APIS is not yet initialized.");
            }
        }

        /*
         * AB.FacebookService#activate(context) が実行済みであるかどうかを検証する。
         * @throws IllegalStateException AB.FacebookService#activate(context) が未実行の場合にスロー
         */
        private static void verifyClassInitialization() throws IllegalStateException {
            if (!sActivated) {
                throw new IllegalStateException("FacebookService is not yet initialized.");
            }
        }

//endregion

    }

    /**
     * ヘルパー。
     */
    public static class Helper {

        static class DateHelper {

            public static Date convert(Object value) {
                if (value instanceof Long) {
                    return new Date((long)value);
                }
                return null;
            }

        }

        static class OptionHelper {

            public static EnumSet<AB.DBObjectFetchOption> convertToDBObjectFetchOption(EnumSet<AB.DBObjectRefreshOption> options) {
                Set<AB.DBObjectFetchOption> set = new HashSet<>();
                if (options.contains(AB.DBObjectRefreshOption.NONE)) {
                    set.add(AB.DBObjectFetchOption.NONE);
                } else {
                    set.add(AB.DBObjectFetchOption.NONE);
                }
                return EnumSet.copyOf(set);
            }

            public static EnumSet<AB.FileFetchOption> convertToFileFetchOption(EnumSet<AB.FileRefreshOption> options) {
                Set<AB.FileFetchOption> set = new HashSet<>();
                if (options.contains(AB.FileRefreshOption.NONE)) {
                    set.add(AB.FileFetchOption.NONE);
                } else {
                    set.add(AB.FileFetchOption.NONE);
                }
                return EnumSet.copyOf(set);
            }

            public static EnumSet<AB.UserFetchOption> convertToUserFetchOption(EnumSet<AB.UserRefreshOption> options) {
                Set<AB.UserFetchOption> set = new HashSet<>();
                if (options.contains(AB.UserRefreshOption.NONE)) {
                    set.add(AB.UserFetchOption.NONE);
                } else {
                    set.add(AB.UserFetchOption.NONE);
                }
                return EnumSet.copyOf(set);
            }

            //** 非公開 **
            static EnumSet<AB.DeviceFetchOption> convertToDeviceFetchOption(EnumSet<AB.DeviceRefreshOption> options) {
                Set<AB.DeviceFetchOption> set = new HashSet<>();
                if (options.contains(AB.DeviceRefreshOption.NONE)) {
                    set.add(AB.DeviceFetchOption.NONE);
                } else {
                    set.add(AB.DeviceFetchOption.NONE);
                }
                return EnumSet.copyOf(set);
            }

            //** 非公開 **
            static EnumSet<AB.PushMessageFetchOption> convertToPushMessageFetchOption(EnumSet<AB.PushMessageRefreshOption> options) {
                Set<AB.PushMessageFetchOption> set = new HashSet<>();
                if (options.contains(AB.PushMessageRefreshOption.NONE)) {
                    set.add(AB.PushMessageFetchOption.NONE);
                } else {
                    set.add(AB.PushMessageFetchOption.NONE);
                }
                return EnumSet.copyOf(set);
            }

        }

        /**
         * モデル・ヘルパー。
         */
        public static class ModelHelper {

            /**
             * Mapデータからモデル・インスタンスを生成します。
             * @param clazz インスタンスを生成する {@link ABModel} クラスの派生クラス
             * @param jsonMap JSONデータ
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return {@link ABModel} クラスの派生オブジェクト
             * @throws ABException インスタンス生成に失敗した場合にスロー
             * @deprecated use {@link #toObject(Class, String, Map)} instead.
             */
            public static <T extends ABModel> T toObject(Class<T> clazz, Map<String, Object> jsonMap) throws ABException {
                return toObject(clazz, null, jsonMap, false);
            }

            /**
             * Mapデータからモデル・インスタンスを生成します。
             * @param clazz インスタンスを生成する {@link ABModel} クラスの派生クラス
             * @param collectionID 非ユーザ定義クラスの場合にオブジェクトにセットするコレクションID
             * @param jsonMap JSONデータ
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return {@link ABModel} クラスの派生オブジェクト
             * @throws ABException インスタンス生成に失敗した場合にスロー
             */
            public static <T extends ABModel> T toObject(Class<T> clazz, String collectionID, Map<String, Object> jsonMap) throws ABException {
                return toObject(clazz, collectionID, jsonMap, false);
            }

            /**
             * Mapデータからモデル・インスタンスを生成します。
             * @param clazz インスタンスを生成する {@link ABModel} クラスの派生クラス
             * @param jsonMap JSONデータ
             * @param isNew isNewフラグ
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return {@link ABModel} クラスの派生オブジェクト
             * @throws ABException インスタンス生成に失敗した場合にスロー
             * @deprecated use {@link #toObject(Class, String, Map, boolean)} instead.
             */
            public static <T extends ABModel> T toObject(Class<T> clazz, Map<String, Object> jsonMap, boolean isNew) throws ABException {
                return toObject(clazz, null, jsonMap, isNew);
            }

            /**
             * Mapデータからモデル・インスタンスを生成します。
             * @param clazz インスタンスを生成する {@link ABModel} クラスの派生クラス
             * @param collectionID 非ユーザ定義クラスの場合にオブジェクトにセットするコレクションID
             * @param jsonMap JSONデータ
             * @param isNew isNewフラグ
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return {@link ABModel} クラスの派生オブジェクト
             * @throws ABException インスタンス生成に失敗した場合にスロー
             */
            public static <T extends ABModel> T toObject(Class<T> clazz, String collectionID, Map<String, Object> jsonMap, boolean isNew) throws ABException {
                T instance = null;
                try {
                    //instance = clazz.getConstructor(Map.class).newInstance(jsonMap);
                    instance = clazz.newInstance();
                    //instance.setEstimatedData(jsonMap);
                    instance.putAll(jsonMap.entrySet());
                    instance.apply(); //いらないかも
                    instance.setNew(isNew);
                    if (collectionID != null && collectionID.length() > 0) {
                        instance.setCollectionID(collectionID);
                    }
                } catch (InstantiationException e) {
                    ABLog.e(TAG, e.getMessage());
                } catch (IllegalAccessException e) {
                    ABLog.e(TAG, e.getMessage());
                }
                return instance;
            }

            /**
             * Mapデータから複数のモデル・インスタンスを生成します。
             * <div class="important">トップレベルのキーは "_objs" である必要があります。</div>
             * @param clazz インスタンスを生成する {@link ABModel} クラスの派生クラス
             * @param jsonMap JSONデータ
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return {@link ABModel} クラスの派生オブジェクト
             * @throws ABException インスタンス生成に失敗した場合にスロー
             * @deprecated use {@link #toObjects(Class, String, Map)} instead.
             */
            public static <T extends ABModel> List<T> toObjects(Class<T> clazz, Map<String, Object> jsonMap) throws ABException {
                return toObjects(clazz, null, jsonMap, false);
            }

            /**
             * Mapデータから複数のモデル・インスタンスを生成します。
             * <div class="important">トップレベルのキーは "_objs" である必要があります。</div>
             * @param clazz インスタンスを生成する {@link ABModel} クラスの派生クラス
             * @param collectionID 非ユーザ定義クラスの場合にオブジェクトにセットするコレクションID
             * @param jsonMap JSONデータ
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return {@link ABModel} クラスの派生オブジェクト
             * @throws ABException インスタンス生成に失敗した場合にスロー
             */
            public static <T extends ABModel> List<T> toObjects(Class<T> clazz, String collectionID, Map<String, Object> jsonMap) throws ABException {
                return toObjects(clazz, collectionID, jsonMap, false);
            }

            /**
             * Mapデータから複数のモデル・インスタンスを生成します。
             * <div class="important">トップレベルのキーは "_objs" である必要があります。</div>
             * @param clazz インスタンスを生成する {@link ABModel} クラスの派生クラス
             * @param jsonMap JSONデータ
             * @param isNew isNewフラグ
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return {@link ABModel} クラスの派生オブジェクト
             * @throws ABException インスタンス生成に失敗した場合にスロー
             * @deprecated use {@link #toObjects(Class, String, Map, boolean)} instead.
             */
            public static <T extends ABModel> List<T> toObjects(Class<T> clazz, Map<String, Object> jsonMap, boolean isNew) throws ABException {
                return toObjects(clazz, null, jsonMap, isNew);
            }

            /**
             * Mapデータから複数のモデル・インスタンスを生成します。
             * <div class="important">トップレベルのキーは "_objs" である必要があります。</div>
             * @param clazz インスタンスを生成する {@link ABModel} クラスの派生クラス
             * @param collectionID 非ユーザ定義クラスの場合にオブジェクトにセットするコレクションID
             * @param jsonMap JSONデータ
             * @param isNew isNewフラグ
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return {@link ABModel} クラスの派生オブジェクト
             * @throws ABException インスタンス生成に失敗した場合にスロー
             */
            public static <T extends ABModel> List<T> toObjects(Class<T> clazz,String collectionID, Map<String, Object> jsonMap, boolean isNew) throws ABException {
                if (!jsonMap.containsKey("_objs")) return null;

                List<T> results = new ArrayList<T>();
                @SuppressWarnings("unchecked")List<Map<String, Object>> itemsJsonMap = (List<Map<String, Object>>)jsonMap.get("_objs");
                for (Map<String, Object> map : itemsJsonMap) {
                    T instance = toObject(clazz, collectionID, map);
                    if (instance != null) {
                        results.add(instance);
                    }
                }
                return results.size() > 0 ? results : new ArrayList<T>();
            }

            /**
             * MapデータからJSON文字列を生成します。
             * @param jsonMap JSONデータ
             * @return JSON形式の文字列
             * @throws ABException JSON文字列の生成に失敗した場合にスロー
             */
            public static String toJson(Map<String, Object> jsonMap) throws ABException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.getSerializationConfig().set(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
                try {
                    return mapper.writeValueAsString(jsonMap);
                } catch (IOException e) {
                    throw new ABException(e);
                }
            }

            /**
             * モデル・オブジェクトからJSON文字列を生成します。
             * @param object {@link ABModel} クラスの派生クラス
             * @param <T> {@link ABModel} クラスの派生クラス
             * @return JSON形式の文字列
             * @throws ABException JSON文字列の生成に失敗した場合にスロー
             */
            public static <T extends ABModel> String toJson(T object) throws ABException {
                //see http://stackoverflow.com/questions/21720759/convert-a-json-string-to-a-hashmap

                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, Object> map = object.getFilteredEstimatedData();
                    return mapper.writeValueAsString(map);
                } catch (IOException e) {
                    throw new ABException(e);
                }
                /*
                StringBuilder buff = new StringBuilder();
                Map<String, Object> map = object.getEstimatedData();
                for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
                    Map.Entry entry = (Map.Entry)itr.next();
                    String key = (String)entry.getKey();
                    Object value = entry.getValue();
                    Object fixedValue = object.outputDataFilter(key, value);
                    buff.append((buff.length() == 0) ? "{" : ",");
                    if (fixedValue instanceof String) {
                        buff.append("'").append(key).append("':'").append(fixedValue).append("'");
                    } else if (fixedValue instanceof Number) {
                        buff.append("'").append(key).append("':").append(fixedValue);
                    } else if (fixedValue instanceof Date) {
                        buff.append("'").append(key).append("':").append(((Date)fixedValue).getTime()); //TO DO:
                    } else {
                        buff.append("'").append(key).append("':'").append(fixedValue).append("'");
                    }
                }
                if (buff.length() > 0) {
                    buff.append("}");
                }
                return buff.toString();
                */
            }

            /**
             * JSONObject を Map に変換します。
             * @param json org.json.JSONObject オブジェクト
             * @return Map
             * @throws JSONException
             */
            public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
                Map<String, Object> retMap = new HashMap<>();

                if(json != JSONObject.NULL) {
                    retMap = toMap(json);
                }
                return retMap;
            }

            /*
             * JSONObject を Map に変換します。
             * @param object org.json.JSONObject オブジェクト
             * @return Map
             * @throws JSONException
             */
            static Map<String, Object> toMap(JSONObject object) throws JSONException {
                Map<String, Object> map = new HashMap<>();

                Iterator<String> keysItr = object.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    Object value = object.get(key);

                    if(value instanceof JSONArray) {
                        value = toList((JSONArray) value);
                    }

                    else if(value instanceof JSONObject) {
                        value = toMap((JSONObject) value);
                    }
                    map.put(key, value);
                }
                return map;
            }

            /*
             * JSONArray を List に変換します
             * @param array JSONArray
             * @return List
             * @throws JSONException
             */
            static List<Object> toList(JSONArray array) throws JSONException {
                List<Object> list = new ArrayList<>();
                for(int i = 0; i < array.length(); i++) {
                    Object value = array.get(i);
                    if(value instanceof JSONArray) {
                        value = toList((JSONArray) value);
                    } else if(value instanceof JSONObject) {
                        value = toMap((JSONObject) value);
                    }
                    list.add(value);
                }
                return list;
            }

        }

        static final class StreamHelper {
            private static String TAG = AB.Helper.StreamHelper.class.getSimpleName();

            public static byte[] getBytesFromInputStream(InputStream inputStream) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                OutputStream os = new BufferedOutputStream(b);
                int c;
                try {
                    while ((c = inputStream.read()) != -1) {
                        os.write(c);
                    }
                } catch (IOException e) {
                    ABLog.e(TAG, e.getMessage());
                } finally {
                    try {
                        os.flush();
                        os.close();
                    } catch (IOException e) {
                        ABLog.e(TAG, e.getMessage());
                    }
                }
                return b.toByteArray();
            }

        }
    }

}
