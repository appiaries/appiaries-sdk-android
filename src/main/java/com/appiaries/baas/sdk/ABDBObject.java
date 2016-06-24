//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import android.os.AsyncTask;
import android.util.Log;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * DBオブジェクト・モデル。
 * <p>JSONデータ・コレクションに格納するJSONデータを表現するモデルクラスです。</p>
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="http://docs.appiaries.com/?p=70">アピアリーズドキュメント &raquo; JSONデータを管理する</a>
 * @see <a href="http://tutorial.appiaries.com/v1/tutorial/json/">アピアリーズドキュメント &raquo; JSONについて</a>
 */
@SuppressWarnings("unused")
@ABCollection("com.appiaries.baas.sdk.ABDBObject")
public class ABDBObject extends ABModel {

    private static String TAG = ABDBObject.class.getSimpleName();

//region Fields

    /**
     * DBオブジェクト・フィールド。
     * <p>DBオブジェクト・モデルが持つフィールドの定数を保持します。</p>
     */
    public static class Field extends ABModel.Field {
        /**
         * 位置情報。
         * <p>緯度・経度。</p>
         */
        public static final ABField GEO_POINT = new ABField("_coord", ABGeoPoint.class);
        /**
         * 距離。
         * <p>基点位置情報からの距離。</p>
         */
        public static final ABField DISTANCE  = new ABField("distance", double.class);
    }

//endregion

//region Initialization

    /*
     * (非公開)
     * デフォルト・コンストラクタ。
     *
     * NOTE: subclassingの場合は @ABCollection で指定するので不要。
     *       継承せずにインスタンス化する場合は明示的にコレクションIDを指定させる必要がある。
     */
    protected ABDBObject() {
        super();
    }

    /**
     * コンストラクタ。
     * <p>引数にコレクションIDを取ります。</p>
     * @param collectionID コレクションID
     */
    public ABDBObject(String collectionID) {
        super(collectionID);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param collectionID コレクションID
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABDBObject(String collectionID, Map<String, Object> map) {
        super(collectionID, map);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABDBObject(Map<String, Object> map) {
        super();
        setEstimatedData(map);
        setOriginalData(map);
        apply();
        mNew = true;
    }

//endregion

//region Save

    /**
     * 同期モードでDBオブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> ABResult<T> saveSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DBService.saveSynchronously(obj, EnumSet.of(AB.DBObjectSaveOption.NONE));
    }

    /**
     * 同期モードでDBオブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.DBObjectSaveOption} オプション
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> ABResult<T> saveSynchronously(final AB.DBObjectSaveOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DBService.saveSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでDBオブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.DBObjectSaveOption} オプション群
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> ABResult<T> saveSynchronously(final EnumSet<AB.DBObjectSaveOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DBService.saveSynchronously(obj, options);
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアへ保存します。
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> void save() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.save(obj, null, EnumSet.of(AB.DBObjectSaveOption.NONE));
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアへ保存します。
     * @param option {@link AB.DBObjectSaveOption} オプション
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> void save(final AB.DBObjectSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.save(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアへ保存します。
     * @param options {@link AB.DBObjectSaveOption} オプション群
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> void save(final EnumSet<AB.DBObjectSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.save(obj, null, options);
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> void save(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.save(obj, callback, EnumSet.of(AB.DBObjectSaveOption.NONE));
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.DBObjectSaveOption} オプション
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> void save(final ResultCallback<T> callback, final AB.DBObjectSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.save(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.DBObjectSaveOption} オプション群
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1020">アピアリーズドキュメント &raquo; JSONオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1030">アピアリーズドキュメント &raquo; JSONオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1040">アピアリーズドキュメント &raquo; JSONオブジェクトを更新する</a>
     */
    public <T extends ABDBObject> void save(final ResultCallback<T> callback, final EnumSet<AB.DBObjectSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.save(obj, callback, options);
    }

//endregion

//region delete

    /**
     * 同期モードでDBオブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> ABResult<Void> deleteSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DBService.deleteSynchronously(obj, EnumSet.of(AB.DBObjectDeleteOption.NONE));
    }

    /**
     * 同期モードでDBオブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.DBObjectDeleteOption} オプション
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> ABResult<Void> deleteSynchronously(final AB.DBObjectDeleteOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DBService.deleteSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでDBオブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.DBObjectDeleteOption} オプション群
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> ABResult<Void> deleteSynchronously(final EnumSet<AB.DBObjectDeleteOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.DBService.deleteSynchronously(obj, options);
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアから削除します。
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> void delete() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.delete(obj, null, EnumSet.of(AB.DBObjectDeleteOption.NONE));
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアから削除します。
     * @param option {@link AB.DBObjectDeleteOption} オプション
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> void delete(final AB.DBObjectDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.delete(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアから削除します。
     * @param options {@link AB.DBObjectDeleteOption} オプション群
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> void delete(final EnumSet<AB.DBObjectDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.delete(obj, null, options);
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> void delete(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.delete(obj, callback, EnumSet.of(AB.DBObjectDeleteOption.NONE));
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.DBObjectDeleteOption} オプション
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> void delete(final ResultCallback<Void> callback, final AB.DBObjectDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.delete(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでDBオブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.DBObjectDeleteOption} オプション群
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1050">アピアリーズドキュメント &raquo; JSONオブジェクトを削除する</a>
     */
    public <T extends ABDBObject> void delete(final ResultCallback<Void> callback, final EnumSet<AB.DBObjectDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.DBService.delete(obj, callback, options);
    }

//endregion

//region Refresh

    /**
     * 同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public <T extends ABDBObject> ABResult<T> refreshSynchronously() throws ABException {
        return refreshSynchronously(EnumSet.of(AB.DBObjectRefreshOption.NONE));
    }

    /**
     * 同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.DBObjectDeleteOption} オプション
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public <T extends ABDBObject> ABResult<T> refreshSynchronously(final AB.DBObjectRefreshOption option) throws ABException {
        return refreshSynchronously(EnumSet.of(option));
    }

    /**
     * 同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.DBObjectDeleteOption} オプション群
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public <T extends ABDBObject> ABResult<T> refreshSynchronously(final EnumSet<AB.DBObjectRefreshOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        ABResult<T> ret = AB.DBService.fetchSynchronously(obj, AB.Helper.OptionHelper.convertToDBObjectFetchOption(options));
        T fetched = ret.getData();
        if (fetched != null) {
            setEstimatedData(fetched.getEstimatedData());
            apply();
        }
        return ret;
    }

    /**
     * 非同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public void refresh() {
        refresh(null, EnumSet.of(AB.DBObjectRefreshOption.NONE));
    }

    /**
     * 非同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * @param option {@link AB.DBObjectDeleteOption} オプション
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public void refresh(final AB.DBObjectRefreshOption option) {
        refresh(null, EnumSet.of(option));
    }

    /**
     * 非同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * @param options {@link AB.DBObjectDeleteOption} オプション群
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public void refresh(final EnumSet<AB.DBObjectRefreshOption> options) {
        refresh(null, options);
    }

    /**
     * 非同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public <T extends ABDBObject> void refresh(final ResultCallback<T> callback) {
        refresh(callback, EnumSet.of(AB.DBObjectRefreshOption.NONE));
    }

    /**
     * 非同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.DBObjectDeleteOption} オプション
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public <T extends ABDBObject> void refresh(final ResultCallback<T> callback, final AB.DBObjectRefreshOption option) {
        refresh(callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでDBオブジェクトをリフレッシュします。
     * <p>オブジェクトをサーバに保存されている最新の状態にリフレッシュしたい場合に利用します。</p>
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.DBObjectDeleteOption} オプション群
     * @param <T> {@link ABDBObject} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1070">アピアリーズドキュメント &raquo; JSONオブジェクトを検索する</a>
     */
    public <T extends ABDBObject> void refresh(final ResultCallback<T> callback, final EnumSet<AB.DBObjectRefreshOption> options) {
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
     * DBオブジェクト検索用のクエリオブジェクトを取得します。
     * @return {@link ABQuery} オブジェクト
     */
    public static ABQuery<? extends ABDBObject> query() {
        return ABQuery.query(ABDBObject.class);
    }

//endregion

//region Accessors

    /**
     * 位置情報 (緯度・経度) を取得します。
     * @return {@link ABGeoPoint} オブジェクト
     * @see <a href="http://docs.appiaries.com/?p=100">アピアリーズドキュメント &raquo; 位置情報を利用する</a>
     */
    public ABGeoPoint getGeoPoint() {
        List<Number> val = get(Field.GEO_POINT);
        if (val != null && val.size() >= 2) {
            return new ABGeoPoint(val.get(1).doubleValue(), val.get(0).doubleValue());
        } else {
            return null;
        }
    }

    /**
     * 位置情報 (緯度・経度) をセットします。
     * @param geoPoint {@link ABGeoPoint} オブジェクト
     * @see <a href="http://docs.appiaries.com/?p=100">アピアリーズドキュメント &raquo; 位置情報を利用する</a>
     */
    public void setGeoPoint(ABGeoPoint geoPoint) {
        if (geoPoint != null) {
            put(Field.GEO_POINT, String.format("%f,%f", geoPoint.getLongitude(), geoPoint.getLatitude()));
        } else {
            put(Field.GEO_POINT, null);
        }
    }

    /**
     * 基準点から距離を取得します。
     * @return 基準点から距離
     * @see <a href="http://docs.appiaries.com/?p=100">アピアリーズドキュメント &raquo; 位置情報を利用する</a>
     */
    public double getDistance() {
        Number val = get(Field.DISTANCE);
        return val == null ? 0.0 : val.doubleValue();
    }

    /**
     * 基準点から距離をセットします。
     * @param distance 基準点から距離
     * @see <a href="http://docs.appiaries.com/?p=100">アピアリーズドキュメント &raquo; 位置情報を利用する</a>
     */
    public void setDistance(double distance) {
        put(Field.DISTANCE, distance);
    }

//endregion

//region Cloneable

    /**
     * DBオブジェクトを複製します。
     * @return 複製した {@link ABDBObject} オブジェクト
     * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
     * @see java.lang.Object#clone()
     */
    public ABDBObject clone() throws CloneNotSupportedException {
        return (ABDBObject)super.clone();
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
