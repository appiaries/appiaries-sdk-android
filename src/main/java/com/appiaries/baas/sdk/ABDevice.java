//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import android.os.AsyncTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * デバイス・モデル。
 * <p>アプリがインストールされたデバイスを表現するモデルクラスです。</p>
 * <div class="important">
 * 現在のバージョンでは、本クラスを継承してサブクラス化 (Subclassing) することはできません。
 * </div>
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="http://docs.appiaries.com/?p=130">アピアリーズドキュメント &raquo; プッシュ通知</a>
 */
@SuppressWarnings("unused")
@ABCollection("com.appiaries.baas.sdk.ABDevice")
public class ABDevice extends ABModel implements Serializable {

    private static final String TAG = ABDevice.class.getSimpleName();

    private static final long serialVersionUID = 2067295857487670373L;

    private static final List<String> RESERVED_KEYS = Collections.unmodifiableList(new ArrayList<String>(){{
        add(Field.REGISTRATION_ID.getKey());
        add(Field.ENVIRONMENT.getKey());
        add(Field.TYPE.getKey());
        add(Field.RESERVED_PUSH_IDS.getKey());
        add(Field.ID.getKey());
        add(Field.CREATED.getKey());
        add(Field.CREATED_BY.getKey());
        add(Field.UPDATED.getKey());
        add(Field.UPDATED_BY.getKey());
    }});

//region Fields

    /**
     * デバイス・フィールド。
     * <p>デバイス・モデルが持つフィールドの定数を保持します。</p>
     */
    public static class Field extends ABModel.Field {
        /**
         * レジストレーションID。
         */
        public static final ABField REGISTRATION_ID   = new ABField("regid", String.class);
        /**
         * デバイス・タイプ。
         */
        public static final ABField TYPE              = new ABField("_typ", String.class);
        /**
         * 環境。
         */
        public static final ABField ENVIRONMENT       = new ABField("_env", int.class);
//        public static final ABField ATTRIBUTES        = new ABField("attr", Map.class);
        /**
         * 予約済みプッシュID。
         */
        public static final ABField RESERVED_PUSH_IDS = new ABField("reserveids", Set.class);
    }

//endregion

//region Initialization

    /**
     * デフォルト・コンストラクタ。
     */
    public ABDevice() {
        super();
    }

    /**
     * コンストラクタ。
     * <p>引数にコレクションIDを取ります。</p>
     * @param collectionID コレクションID
     */
    public ABDevice(String collectionID) {
        super(collectionID);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param collectionID コレクションID
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABDevice(String collectionID, Map<String, Object> map) {
        super(collectionID, map);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABDevice(Map<String, Object> map) {
        super();
        setEstimatedData(map);
        setOriginalData(map);
        apply();
        mNew = true;
    }

//endregion

//region Register

    /**
     * 同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> ABResult<T> registerSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.registerSynchronously(obj, EnumSet.of(AB.DeviceRegistrationOption.NONE));
    }

    /**
     * 同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.DeviceRegistrationOption} オプション
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> ABResult<T> registerSynchronously(final AB.DeviceRegistrationOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.registerSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.DeviceRegistrationOption} オプション群
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> ABResult<T> registerSynchronously(final EnumSet<AB.DeviceRegistrationOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.registerSynchronously(obj, options);
    }

    /**
     * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> void register() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.register(obj, null, EnumSet.of(AB.DeviceRegistrationOption.NONE));
    }

    /**
     * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * @param option {@link AB.DeviceRegistrationOption} オプション
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> void register(final AB.DeviceRegistrationOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.register(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * @param options {@link AB.DeviceRegistrationOption} オプション群
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> void register(final EnumSet<AB.DeviceRegistrationOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.register(obj, null, options);
    }

    /**
     * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> void register(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.register(obj, callback, EnumSet.of(AB.DeviceRegistrationOption.NONE));
    }

    /**
     * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.DeviceRegistrationOption} オプション
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> void register(final ResultCallback<T> callback, final AB.DeviceRegistrationOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.register(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでデバイス・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.DeviceRegistrationOption} オプション群
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1210">アピアリーズドキュメント &raquo; レジストレーションIDを登録する</a>
     */
    public <T extends ABDevice> void register(final ResultCallback<T> callback, final EnumSet<AB.DeviceRegistrationOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.register(obj, callback, options);
    }

//endregion

//region Unregister

    /**
     * 同期モードでデバイス・オブジェクトの登録を解除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> ABResult<Void> unregisterSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.unregisterSynchronously(obj, EnumSet.of(AB.DeviceUnregistrationOption.NONE));
    }

    /**
     * 同期モードでデバイス・オブジェクトの登録を解除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.DeviceUnregistrationOption} オプション
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> ABResult<Void> unregisterSynchronously(final AB.DeviceUnregistrationOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.unregisterSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでデバイス・オブジェクトの登録を解除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.DeviceUnregistrationOption} オプション群
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> ABResult<Void> unregisterSynchronously(final EnumSet<AB.DeviceUnregistrationOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.unregisterSynchronously(obj, options);
    }

    /**
     * 非同期モードでデバイス・オブジェクトの登録を解除します。
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> void unregister() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.unregister(obj, null, EnumSet.of(AB.DeviceUnregistrationOption.NONE));
    }

    /**
     * 非同期モードでデバイス・オブジェクトの登録を解除します。
     * @param option {@link AB.DeviceUnregistrationOption} オプション
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> void unregister(final AB.DeviceUnregistrationOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.unregister(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでデバイス・オブジェクトの登録を解除します。
     * @param options {@link AB.DeviceUnregistrationOption} オプション群
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> void unregister(final EnumSet<AB.DeviceUnregistrationOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.unregister(obj, null, options);
    }

    /**
     * 非同期モードでデバイス・オブジェクトの登録を解除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> void unregister(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.unregister(obj, callback, EnumSet.of(AB.DeviceUnregistrationOption.NONE));
    }

    /**
     * 非同期モードでデバイス・オブジェクトの登録を解除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.DeviceUnregistrationOption} オプション
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> void unregister(final ResultCallback<Void> callback, final AB.DeviceUnregistrationOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.unregister(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでデバイス・オブジェクトの登録を解除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.DeviceUnregistrationOption} オプション群
     * @param <T> {@link ABDevice} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1220">アピアリーズドキュメント &raquo; レジストレーションIDを削除する</a>
     */
    public <T extends ABDevice> void unregister(final ResultCallback<Void> callback, final EnumSet<AB.DeviceUnregistrationOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.unregister(obj, callback, options);
    }

//endregion

//region Save ** 非公開

    <T extends ABDevice> ABResult<T> saveSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.saveSynchronously(obj, EnumSet.of(AB.DeviceSaveOption.NONE));
    }

    <T extends ABDevice> ABResult<T> saveSynchronously(final AB.DeviceSaveOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.saveSynchronously(obj, EnumSet.of(option));
    }

    <T extends ABDevice> ABResult<T> saveSynchronously(final EnumSet<AB.DeviceSaveOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.saveSynchronously(obj, options);
    }

    <T extends ABDevice> void save() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.save(obj, null, EnumSet.of(AB.DeviceSaveOption.NONE));
    }

    <T extends ABDevice> void save(final AB.DeviceSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.save(obj, null, EnumSet.of(option));
    }

    <T extends ABDevice> void save(final EnumSet<AB.DeviceSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.save(obj, null, options);
    }

    <T extends ABDevice> void save(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.save(obj, callback, EnumSet.of(AB.DeviceSaveOption.NONE)); //XXX: unsafe cast
    }

    <T extends ABDevice> void save(final ResultCallback<T> callback, final AB.DeviceSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.save(obj, callback, EnumSet.of(option)); //XXX: unsafe cast
    }

    <T extends ABDevice> void save(final ResultCallback<T> callback, final EnumSet<AB.DeviceSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.save(obj, callback, options); //XXX: unsafe cast
    }

//endregion

//region Delete ** 非公開 **

    <T extends ABDevice> ABResult<Void> deleteSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.deleteSynchronously(obj, EnumSet.of(AB.DeviceDeleteOption.NONE));
    }

    <T extends ABDevice> ABResult<Void> deleteSynchronously(final AB.DeviceDeleteOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.deleteSynchronously(obj, EnumSet.of(option));
    }

    <T extends ABDevice> ABResult<Void> deleteSynchronously(final EnumSet<AB.DeviceDeleteOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DeviceService.deleteSynchronously(obj, options);
    }

    <T extends ABDevice> void delete() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.delete(obj, null, EnumSet.of(AB.DeviceDeleteOption.NONE));
    }

    <T extends ABDevice> void delete(final AB.DeviceDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.delete(obj, null, EnumSet.of(option));
    }

    <T extends ABDevice> void delete(final EnumSet<AB.DeviceDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.delete(obj, null, options);
    }

    <T extends ABDevice> void delete(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.delete(obj, callback, EnumSet.of(AB.DeviceDeleteOption.NONE));
    }

    <T extends ABDevice> void delete(final ResultCallback<Void> callback, final AB.DeviceDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.delete(obj, callback, EnumSet.of(option));
    }

    <T extends ABDevice> void delete(final ResultCallback<Void> callback, final EnumSet<AB.DeviceDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DeviceService.delete(obj, callback, options);
    }

//endregion

//region Refresh ** 非公開 **

    <T extends ABDevice> ABResult<T> refreshSynchronously() throws ABException {
        return refreshSynchronously(EnumSet.of(AB.DeviceRefreshOption.NONE));
    }

    <T extends ABDevice> ABResult<T> refreshSynchronously(final AB.DeviceRefreshOption option) throws ABException {
        return refreshSynchronously(EnumSet.of(option));
    }

    <T extends ABDevice> ABResult<T> refreshSynchronously(final EnumSet<AB.DeviceRefreshOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        ABResult<T> ret = AB.DeviceService.fetchSynchronously(obj, AB.Helper.OptionHelper.convertToDeviceFetchOption(options));
        T fetched = ret.getData();
        if (fetched != null) {
            setEstimatedData(fetched.getEstimatedData());
            apply();
        }
        return ret;
    }

    <T extends ABDevice> void refresh() {
        refresh(null, EnumSet.of(AB.DeviceRefreshOption.NONE));
    }

    <T extends ABDevice> void refresh(final AB.DeviceRefreshOption option) {
        refresh(null, EnumSet.of(option));
    }

    <T extends ABDevice> void refresh(final EnumSet<AB.DeviceRefreshOption> options) {
        refresh(null, options);
    }

    <T extends ABDevice> void refresh(final ResultCallback<T> callback) {
        refresh(callback, EnumSet.of(AB.DeviceRefreshOption.NONE));
    }

    <T extends ABDevice> void refresh(final ResultCallback<T> callback, final AB.DeviceRefreshOption option) {
        refresh(callback, EnumSet.of(option));
    }

    <T extends ABDevice> void refresh(final ResultCallback<T> callback, final EnumSet<AB.DeviceRefreshOption> options) {
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
     * デバイス・オブジェクト検索用のクエリオブジェクトを取得します。
     * @return {@link ABQuery} オブジェクト
     */
    public static ABQuery<? extends ABDevice> query() {
        return ABQuery.query(ABDevice.class);
    }

//endregion

//region Accessors

    /**
     * レジストレーションIDを取得します。
     * @return レジストレーションID
     */
    public String getRegistrationID() {
        return get(Field.REGISTRATION_ID);
    }

    /**
     * レジストレーションIDをセットします。
     * @param registrationID レジストレーションID
     */
    public void setRegistrationID(String registrationID) {
        put(Field.REGISTRATION_ID, registrationID);
    }

    /**
     * デバイス・プラットフォームを取得します。
     * @return {@link AB.Platform}
     */
    public AB.Platform getPlatform() {
        if ("gcm".equals(get(Field.TYPE))) {
            return AB.Platform.ANDROID;
        } else if ("apns".equals(get(Field.TYPE))) {
            return AB.Platform.IOS;
        } else {
            return AB.Platform.UNKNOWN;
        }
    }

    /**
     * デバイス・タイプを取得します。
     * <p>Android端末の場合は"gcm"が、iPhone端末の場合は"apns"が返されます。</p>
     * @return デバイス・タイプ
     */
    public String getType() {
        return get(Field.TYPE);
    }

    /**
     * デバイス・タイプをセットします。
     * <p>Android端末の場合は"gcm"を、iPhone端末の場合は"apns"をセットします。</p>
     * @param type デバイス・タイプ
     */
    public void setType(String type) {
        put(Field.TYPE, type);
    }

    /**
     * プッシュ配信環境を取得します。
     * @return Production環境でプッシュ通知を配信する場合は1が、Sandbox環境の場合は0が返されます
     */
    public int getEnvironment() {
        Number val = get(Field.ENVIRONMENT);
        return val == null ? -1 : val.intValue();
    }

    /**
     * プッシュ配信環境をセットします。
     * @param environment Production環境でプッシュ通知を配信する場合は1を、Sandbox環境の場合は0をセットします
     */
    public void setEnvironment(int environment) {
        put(Field.ENVIRONMENT, environment);
    }

    /**
     * デバイス属性を取得します。
     * @return デバイス属性
     */
    public Map<String, Object> getAttributes() {
        Map<String, Object> attrs = new HashMap<>();
        for (Map.Entry<String, Object> entry : mEstimatedData.entrySet()) {
            String key = entry.getKey();
            if (!RESERVED_KEYS.contains(key)) {
                attrs.put(key, entry.getValue());
            }
        }
        return attrs;
    }

    /**
     * デバイス属性をセットします。
     * @param attributes デバイス属性
     */
    public void setAttributes(Map<String, Object> attributes) {
        //put(Field.ATTRIBUTES, attributes);
        mEstimatedData.putAll(attributes); //TODO: 予約キーが上書きされる恐れがある
    }

    /**
     * 予約されたプッシュ通知IDを取得します。
     * @return 予約されたプッシュ通知ID
     */
    public Set<Long> getReservedPushIds() {
        return get(Field.RESERVED_PUSH_IDS);
    }

    /**
     * 予約されたプッシュ通知IDをセットします。
     * @param reservedPushIds 予約されたプッシュ通知ID
     */
    public void setReservedPushIds(Set<Long> reservedPushIds) {
        put(Field.RESERVED_PUSH_IDS, reservedPushIds);
    }

//endregion

//region Cloneable

    /**
     * デバイス・オブジェクトを複製します。
     * @return 複製した {@link ABDevice} オブジェクト
     * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
     * @see java.lang.Object#clone()
     */
    public ABDevice clone() throws CloneNotSupportedException {
        ABDevice clone = (ABDevice)super.clone();
        clone.setRegistrationID(this.getRegistrationID());
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
