//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ファイル・モデル。
 * <p>ファイル・コレクションに格納するファイルを表現するモデルクラスです。</p>
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="http://docs.appiaries.com/?p=80">アピアリーズドキュメント &raquo; ファイルを管理する</a>
 */
@SuppressWarnings("unused")
@ABCollection("com.appiaries.baas.sdk.ABFile")
public class ABFile extends ABModel {

    private static final String TAG = ABFile.class.getSimpleName();

//region Fields

    /**
     * ファイル・フィールド。
     * <p>ファイル・モデルが持つフィールドの定数を保持します。</p>
     */
    public static class Field extends ABModel.Field {
        public static final ABField URL          = new ABField("_uri", String.class);
        public static final ABField NAME         = new ABField("_filename", String.class);
        public static final ABField CONTENT_TYPE = new ABField("_type", String.class);
        public static final ABField LENGTH       = new ABField("_length", long.class);
        public static final ABField TAGS         = new ABField("_tags", Set.class);
        public static final ABField DATA         = new ABField("_file", byte[].class);
    }

//endregion

//region Initialization

    /*
     * (非公開)
     * デフォルト・コンストラクタ。
     *
     * NOTE: subclassingの場合は @ABCollection で指定するので不要。
     *       継承せずにインスタンス化する場合は明示的にコレクションIDを指定させる必要がある
     */
    protected ABFile() {
        super();
    }

    /**
     * コンストラクタ。
     * <p>引数にコレクションIDを取ります。</p>
     * @param collectionID コレクションID
     */
    public ABFile(String collectionID) {
        super(collectionID);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param collectionID コレクションID
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABFile(String collectionID, Map<String, Object> map) {
        super(collectionID, map);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABFile(Map<String, Object> map) {
        super();
        setEstimatedData(map);
        setOriginalData(map);
        apply();
        mNew = true;
    }

//endregion

//region Save

    /**
     * 同期モードでファイル・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> ABResult<T> saveSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.saveSynchronously(obj, EnumSet.of(AB.FileSaveOption.NONE));
    }

    /**
     * 同期モードでファイル・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.FileSaveOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> ABResult<T> saveSynchronously(final AB.FileSaveOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.saveSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでファイル・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.FileSaveOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> ABResult<T> saveSynchronously(final EnumSet<AB.FileSaveOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.saveSynchronously(obj, options);
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, null, null, EnumSet.of(AB.FileSaveOption.NONE));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param option {@link AB.FileSaveOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save(final AB.FileSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, null, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param options {@link AB.FileSaveOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save(final EnumSet<AB.FileSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, null, null, options);
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, callback, null, EnumSet.of(AB.FileSaveOption.NONE));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param progressCallback 進捗取得コールバックハンドラ
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save(final ResultCallback<T> callback, final ProgressCallback progressCallback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, callback, progressCallback, EnumSet.of(AB.FileSaveOption.NONE));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.FileSaveOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save(final ResultCallback<T> callback, final AB.FileSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, callback, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.FileSaveOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save(final ResultCallback<T> callback, final EnumSet<AB.FileSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, callback, null, options);
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param progressCallback 進捗取得コールバックハンドラ
     * @param option {@link AB.FileSaveOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save(final ResultCallback<T> callback, final ProgressCallback progressCallback, final AB.FileSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, callback, progressCallback, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param progressCallback 進捗取得コールバックハンドラ
     * @param options {@link AB.FileSaveOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1080">アピアリーズドキュメント &raquo; バイナリオブジェクトを登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1090">アピアリーズドキュメント &raquo; バイナリオブジェクトを置き換え登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1100">アピアリーズドキュメント &raquo; バイナリオブジェクトを更新する</a>
     */
    public <T extends ABFile> void save(final ResultCallback<T> callback, final ProgressCallback progressCallback, final EnumSet<AB.FileSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.save(obj, callback, progressCallback, options);
    }

//endregion

//region Delete

    /**
     * 同期モードでファイル・オブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> ABResult<Void> deleteSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.deleteSynchronously(obj, EnumSet.of(AB.FileDeleteOption.NONE));
    }

    /**
     * 同期モードでファイル・オブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.FileDeleteOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> ABResult<Void> deleteSynchronously(final AB.FileDeleteOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.deleteSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでファイル・オブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.FileDeleteOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> ABResult<Void> deleteSynchronously(final EnumSet<AB.FileDeleteOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.deleteSynchronously(obj, options);
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアから削除します。
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> void delete() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.delete(obj, null, EnumSet.of(AB.FileDeleteOption.NONE));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアから削除します。
     * @param option {@link AB.FileDeleteOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> void delete(final AB.FileDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.delete(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアから削除します。
     * @param options {@link AB.FileDeleteOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> void delete(final EnumSet<AB.FileDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.delete(obj, null, options);
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> void delete(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.delete(obj, callback, EnumSet.of(AB.FileDeleteOption.NONE));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.FileDeleteOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> void delete(final ResultCallback<Void> callback, final AB.FileDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.delete(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル・オブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.FileDeleteOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1110">アピアリーズドキュメント &raquo; バイナリオブジェクトを削除する</a>
     */
    public <T extends ABFile> void delete(final ResultCallback<Void> callback, final EnumSet<AB.FileDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.delete(obj, callback, options);
    }

//endregion

//region Refresh

    /**
     * 同期モードでファイル・オブジェクトをリフレッシュします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public <T extends ABFile> ABResult<T> refreshSynchronously() throws ABException {
        return refreshSynchronously(EnumSet.of(AB.FileRefreshOption.NONE));
    }

    /**
     * 同期モードでファイル・オブジェクトをリフレッシュします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.FileRefreshOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public <T extends ABFile> ABResult<T> refreshSynchronously(final AB.FileRefreshOption option) throws ABException {
        return refreshSynchronously(EnumSet.of(option));
    }

    /**
     * 同期モードでファイル・オブジェクトをリフレッシュします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.FileRefreshOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public <T extends ABFile> ABResult<T> refreshSynchronously(final EnumSet<AB.FileRefreshOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        ABResult<T> ret = AB.FileService.fetchSynchronously(obj, AB.Helper.OptionHelper.convertToFileFetchOption(options));
        T fetched = ret.getData();
        if (fetched != null) {
            setEstimatedData(fetched.getEstimatedData());
            apply();
        }
        return ret;
    }

    /**
     * 非同期モードでファイル・オブジェクトをリフレッシュします。
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public void refresh() {
        refresh(null, EnumSet.of(AB.FileRefreshOption.NONE));
    }

    /**
     * 非同期モードでファイル・オブジェクトをリフレッシュします。
     * @param option {@link AB.FileRefreshOption} オプション
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public void refresh(final AB.FileRefreshOption option) {
        refresh(null, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル・オブジェクトをリフレッシュします。
     * @param options {@link AB.FileRefreshOption} オプション群
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public void refresh(final EnumSet<AB.FileRefreshOption> options) {
        refresh(null, options);
    }

    /**
     * 非同期モードでファイル・オブジェクトをリフレッシュします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public <T extends ABFile> void refresh(final ResultCallback<T> callback) {
        refresh(callback, EnumSet.of(AB.FileRefreshOption.NONE));
    }

    /**
     * 非同期モードでファイル・オブジェクトをリフレッシュします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.FileRefreshOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public <T extends ABFile> void refresh(final ResultCallback<T> callback, final AB.FileRefreshOption option) {
        refresh(callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル・オブジェクトをリフレッシュします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.FileRefreshOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1120">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(メタ情報部)</a>
     */
    public <T extends ABFile> void refresh(final ResultCallback<T> callback, final EnumSet<AB.FileRefreshOption> options) {
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

//region Download

    /**
     * 同期モードでファイル実体（バイナリ）をダウンロードします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> ABResult<Void> downloadSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.downloadSynchronously(obj, EnumSet.of(AB.FileDownloadOption.NONE));
    }

    /**
     * 同期モードでファイル実体（バイナリ）をダウンロードします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.FileDownloadOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> ABResult<Void> downloadSynchronously(final AB.FileDownloadOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.downloadSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでファイル実体（バイナリ）をダウンロードします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.FileDownloadOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> ABResult<Void> downloadSynchronously(final EnumSet<AB.FileDownloadOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.FileService.downloadSynchronously(obj, options);
    }

    /**
     * 非同期モードでファイル実体（バイナリ）をダウンロードします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> void download(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.download(obj, callback, null, EnumSet.of(AB.FileDownloadOption.NONE));
    }

    /**
     * 非同期モードでファイル実体（バイナリ）をダウンロードします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param progressCallback 進捗取得コールバックハンドラ
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> void download(final ResultCallback<Void> callback, final ProgressCallback progressCallback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.download(obj, callback, progressCallback, EnumSet.of(AB.FileDownloadOption.NONE));
    }

    /**
     * 非同期モードでファイル実体（バイナリ）をダウンロードします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.FileDownloadOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> void download(final ResultCallback<Void> callback, final AB.FileDownloadOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.download(obj, callback, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル実体（バイナリ）をダウンロードします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.FileDownloadOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> void download(final ResultCallback<Void> callback, final EnumSet<AB.FileDownloadOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.download(obj, callback, null, options);
    }

    /**
     * 非同期モードでファイル実体（バイナリ）をダウンロードします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param progressCallback 進捗取得コールバックハンドラ
     * @param option {@link AB.FileDownloadOption} オプション
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> void download(final ResultCallback<Void> callback, final ProgressCallback progressCallback, final AB.FileDownloadOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.download(obj, callback, progressCallback, EnumSet.of(option));
    }

    /**
     * 非同期モードでファイル実体（バイナリ）をダウンロードします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param progressCallback 進捗取得コールバックハンドラ
     * @param options {@link AB.FileDownloadOption} オプション群
     * @param <T> {@link ABFile} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1130">アピアリーズドキュメント &raquo; バイナリオブジェクトを検索する(バイナリ)</a>
     */
    public <T extends ABFile> void download(final ResultCallback<Void> callback, final ProgressCallback progressCallback, final EnumSet<AB.FileDownloadOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.FileService.download(obj, callback, progressCallback, options);
    }

//endregion

//region Miscellaneous

    /**
     * ファイル実体（バイナリ）をファイルシステムからロードし、プロパティ"data"にセットします。<br>
     * また、ロード時点でファイル名やコンテンツタイプが未指定であった場合は、それらが自動的にプロパティにセットされます。
     * @param resourceId /res/raw/ ディレクトリ配下に配置したファイルのID (e.g. R.raw.my_file)
     * @param filename ファイル名
     */
    public void loadData(int resourceId, String filename) {
        if (filename == null) return;

        String path = "android.resource://" + AB.sApplicationContext.getPackageName() + "/" + resourceId;

        Uri uri = Uri.parse(path);
        ContentResolver resolver = AB.sApplicationContext.getContentResolver();
        try {
            AssetFileDescriptor fd = resolver.openAssetFileDescriptor(uri, "r");
            setData(AB.FileService.getFileData(fd));

            setName(filename);

            //String mimeType = resolver.getType(uri);
            String mimeType = AB.FileService.getMimeTypeFromFilename(filename);
            setContentType(mimeType);

        } catch (FileNotFoundException e) {
            ABLog.e(TAG, e.getMessage());
        }
    }

    /**
     * ファイル実体（バイナリ）をファイルシステムからロードし、プロパティ"data"にセットします。<br>
     * また、ロード時点でファイル名やコンテンツタイプが未指定であった場合は、それらが自動的にプロパティにセットされます。
     * @param path ストレージに配置したファイルの絶対パス (e.g. getFilesDir() + "/my_file.png")
     */
    public void loadData(String path) {
        if (path == null) return;

        Uri uri = Uri.parse(path);
        //ContentResolver resolver = AB.sApplicationContext.getContentResolver();
        //try {
            //AssetFileDescriptor fd = resolver.openAssetFileDescriptor(uri, "r");
            //setData(AB.FileService.getFileData(fd));
            setData(AB.FileService.getFileData(path));

            String filename = uri.getLastPathSegment();
            setName(filename);

            String mimeType = AB.FileService.getMimeTypeFromFilename(filename);
            setContentType(mimeType);

        //} catch (FileNotFoundException e) {
        //    ABLogger.e(TAG, e.getMessage());
        //}
    }

    /**
     * ファイル・オブジェクト検索用のクエリオブジェクトを取得します。
     * @return {@link ABQuery} オブジェクト
     */
    public static ABQuery<? extends ABFile> query() {
        return ABQuery.query(ABFile.class);
    }

//endregion

//region Accessors

    /**
     * ファイル実体のダウンロードURLを取得します。
     * @return ファイル実体のダウンロードURL
     */
    public String getUrl() {
        return get(Field.URL);
    }

    /**
     * ファイル実体のダウンロードURLをセットします。
     * @param url ファイル実体のダウンロードURL
     */
    public void setUrl(String url) {
        put(Field.URL, url);
    }

    /**
     * ファイル名を取得します。
     * @return ファイル名
     */
    public String getName() {
        return get(Field.NAME);
    }

    /**
     * ファイル名をセットします。
     * @param name ファイル名
     */
    public void setName(String name) {
        put(Field.NAME, name);
    }

    /**
     * コンテンツタイプを取得します。
     * @return コンンテンツタイプ
     */
    public String getContentType() {
        return get(Field.CONTENT_TYPE);
    }

    /**
     * コンテンツタイプをセットします。
     * @param contentType コンテンツタイプ
     */
    public void setContentType(String contentType) {
        //if (contentType == null) return;
        put(Field.CONTENT_TYPE, contentType);
    }

    /**
     * ファイルサイズ（バイト数）を取得します。
     * @return ファイルサイズ（バイト数）
     */
    public long getLength() {
        Number val = get(Field.LENGTH);
        return val == null ? -1 : val.longValue();
    }

    /**
     * ファイルサイズ（バイト数）をセットします。
     * @param length ファイルサイズ（バイト数）
     */
    public void setLength(long length) {
        put(Field.LENGTH, length);
    }

    /**
     * タグを取得します。
     * @return タグ
     */
    public Set<String> getTags() {
        return get(Field.TAGS);
    }

    /*
    // カンマで連結したタグ文字列を返す
    String getJoinedTags() {
        StringBuilder builder = new StringBuilder();
        Set<String> tags = get(Field.TAGS);
        for (String t : tags) {
            if (builder.length() > 0) builder.append(",");
            builder.append(t);
        }
        return (builder.length() > 0) ? builder.toString() : null;
    }
    */

    /**
     * タグをセットします。
     * @param tags タグ
     */
    public void setTags(Set<String> tags) {
        put(Field.TAGS, tags);
    }

    /**
     * データ（バイナリ）を取得します。
     * @return データ（バイナリ）
     */
    public byte[] getData() {
        return get(Field.DATA);
    }

    /**
     * データ（バイナリ）をセットします。
     * @param data データ（バイナリ）
     */
    public void setData(byte[] data) {
        put(Field.DATA, data);
    }

//endregion


    // @see ABModel#inputDataFilter(String, Object)
    @Override
    public Object inputDataFilter(String key, Object value) {
        Object fixed = super.inputDataFilter(key, value);

        if (Field.TAGS.getKey().equals(key)) {
            //String[]で渡される
            if (fixed instanceof List) {
                fixed = new HashSet<>((List<String>) fixed);
            } else if (fixed instanceof Set) {
                /*NOP*/
            } else {
                ABLog.e(TAG, String.format("Unexpected input data (key:%s, value:%s, dataType:%s)", key, fixed, fixed.getClass()));
            }
        }
        return fixed;
    }

    // @see ABModel#outputDataFilter(String, Object)
    @Override
    public Object outputDataFilter(String key, Object value) {
        Object fixed = super.outputDataFilter(key, value);

        if (Field.TAGS.getKey().equals(key)) {
            if (fixed instanceof Set) {
                //Set<String>形式のデータをカンマ(,)で連結した文字列に変換する
                StringBuilder buff = new StringBuilder();
                @SuppressWarnings("unchecked") Set<String> tags = (Set<String>)value;
                for (String t : tags) {
                    if (buff.length() > 0) buff.append(",");
                    buff.append(t);
                }
                fixed = (buff.length() > 0) ? buff.toString() : value;
            } else {
                ABLog.e(TAG, String.format("Unexpected output data (key:%s, value:%s, dataType:%s)", key, fixed, fixed.getClass()));
            }
        }
        return fixed;
    }

//region Cloneable

    /**
     * ファイル・オブジェクトを複製します。
     * @return 複製した {@link ABFile} オブジェクト
     * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
     * @see java.lang.Object#clone()
     */
    public ABFile clone() throws CloneNotSupportedException {
        return (ABFile)super.clone();
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
