//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プッシュ通知メッセージ・モデル。
 * <p>プッシュ通知メッセージを表現するモデルクラスです。</p>
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="http://docs.appiaries.com/?p=130">アピアリーズドキュメント &raquo; プッシュ通知</a>
 */
@SuppressWarnings("unused")
@ABCollection("com.appiaries.baas.sdk.ABPushMessage")
public class ABPushMessage extends ABModel implements Serializable {

    private static final String TAG = ABPushMessage.class.getSimpleName();

    private static final long serialVersionUID = -6980204040444415299L;

//region Fields

    /**
     * プッシュ通知メッセージ・フィールド。
     * <p>プッシュ通知メッセージ・モデルが持つフィールドの定数を保持します。</p>
     */
    public static class Field extends ABModel.Field {
        public static final ABField TITLE   = new ABField(AB.EXTRA_KEY_TITLE, String.class);
        public static final ABField MESSAGE = new ABField(AB.EXTRA_KEY_MESSAGE, String.class);
        public static final ABField PUSH_ID = new ABField(AB.EXTRA_KEY_PUSH_ID, long.class);
        public static final ABField URL     = new ABField(AB.EXTRA_KEY_URL, String.class);
        public static final ABField FROM    = new ABField(AB.EXTRA_KEY_FROM, long.class);
        public static final ABField COLLAPSE_KEY = new ABField(AB.EXTRA_KEY_COLLAPSE_KEY, String.class);
        public static final ABField TIME_TO_LIVE = new ABField(AB.EXTRA_KEY_TIME_TO_LIVE, int.class);
        public static final ABField DELAY_WHILE_IDLE = new ABField(AB.EXTRA_KEY_DELAY_WHILE_IDLE, boolean.class);
        public static final ABField RESTRICTED_PACKAGE_NAME = new ABField(AB.EXTRA_KEY_RESTRICTED_PACKAGE_NAME, String.class);
        public static final ABField DRY_RUN = new ABField(AB.EXTRA_KEY_DRY_RUN, boolean.class);
        public static final ABField REGISTRATION_IDS = new ABField(AB.EXTRA_KEY_REGISTRATION_IDS, List.class);
    }

//endregion

    private ABDevice mDevice;

//region Initialization

    /**
     * デフォルト・コンストラクタ。
     */
    public ABPushMessage() {
        super();
    }

    /**
     * コンストラクタ。
     * <p>引数にコレクションIDを取ります。</p>
     * @param collectionID コレクションID
     */
    public ABPushMessage(String collectionID) {
        super(collectionID);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param collectionID コレクションID
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABPushMessage(String collectionID, Map<String, Object> map) {
        super(collectionID, map);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABPushMessage(Map<String, Object> map) {
        super();
        setEstimatedData(map);
        setOriginalData(map);
        apply();
        mNew = true;
    }

    /**
     * コンストラクタ。
     * <p>引数に GCM から受け取ったプッシュ通知の Intent を取ります。</p>
     * @param intent GCM から受け取ったプッシュ通知の Intent
     */
    public ABPushMessage(Intent intent) {
        if (intent == null) return;

        Bundle extras = intent.getExtras();

        if (extras != null && !extras.isEmpty()) {

            //NOTE: extras 内の数値関連データは、GCMから受け取る際、すべて文字列で渡される。
            //      "key=<String>, value=<String>" みたいな GCM の制約があった気がするけれど、
            //      手動でIntentを組み立てるケースに備え、文字列型以外に数値型も格納できるようにしている。

            //>> message
            String message = extras.getString(AB.EXTRA_KEY_MESSAGE);
            //>> pushId
            long pushId = 0L;
            Object pushIdObj = extras.get(AB.EXTRA_KEY_PUSH_ID);
            if (pushIdObj != null) {
                if (pushIdObj instanceof String) {
                    pushId = Long.parseLong((String) pushIdObj);
                } else { //expect long
                    pushId = (long) pushIdObj;
                }
            }
            String title = extras.getString(AB.EXTRA_KEY_TITLE);
            String url = extras.getString(AB.EXTRA_KEY_URL);
            //>> from
            long from = 0L;
            Object fromObj = extras.get(AB.EXTRA_KEY_FROM);
            if (fromObj != null) {
                if (fromObj instanceof String) {
                    try {
                        from = Long.parseLong((String) fromObj);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, String.format("Ignore non-numeric from value. [from=%s]", from));
                    }
                } else { //expect long
                    from = (long) fromObj;
                }
            }
            //>> collapseKey
            String collapseKey = extras.getString(AB.EXTRA_KEY_COLLAPSE_KEY);
            //>> timeToLive
            int timeToLive = 0;
            Object timeToLiveObj = extras.get(AB.EXTRA_KEY_TIME_TO_LIVE);
            if (timeToLiveObj != null) {
                if (timeToLiveObj instanceof String) {
                    timeToLive = Integer.parseInt((String) timeToLiveObj);
                } else { //expect int
                    timeToLive = (int) timeToLiveObj;
                }
            }
            //>> delayWhileIdle
            boolean delayWhileIdle = extras.getBoolean(AB.EXTRA_KEY_DELAY_WHILE_IDLE, false); //TODO: parseBooleanしないとダメ?
            //>> restrictedPackageName
            String restrictedPackageName = extras.getString(AB.EXTRA_KEY_RESTRICTED_PACKAGE_NAME);
            //>> dryRun
            boolean dryRun = extras.getBoolean(AB.EXTRA_KEY_DRY_RUN, false); //TODO: parseBooleanしないとダメ?
            //>> registrationIds
            List<String> registrationIds = extras.getStringArrayList(AB.EXTRA_KEY_REGISTRATION_IDS);

            //TODO: other properties
            if (title != null) put(Field.TITLE, title);
            if (message != null) put(Field.MESSAGE, message);
            if (pushId > 0) put(Field.PUSH_ID, pushId);
            if (url != null) put(Field.URL, url);
            if (from > 0) put(Field.FROM, from);
            if (collapseKey != null) put(Field.COLLAPSE_KEY, collapseKey);
            if (timeToLive > 0) put(Field.TIME_TO_LIVE, timeToLive);
            if (delayWhileIdle) put(Field.DELAY_WHILE_IDLE, delayWhileIdle);
            if (dryRun) put(Field.DRY_RUN, dryRun);
            if (restrictedPackageName != null)
                put(Field.RESTRICTED_PACKAGE_NAME, restrictedPackageName);
            if (registrationIds != null && registrationIds.size() > 0)
                put(Field.REGISTRATION_IDS, registrationIds);
        }
    }

//endregion

//region Open Message

    /**
     * 同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> ABResult<Void> openSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.openMessageSynchronously(obj, EnumSet.of(AB.PushMessageOpenOption.NONE));
    }

    /**
     * 同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.PushMessageOpenOption} オプション
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> ABResult<Void> openSynchronously(final AB.PushMessageOpenOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.openMessageSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.PushMessageOpenOption} オプション群
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> ABResult<Void> openSynchronously(final EnumSet<AB.PushMessageOpenOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.openMessageSynchronously(obj, options);
    }

    /**
     * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> void open() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.openMessage(obj, null, EnumSet.of(AB.PushMessageOpenOption.NONE));
    }

    /**
     * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * @param option {@link AB.PushMessageOpenOption} オプション
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> void open(final AB.PushMessageOpenOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.openMessage(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * @param options {@link AB.PushMessageOpenOption} オプション群
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> void open(final EnumSet<AB.PushMessageOpenOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.openMessage(obj, null, options);
    }

    /**
     * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> void open(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.openMessage(obj, callback, EnumSet.of(AB.PushMessageOpenOption.NONE));
    }

    /**
     * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.PushMessageOpenOption} オプション
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> void open(final ResultCallback<Void> callback, final AB.PushMessageOpenOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.openMessage(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでメッセージを開封します。このタイミングで、サーバにメッセージが開封されたことが通知されます。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.PushMessageOpenOption} オプション群
     * @param <T> {@link ABPushMessage} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1250">アピアリーズドキュメント &raquo; 開封登録をする</a>
     */
    public <T extends ABPushMessage> void open(final ResultCallback<Void> callback, final EnumSet<AB.PushMessageOpenOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.openMessage(obj, callback, options);
    }

//endregion

//region Save ** 非公開 **

    <T extends ABPushMessage> ABResult<T> saveSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.saveSynchronously(obj, EnumSet.of(AB.PushMessageSaveOption.NONE)); //XXX: unsafe cast
    }

    <T extends ABPushMessage> ABResult<T> saveSynchronously(final AB.PushMessageSaveOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.saveSynchronously(obj, EnumSet.of(option)); //XXX: unsafe cast
    }

    <T extends ABPushMessage> ABResult<T> saveSynchronously(final EnumSet<AB.PushMessageSaveOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.saveSynchronously(obj, options); //XXX: unsafe cast
    }

    <T extends ABPushMessage> void save() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.save(obj, null, EnumSet.of(AB.PushMessageSaveOption.NONE));
    }

    <T extends ABPushMessage> void save(final AB.PushMessageSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.save(obj, null, EnumSet.of(option));
    }

    <T extends ABPushMessage> void save(final EnumSet<AB.PushMessageSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.save(obj, null, options);
    }

    <T extends ABPushMessage> void save(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.save(obj, callback, EnumSet.of(AB.PushMessageSaveOption.NONE)); //XXX: unsafe cast
    }

    <T extends ABPushMessage> void save(final ResultCallback<T> callback, final AB.PushMessageSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.save(obj, callback, EnumSet.of(option)); //XXX: unsafe cast
    }

    <T extends ABPushMessage> void save(final ResultCallback<T> callback, final EnumSet<AB.PushMessageSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.save(obj, callback, options); //XXX: unsafe cast
    }

//endregion

//region delete ** 非公開 **

    <T extends ABPushMessage> ABResult<Void> deleteSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.deleteSynchronously(obj, EnumSet.of(AB.PushMessageDeleteOption.NONE));
    }

    <T extends ABPushMessage> ABResult<Void> deleteSynchronously(final AB.PushMessageDeleteOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.deleteSynchronously(obj, EnumSet.of(option));
    }

    <T extends ABPushMessage> ABResult<Void> deleteSynchronously(final EnumSet<AB.PushMessageDeleteOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.PushService.deleteSynchronously(obj, options);
    }

    <T extends ABPushMessage> void delete() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.delete(obj, null, EnumSet.of(AB.PushMessageDeleteOption.NONE));
    }

    <T extends ABPushMessage> void delete(final AB.PushMessageDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.delete(obj, null, EnumSet.of(option));
    }

    <T extends ABPushMessage> void delete(final EnumSet<AB.PushMessageDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.delete(obj, null, options);
    }

    <T extends ABPushMessage> void delete(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.delete(obj, callback, EnumSet.of(AB.PushMessageDeleteOption.NONE));
    }

    <T extends ABPushMessage> void delete(final ResultCallback<Void> callback, final AB.PushMessageDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.delete(obj, callback, EnumSet.of(option));
    }

    <T extends ABPushMessage> void delete(final ResultCallback<Void> callback, final EnumSet<AB.PushMessageDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.PushService.delete(obj, callback, options);
    }

//endregion

//region Refresh ** 非公開 **

    <T extends ABPushMessage> ABResult<T> refreshSynchronously() throws ABException {
        return refreshSynchronously(EnumSet.of(AB.PushMessageRefreshOption.NONE));
    }

    <T extends ABPushMessage> ABResult<T> refreshSynchronously(final AB.PushMessageRefreshOption option) throws ABException {
        return refreshSynchronously(EnumSet.of(option));
    }

    <T extends ABPushMessage> ABResult<T> refreshSynchronously(final EnumSet<AB.PushMessageRefreshOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        ABResult<T> ret = AB.PushService.fetchSynchronously(obj, AB.Helper.OptionHelper.convertToPushMessageFetchOption(options));
        T fetched = ret.getData();
        if (fetched != null) {
            setEstimatedData(fetched.getEstimatedData());
            apply();
        }
        return ret;
    }

    void refresh() {
        refresh(null, EnumSet.of(AB.PushMessageRefreshOption.NONE));
    }

    void refresh(final AB.PushMessageRefreshOption option) {
        refresh(null, EnumSet.of(option));
    }

    void refresh(final EnumSet<AB.PushMessageRefreshOption> options) {
        refresh(null, options);
    }

    <T extends ABPushMessage> void refresh(final ResultCallback<T> callback) {
        refresh(callback, EnumSet.of(AB.PushMessageRefreshOption.NONE));
    }

    <T extends ABPushMessage> void refresh(final ResultCallback<T> callback, final AB.PushMessageRefreshOption option) {
        refresh(callback, EnumSet.of(option));
    }

    <T extends ABPushMessage> void refresh(final ResultCallback<T> callback, final EnumSet<AB.PushMessageRefreshOption> options) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ABResult<T> ret = null;
                ABException rex = null;
                try {
                    ret = refreshSynchronously(options);
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

//region Miscellaneous

    /**
     * 有効な通知メッセージかどうかを取得します。
     * @return true: 有効なメッセージ
     */
    public boolean isValid() {
        if (get(Field.PUSH_ID) == null) return false;
        long pushId = get(Field.PUSH_ID);
        return pushId > 0L;
    }

    /**
     * プッシュ通知メッセージ・オブジェクト検索用のクエリオブジェクトを取得します。
     * @return {@link ABQuery} オブジェクト
     */
    public static ABQuery<? extends ABPushMessage> query() {
        return ABQuery.query(ABPushMessage.class);
    }

//endregion

//region Accessors

    /**
     * プッシュ通知IDを取得します。
     * @return プッシュ通知ID
     */
    public Long getPushId() {
        Number val = get(Field.PUSH_ID);
        return val == null ? 0L : val.longValue();
    }

    /**
     * プッシュ通知IDをセットします。
     * @param pushId プッシュ通知ID
     */
    public void setPushId(Long pushId) {
        put(Field.PUSH_ID, pushId);
    }

    /**
     * メッセージのタイトルを取得します。
     * @return メッセージのタイトル
     */
    public String getTitle() {
        return get(Field.TITLE);
    }

    /**
     * メッセージのタイトルをセットします。
     * @param title メッセージのタイトル
     */
    public void setTitle(String title) {
        put(Field.TITLE, title);
    }

    /**
     * メッセージ（本文）を取得します。
     * @return メッセージ（本文）
     */
    public String getMessage() {
        return get(Field.MESSAGE);
    }

    /**
     * メッセージ（本文）をセットします。
     * @param message メッセージ（本文）
     */
    public void setMessage(String message) {
        put(Field.MESSAGE, message);
    }

    /**
     * リッチプッシュ通知を送信する場合に表示させるWebコンテンツのURLを取得します。
     * @return リッチプッシュ通知を送信する場合に表示させるWebコンテンツのURL
     */
    public String getUrl() {
        return get(Field.URL);
    }

    /**
     * リッチプッシュ通知を送信する場合に表示させるWebコンテンツのURLをセットします。
     * @param url リッチプッシュ通知を送信する場合に表示させるWebコンテンツのURL
     */
    public void setUrl(String url) {
        put(Field.URL, url);
    }

    /**
     * From を取得します。
     * @return From
     */
    public long getFrom() {
        Number val = get(Field.FROM);
        return val == null ? 0L : val.longValue();
    }

    /**
     * From をセットします。
     * @param from From
     */
    public void setAction(long from) {
        put(Field.FROM, from);
    }

    /**
     * collapse_key フラグを取得します。
     * @return collapse_key フラグ
     */
    public String getCollapseKey() {
        return get(Field.COLLAPSE_KEY);
    }

    /**
     * collapse_key フラグをセットします。
     * @param collapseKey collapse_key フラグ
     */
    public void setCollapseKey(String collapseKey) {
        put(Field.COLLAPSE_KEY, collapseKey);
    }

    /**
     * time_to_live 値を取得します。
     * @return time_to_live 値
     */
    public int getTimeToLive() {
        Number val = get(Field.TIME_TO_LIVE);
        return val == null ? 0 : val.intValue();
    }

    /**
     * time_to_live 値をセットします。
     * @param timeToLive time_to_live 値
     */
    public void setTimeToLive(int timeToLive) {
        put(Field.TIME_TO_LIVE, timeToLive);
    }

    /**
     * delay_while_idle フラグを取得します。
     * @return delay_while_idle フラグ
     */
    public boolean isDelayWhileIdle() {
        Boolean val = get(Field.DELAY_WHILE_IDLE);
        return val == null ? false : val.booleanValue();
    }

    /**
     * delay_while_idle フラグをセットします。
     * @param flag delay_while_idle フラグ
     */
    public void setDelayWhileIdle(boolean flag) {
        put(Field.DELAY_WHILE_IDLE, flag);
    }

    /**
     * dry_run フラグを取得します。
     * @return dry_run フラグ
     */
    public boolean isDryRun() {
        Boolean val = get(Field.DRY_RUN);
        return val == null ? false : val.booleanValue();
    }

    /**
     * dry_run フラグをセットします。
     * @param flag dry_run フラグ
     */
    public void setDryRun(boolean flag) {
        put(Field.DRY_RUN, flag);
    }

    /**
     * restricted_package_name を取得します。
     * @return restricted_package_name
     */
    public String getRestrictedPackageName() {
        return get(Field.RESTRICTED_PACKAGE_NAME);
    }

    /**
     * restricted_package_name をセットします。
     * @param restrictedPackageName restricted_package_name
     */
    public void setRestrictedPackageName(String restrictedPackageName) {
        put(Field.RESTRICTED_PACKAGE_NAME, restrictedPackageName);
    }

    /**
     * 配信対象端末のレジストレーションIDを取得します。
     * @return 配信対象端末のレジストレーションID
     */
    public List<String> getRegistrationIds() {
        return get(Field.REGISTRATION_IDS);
    }

    /**
     * 配信対象端末のレジストレーションIDをセットします。
     * @param registrationIds 配信対象端末のレジストレーションID
     */
    public void setRegistrationIds(List<String> registrationIds) {
        put(Field.REGISTRATION_IDS, registrationIds);
    }

    /**
     * メッセージを受信した端末のデバイスを取得します。
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @return メッセージを受信した端末の {@link ABDevice} オブジェクト
     */
    public <T extends ABDevice> T getDevice() {
        @SuppressWarnings("unchecked") T device = (T)mDevice; //unsafe cast
        return device;
    }

    /**
     * メッセージを受信した端末のデバイスをセットします。
     * @param device メッセージを受信した端末の {@link ABDevice} オブジェクト
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     */
    public <T extends ABDevice> void setDevice(T device) {
        mDevice = device;
    }

//endregion

//region Cloneable

    /**
     * プッシュ通知メッセージ・オブジェクトを複製します。
     * @return 複製した {@link ABPushMessage} オブジェクト
     * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
     * @see java.lang.Object#clone()
     */
    public ABPushMessage clone() throws CloneNotSupportedException {
        ABPushMessage clone = (ABPushMessage)super.clone();
        clone.setDevice(this.getDevice());
        return clone;
    }

//endregion

//region Private methods

    // コールバックを実行する
    private <T> void executeResultCallbackIfNeeded(ResultCallback<T> callback, ABResult<T> result, ABException e) {
        if (callback != null) {
            callback.internalDone(result, e);
        }
    }

//endregion

}
