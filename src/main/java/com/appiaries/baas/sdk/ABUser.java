//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import android.os.AsyncTask;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ユーザ・モデル。
 * <p>
 * アプリ内のユーザを表現するモデルクラスです。<br>
 * アピアリーズ BaaS コントロールパネル上の「会員管理」で管理するユーザと連動します。
 * </p>
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="http://docs.appiaries.com/?p=30">アピアリーズドキュメント &raquo; 会員管理</a>
 */
@SuppressWarnings("unused")
@ABCollection("com.appiaries.baas.sdk.ABUser")
public class ABUser extends ABModel {

    private static String TAG = ABUser.class.getSimpleName();

//region Fields

    /**
     * ユーザ・フィールド。
     * <p>ユーザ・モデルが持つフィールドの定数を保持します。</p>
     */
    public static class Field extends ABModel.Field {
        public static final ABField LOGIN_ID       = new ABField("login_id", String.class);
        public static final ABField EMAIL          = new ABField("email", String.class);
        public static final ABField EMAIL_VERIFIED = new ABField("email_verified", boolean.class);
        public static final ABField PASSWORD       = new ABField("pw", String.class);
        public static final ABField AUTH_DATA      = new ABField("authData", Map.class);
        public static final ABField STATE          = new ABField("_state", Map.class);
    }

//endregion

//region Initialization

    /**
     * デフォルト・コンストラクタ。
     */
    public ABUser() {
        super();
    }

    /**
     * コンストラクタ。
     * <p>引数にコレクションIDを取ります。</p>
     * @param collectionID コレクションID
     */
    public ABUser(String collectionID) {
        super(collectionID);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param collectionID コレクションID
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABUser(String collectionID, Map<String, Object> map) {
        super(collectionID, map);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABUser(Map<String, Object> map) {
        super();
        setEstimatedData(map);
        setOriginalData(map);
        apply();
        mNew = true;
    }

//endregion

//region Sign-Up

    /**
     * 同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> ABResult<T> signUpSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.signUpSynchronously(obj, EnumSet.of(AB.UserSignUpOption.NONE));
    }

    /**
     * 同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.UserSignUpOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> ABResult<T> signUpSynchronously(final AB.UserSignUpOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.signUpSynchronously(obj, EnumSet.of(option));
    }
    /**
     * 同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.UserSignUpOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> ABResult<T> signUpSynchronously(final EnumSet<AB.UserSignUpOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.signUpSynchronously(obj, options);
    }

    /**
     * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void signUp() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.signUp(obj, null, EnumSet.of(AB.UserSignUpOption.NONE));
    }

    /**
     * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * @param option {@link AB.UserSignUpOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void signUp(final AB.UserSignUpOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.signUp(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * @param options {@link AB.UserSignUpOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void signUp(final EnumSet<AB.UserSignUpOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.signUp(obj, null, options);
    }

    /**
     * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void signUp(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.signUp(obj, callback, EnumSet.of(AB.UserSignUpOption.NONE));
    }

    /**
     * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.UserSignUpOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void signUp(final ResultCallback<T> callback, final AB.UserSignUpOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.signUp(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでサインアップ（ユーザ作成＋ログイン）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.UserSignUpOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void signUp(final ResultCallback<T> callback, final EnumSet<AB.UserSignUpOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.signUp(obj, callback, options);
    }

//endregion

//region Save

    /**
     * 同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> ABResult<T> saveSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.saveSynchronously(obj, EnumSet.of(AB.UserSaveOption.NONE));
    }

    /**
     * 同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.UserSaveOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> ABResult<T> saveSynchronously(final AB.UserSaveOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.saveSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.UserSaveOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> ABResult<T> saveSynchronously(final EnumSet<AB.UserSaveOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.saveSynchronously(obj, options);
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> void save() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.save(obj, null, EnumSet.of(AB.UserSaveOption.NONE));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * @param option {@link AB.UserSaveOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> void save(final AB.UserSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.save(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * @param options {@link AB.UserSaveOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> void save(final EnumSet<AB.UserSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.save(obj, null, options);
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> void save(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.save(obj, callback, EnumSet.of(AB.UserSaveOption.NONE));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.UserSaveOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> void save(final ResultCallback<T> callback, final AB.UserSaveOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.save(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアへ保存します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.UserSaveOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1150">アピアリーズドキュメント &raquo; 会員を登録する</a>
     * @see <a href="http://docs.appiaries.com/?p=1160">アピアリーズドキュメント &raquo; 会員を更新する</a>
     */
    public <T extends ABUser> void save(final ResultCallback<T> callback, final EnumSet<AB.UserSaveOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.save(obj, callback, options);
    }

//endregion

//region Delete

    /**
     * 同期モードでユーザ・オブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> ABResult<Void> deleteSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.deleteSynchronously(obj, EnumSet.of(AB.UserDeleteOption.NONE));
    }

    /**
     * 同期モードでユーザ・オブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.UserDeleteOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> ABResult<Void> deleteSynchronously(final AB.UserDeleteOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.deleteSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでユーザ・オブジェクトをデータストアから削除します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.UserDeleteOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> ABResult<Void> deleteSynchronously(final EnumSet<AB.UserDeleteOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.deleteSynchronously(obj, options);
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> void delete() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.delete(obj, null, EnumSet.of(AB.UserDeleteOption.NONE));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
     * @param option {@link AB.UserDeleteOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> void delete(final AB.UserDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.delete(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
     * @param options {@link AB.UserDeleteOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> void delete(final EnumSet<AB.UserDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.delete(obj, null, options);
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> void delete(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.delete(obj, callback, EnumSet.of(AB.UserDeleteOption.NONE));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.UserDeleteOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> void delete(final ResultCallback<Void> callback, final AB.UserDeleteOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.delete(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをデータストアから削除します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.UserDeleteOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1170">アピアリーズドキュメント &raquo; 会員を削除する</a>
     */
    public <T extends ABUser> void delete(final ResultCallback<Void> callback, final EnumSet<AB.UserDeleteOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.delete(obj, callback, options);
    }

//endregion

//region Refresh

    /**
     * 同期モードでユーザ・オブジェクトをリフレッシュします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> ABResult<T> refreshSynchronously() throws ABException {
        return refreshSynchronously(EnumSet.of(AB.UserRefreshOption.NONE));
    }

    /**
     * 同期モードでユーザ・オブジェクトをリフレッシュします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.UserRefreshOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> ABResult<T> refreshSynchronously(final AB.UserRefreshOption option) throws ABException {
        return refreshSynchronously(EnumSet.of(option));
    }

    /**
     * 同期モードでユーザ・オブジェクトをリフレッシュします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.UserRefreshOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> ABResult<T> refreshSynchronously(final EnumSet<AB.UserRefreshOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        ABResult<T> ret = AB.UserService.fetchSynchronously(obj, AB.Helper.OptionHelper.convertToUserFetchOption(options));
        T fetched = ret.getData();
        if (fetched != null) {
            setEstimatedData(fetched.getEstimatedData());
            apply();
        }
        return ret;
    }

    /**
     * 非同期モードでユーザ・オブジェクトをリフレッシュします。
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> void refresh() {
        refresh(null, EnumSet.of(AB.UserRefreshOption.NONE));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをリフレッシュします。
     * @param option {@link AB.UserRefreshOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> void refresh(final AB.UserRefreshOption option) {
        refresh(null, EnumSet.of(option));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをリフレッシュします。
     * @param options {@link AB.UserRefreshOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> void refresh(final EnumSet<AB.UserRefreshOption> options) {
        refresh(null, options);
    }

    /**
     * 非同期モードでユーザ・オブジェクトをリフレッシュします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> void refresh(final ResultCallback<T> callback) {
        refresh(callback, EnumSet.of(AB.UserRefreshOption.NONE));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをリフレッシュします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.UserRefreshOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> void refresh(final ResultCallback<T> callback, final AB.UserRefreshOption option) {
        refresh(callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでユーザ・オブジェクトをリフレッシュします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.UserRefreshOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1180">アピアリーズドキュメント &raquo; 会員情報を取得する</a>
     */
    public <T extends ABUser> void refresh(final ResultCallback<T> callback, final EnumSet<AB.UserRefreshOption> options) {
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

//region Log-In

    /**
     * 同期モードでログインします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> ABResult<T> logInSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.logInSynchronously(obj, EnumSet.of(AB.UserLogInOption.NONE));
    }

    /**
     * 同期モードでログインします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.UserLogInOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> ABResult<T> logInSynchronously(final AB.UserLogInOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.logInSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでログインします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.UserLogInOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> ABResult<T> logInSynchronously(final EnumSet<AB.UserLogInOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.logInSynchronously(obj, options);
    }

    /**
     * 非同期モードでログインします。
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void logIn() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logIn(obj, null, EnumSet.of(AB.UserLogInOption.NONE));
    }

    /**
     * 非同期モードでログインします。
     * @param option {@link AB.UserLogInOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void logIn(final AB.UserLogInOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logIn(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでログインします。
     * @param options {@link AB.UserLogInOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void logIn(final EnumSet<AB.UserLogInOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logIn(obj, null, options);
    }

    /**
     * 非同期モードでログインします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void logIn(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logIn(obj, callback, EnumSet.of(AB.UserLogInOption.NONE));
    }

    /**
     * 非同期モードでログインします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.UserLogInOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void logIn(final ResultCallback<T> callback, final AB.UserLogInOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logIn(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでログインします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.UserLogInOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1190">アピアリーズドキュメント &raquo; ログインする</a>
     */
    public <T extends ABUser> void logIn(final ResultCallback<T> callback, final EnumSet<AB.UserLogInOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logIn(obj, callback, options);
    }

//endregion

//region Log-Out

    /**
     * 同期モードでログアウトします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> ABResult<Void> logOutSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.logOutSynchronously(obj, EnumSet.of(AB.UserLogOutOption.NONE));
    }

    /**
     * 同期モードでログアウトします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.UserLogOutOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> ABResult<Void> logOutSynchronously(final AB.UserLogOutOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.logOutSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでログアウトします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.UserLogOutOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> ABResult<Void> logOutSynchronously(final EnumSet<AB.UserLogOutOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.UserService.logOutSynchronously(obj, options);
    }

    /**
     * 非同期モードでログアウトします。
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> void logOut() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logOut(obj, null, EnumSet.of(AB.UserLogOutOption.NONE));
    }

    /**
     * 非同期モードでログアウトします。
     * @param option {@link AB.UserLogOutOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> void logOut(final AB.UserLogOutOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logOut(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでログアウトします。
     * @param options {@link AB.UserLogOutOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> void logOut(final EnumSet<AB.UserLogOutOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logOut(obj, null, options);
    }

    /**
     * 非同期モードでログアウトします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> void logOut(final ResultCallback<Void> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logOut(obj, callback, EnumSet.of(AB.UserLogOutOption.NONE));
    }

    /**
     * 非同期モードでログアウトします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.UserLogOutOption} オプション
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> void logOut(final ResultCallback<Void> callback, final AB.UserLogOutOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logOut(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでログアウトします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.UserLogOutOption} オプション群
     * @param <T> {@link ABUser} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1200">アピアリーズドキュメント &raquo; ログアウトする</a>
     */
    public <T extends ABUser> void logOut(final ResultCallback<Void> callback, final EnumSet<AB.UserLogOutOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.UserService.logOut(obj, callback, options);
    }

//endregion

//region Miscellaneous

    /**
     * ユーザ・オブジェクト検索用のクエリオブジェクトを取得します。
     * @return {@link ABQuery} オブジェクト
     */
    public static ABQuery<? extends ABUser> query() {
        return ABQuery.query(ABUser.class);
    }

//endregion

//region Accessors
    public Set<String> keySet() {
        return mEstimatedData.keySet();
    }

    /**
     * ログインIDを取得します。
     * @return ログインID
     */
    public String getLoginId() {
        return get(Field.LOGIN_ID);
    }

    /**
     * ログインIDをセットします。
     * @param loginId ログインID
     */
    public void setLoginId(String loginId) {
        put(Field.LOGIN_ID, loginId);
    }

    /**
     * メールアドレスを取得します。
     * @return メールアドレス
     */
    public String getEmail() {
        return get(Field.EMAIL);
    }

    /**
     * メールアドレスをセットします。
     * @param email メールアドレス
     */
    public void setEmail(String email) {
        put(Field.EMAIL, email);
    }

    /**
     * メールアドレス本人確認済みフラグを取得します。
     * @return true:本人確認済み
     */
    public boolean isEmailVerified() {
        Boolean val = get(Field.EMAIL_VERIFIED);
        return val == null ? false : val;
    }

    /**
     * メールアドレス本人確認済みフラグをセットします。
     * @param emailVerified true:本人確認済み
     */
    public void setEmailVerified(boolean emailVerified) {
        put(Field.EMAIL_VERIFIED, emailVerified);
    }

    /**
     * パスワードを取得します。
     * @return パスワード
     */
    public String getPassword() {
        return get(Field.PASSWORD);
    }

    /**
     * パスワードをセットします。
     * @param password パスワード
     */
    public void setPassword(String password) {
        put(Field.PASSWORD, password);
    }

    /**
     * 認証情報を取得します。
     * @return 認証情報
     */
    public Map<String, Object> getAuthData() {
        return get(Field.AUTH_DATA);
    }

    /**
     * 認証情報をセットします。
     * @param authData 認証情報
     */
    public void setAuthData(Map<String, Object> authData) {
        put(Field.AUTH_DATA, authData);
    }

    /**
     * ユーザの状態を取得します。
     * <p>ユーザの状態には「locked」、「locking」などがあります。</p>
     * @return ユーザの状態
     * @see <a href="http://docs.appiaries.com/?p=1140">アピアリーズドキュメント &raquo; アカウントロック機能</a>
     */
    public String getState() {
        return get(Field.STATE);
    }

    /**
     * ユーザの状態をセットします。
     * <p>ユーザの状態には「locked」、「locking」などがあります。</p>
     * @param state ユーザの状態
     * @see <a href="http://docs.appiaries.com/?p=1140">アピアリーズドキュメント &raquo; アカウントロック機能</a>
     */
    public void setState(String state) {
        put(Field.STATE, state);
    }

//endregion

//region Cloneable

    /**
     * ユーザ・オブジェクトを複製します。
     * @return 複製した {@link ABUser} オブジェクト
     * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
     * @see java.lang.Object#clone()
     */
    public ABUser clone() throws CloneNotSupportedException {
        return (ABUser)super.clone();
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
