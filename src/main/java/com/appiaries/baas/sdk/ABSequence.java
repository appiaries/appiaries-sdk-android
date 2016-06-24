//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * シーケンス・モデル。
 * <p>シーケンス・コレクションを表現するモデルクラスです。</p>
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="http://docs.appiaries.com/?p=90">アピアリーズドキュメント » シーケンス値を管理する</a>
 */
@SuppressWarnings("unused")
@ABCollection("com.appiaries.baas.sdk.ABSequence")
public class ABSequence extends ABModel {

    private static final String TAG = ABSequence.class.getSimpleName();

//region Fields

    /**
     * シーケンス・フィールド。
     * <p>シーケンス・モデルが持つフィールドの定数を保持します。</p>
     */
    public static class Field extends ABModel.Field {
        public static final ABField VALUE = new ABField("seq", long.class);
        public static final ABField INITIAL_VALUE = new ABField("initial", long.class);
    }

//endregion

    // @see ABModel#inputDataFilter(String, Object)
    @Override
    public Object inputDataFilter(String key, Object value) {
        Object fixed = super.inputDataFilter(key, value);
        switch (key) {
            case "seq":
                if (fixed instanceof Integer) {
                    BigDecimal decimal = new BigDecimal((Integer)fixed);
                    fixed = decimal.longValue();
                }/* else if (fixed instanceof Short) {
                    BigDecimal decimal = new BigDecimal((Short)fixed);
                    fixed = decimal.longValue();
                }*/
                break;
            default:
                break;
        }
        return fixed;
    }

//region Initialization

    /*
     * (非公開)
     * デフォルト・コンストラクタ。
     *
     * NOTE: subclassingの場合は @ABCollection で指定するので不要。
     *       継承せずにインスタンス化する場合は明示的にコレクションIDを指定させる必要がある
     */
    protected ABSequence() {
        super();
    }

    /**
     * コンストラクタ。
     * <p>引数にコレクションIDを取ります。</p>
     * @param collectionID コレクションID
     */
    public ABSequence(String collectionID) {
        super(collectionID);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param collectionID コレクションID
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABSequence(String collectionID, Map<String, Object> map) {
        super(collectionID, map);
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABSequence(Map<String, Object> map) {
        super();
        setEstimatedData(map);
        setOriginalData(map);
        apply();
        mNew = true;
    }

//endregion

//region Get Current Value

    /**
     * 同期モードでシーケンスの現在値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
     */
    public <T extends ABSequence> long getCurrentValueSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getCurrentValueSynchronously(obj, EnumSet.of(AB.SequenceFetchOption.NONE));
    }

    /**
     * 同期モードでシーケンスの現在値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.SequenceFetchOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
     */
    public <T extends ABSequence> long getCurrentValueSynchronously(final AB.SequenceFetchOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getCurrentValueSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでシーケンスの現在値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.SequenceFetchOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
     */
    public <T extends ABSequence> long getCurrentValueSynchronously(final EnumSet<AB.SequenceFetchOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getCurrentValueSynchronously(obj, options);
    }

    /**
     * 非同期モードでシーケンスの現在値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
     */
    public <T extends ABSequence> void getCurrentValue(final SequenceCallback callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getCurrentValue(obj, callback, EnumSet.of(AB.SequenceFetchOption.NONE));
    }

    /**
     * 非同期モードでシーケンスの現在値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param option {@link AB.SequenceFetchOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
     */
    public <T extends ABSequence> void getCurrentValue(final SequenceCallback callback, final AB.SequenceFetchOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getCurrentValue(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスの現在値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param options {@link AB.SequenceFetchOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1270">アピアリーズドキュメント &raquo; シーケンス取得</a>
     */
    public <T extends ABSequence> void getCurrentValue(final SequenceCallback callback, final EnumSet<AB.SequenceFetchOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getCurrentValue(obj, callback, options);
    }

//endregion

//region Increment

    /**
     * 同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> incrementSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.incrementSynchronously(obj, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> incrementSynchronously(final AB.SequenceAddOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.incrementSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> incrementSynchronously(final EnumSet<AB.SequenceAddOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.incrementSynchronously(obj, options);
    }

    /**
     * 非同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void increment() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.increment(obj, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void increment(final AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.increment(obj, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void increment(final EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.increment(obj, options);
    }

    /**
     * 非同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void increment(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.increment(obj, callback, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void increment(final ResultCallback<T> callback, final AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.increment(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスをインクリメント（シーケンスの現在値に+1）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void increment(final ResultCallback<T> callback, final EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.increment(obj, callback, options);
    }

//endregion

//region Get Next Value

    /**
     * 同期モードでシーケンスの現在値に+1した値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値に+1した値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getNextValueSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getNextValueSynchronously(obj, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 同期モードでシーケンスの現在値に+1した値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値に+1した値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getNextValueSynchronously(final AB.SequenceAddOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getNextValueSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでシーケンスの現在値に+1した値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値に+1した値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getNextValueSynchronously(final EnumSet<AB.SequenceAddOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getNextValueSynchronously(obj, options);
    }

    /**
     * 非同期モードでシーケンスの現在値に+1した値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getNextValue(final SequenceCallback callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getNextValue(obj, callback, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスの現在値に+1した値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getNextValue(final SequenceCallback callback, final AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getNextValue(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスの現在値に+1した値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getNextValue(final SequenceCallback callback, final EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getNextValue(obj, callback, options);
    }

//endregion

//region Decrement

    /**
     * 同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> decrementSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.decrementSynchronously(obj, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> decrementSynchronously(final AB.SequenceAddOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.decrementSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> decrementSynchronously(final EnumSet<AB.SequenceAddOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.decrementSynchronously(obj, options);
    }

    /**
     * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void decrement() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.decrement(obj, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void decrement(final AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.decrement(obj, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void decrement(final EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.decrement(obj, options);
    }

    /**
     * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void decrement(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.decrement(obj, callback, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void decrement(final ResultCallback<T> callback, final AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.decrement(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスをデクリメント（シーケンスの現在値を-1）します。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void decrement(final ResultCallback<T> callback, final EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.decrement(obj, callback, options);
    }

//endregion

//region Get Previous Value

    /**
     * 同期モードでシーケンスの現在値を-1した値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値を-1した値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getPreviousValueSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getPreviousValueSynchronously(obj, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 同期モードでシーケンスの現在値を-1した値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値を-1した値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getPreviousValueSynchronously(final AB.SequenceAddOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getPreviousValueSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでシーケンスの現在値を-1した値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスの現在値を-1した値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getPreviousValueSynchronously(final EnumSet<AB.SequenceAddOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getPreviousValueSynchronously(obj, options);
    }

    /**
     * 非同期モードでシーケンスの現在値を-1した値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getPreviousValue(final SequenceCallback callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getPreviousValue(obj, callback, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスの現在値を-1した値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getPreviousValue(final SequenceCallback callback, final AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getPreviousValue(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスの現在値を-1した値を取得します。
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getPreviousValue(final SequenceCallback callback, final EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getPreviousValue(obj, callback, options);
    }

//endregion

//region Get Value With Adding Amount

    /**
     * 同期モードでシーケンスに値を加算（減算）した計算後の値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param addingAmount シーケンスの現在値に対して加算（減算）する値
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスに値を加算（減算）した計算後の値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getValueSynchronously(final long addingAmount) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getValueSynchronously(obj, addingAmount, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 同期モードでシーケンスに値を加算（減算）した計算後の値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param addingAmount シーケンスの現在値に対して加算（減算）する値
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスに値を加算（減算）した計算後の値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getValueSynchronously(final long addingAmount, final AB.SequenceAddOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getValueSynchronously(obj, addingAmount, EnumSet.of(option));
    }

    /**
     * 同期モードでシーケンスに値を加算（減算）した計算後の値を取得します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param addingAmount シーケンスの現在値に対して加算（減算）する値
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return シーケンスに値を加算（減算）した計算後の値
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> long getValueSynchronously(final long addingAmount, final EnumSet<AB.SequenceAddOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.getValueSynchronously(obj, addingAmount, options);
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）した後の計算後の値を取得します。
     * @param addingAmount シーケンスの現在値に対して加算（減算）する値
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getValue(final long addingAmount, final SequenceCallback callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getValue(obj, addingAmount, callback, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）した後の計算後の値を取得します。
     * @param addingAmount シーケンスの現在値に対して加算（減算）する値
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getValue(final long addingAmount, final SequenceCallback callback, AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getValue(obj, addingAmount, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）した後の計算後の値を取得します。
     * @param addingAmount シーケンスの現在値に対して加算（減算）する値
     * @param callback 実行結果に long 値を返すコールバックハンドラ
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void getValue(final long addingAmount, final SequenceCallback callback, EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.getValue(obj, addingAmount, callback, options);
    }

//endregion

//region Add

    /**
     * 同期モードでシーケンスに値を加算（減算）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> addSynchronously(final long amount) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.addSynchronously(obj, amount, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 同期モードでシーケンスに値を加算（減算）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> addSynchronously(final long amount, final AB.SequenceAddOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.addSynchronously(obj, amount, EnumSet.of(option));
    }

    /**
     * 同期モードでシーケンスに値を加算（減算）します。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> ABResult<T> addSynchronously(final long amount, final EnumSet<AB.SequenceAddOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.addSynchronously(obj, amount, options);
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）します。
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void add(final long amount) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.add(obj, amount, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）します。
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void add(final long amount, final AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.add(obj, amount, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）します。
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void add(final long amount, final EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.add(obj, amount, options);
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）します。
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void add(final long amount, final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.add(obj, amount, callback, EnumSet.of(AB.SequenceAddOption.NONE));
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）します。
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.SequenceAddOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void add(final long amount, final ResultCallback<T> callback, final AB.SequenceAddOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.add(obj, amount, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンスに値を加算（減算）します。
     * @param amount シーケンスの現在値に対して加算（減算）する値
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.SequenceAddOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @see <a href="http://docs.appiaries.com/?p=1260">アピアリーズドキュメント &raquo; シーケンス発行</a>
     */
    public <T extends ABSequence> void add(final long amount, final ResultCallback<T> callback, final EnumSet<AB.SequenceAddOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.add(obj, amount, callback, options);
    }

//endregion

//region Reset

    /**
     * 同期モードでシーケンス・オブジェクトをリセットします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     */
    public <T extends ABSequence> ABResult<T> resetSynchronously() throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.resetSynchronously(obj, EnumSet.of(AB.SequenceResetOption.NONE));
    }

    /**
     * 同期モードでシーケンス・オブジェクトをリセットします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param option {@link AB.SequenceResetOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     */
    public <T extends ABSequence> ABResult<T> resetSynchronously(final AB.SequenceResetOption option) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.resetSynchronously(obj, EnumSet.of(option));
    }

    /**
     * 同期モードでシーケンス・オブジェクトをリセットします。
     * <div class="important">メインスレッド上で実行した場合、処理結果が返却されるまでUI描画処理がブロックされる点にご注意ください。</div>
     * @param options {@link AB.SequenceResetOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     * @return {@link ABResult} オブジェクト
     * @throws ABException 処理中にエラーが発生した場合にスロー
     */
    public <T extends ABSequence> ABResult<T> resetSynchronously(final EnumSet<AB.SequenceResetOption> options) throws ABException {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        return AB.SequenceService.resetSynchronously(obj, options);
    }

    /**
     * 非同期モードでシーケンス・オブジェクトをリセットします。
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     */
    public <T extends ABSequence> void reset() {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.reset(obj, null, EnumSet.of(AB.SequenceResetOption.NONE));
    }

    /**
     * 非同期モードでシーケンス・オブジェクトをリセットします。
     * @param option {@link AB.SequenceResetOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     */
    public <T extends ABSequence> void reset(final AB.SequenceResetOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.reset(obj, null, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンス・オブジェクトをリセットします。
     * @param options {@link AB.SequenceResetOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     */
    public <T extends ABSequence> void reset(final EnumSet<AB.SequenceResetOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.reset(obj, null, options);
    }

    /**
     * 非同期モードでシーケンス・オブジェクトをリセットします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     */
    public <T extends ABSequence> void reset(final ResultCallback<T> callback) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.reset(obj, callback, EnumSet.of(AB.SequenceResetOption.NONE));
    }

    /**
     * 非同期モードでシーケンス・オブジェクトをリセットします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param option {@link AB.SequenceResetOption} オプション
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     */
    public <T extends ABSequence> void reset(final ResultCallback<T> callback, final AB.SequenceResetOption option) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.reset(obj, callback, EnumSet.of(option));
    }

    /**
     * 非同期モードでシーケンス・オブジェクトをリセットします。
     * @param callback 実行結果に {@link ABResult} を返すコールバックハンドラ
     * @param options {@link AB.SequenceResetOption} オプション群
     * @param <T> {@link ABSequence} クラス (またはその派生クラス)
     */
    public <T extends ABSequence> void reset(final ResultCallback<T> callback, final EnumSet<AB.SequenceResetOption> options) {
        @SuppressWarnings("unchecked") T obj = (T)this; //XXX: unsafe cast
        AB.SequenceService.reset(obj, callback, options);
    }

//endregion

//region Miscellaneous

    /**
     * シーケンス・オブジェクト検索用のクエリオブジェクトを取得します。
     * @return {@link ABQuery} オブジェクト
     */
    public static ABQuery<? extends ABSequence> query() {
        return ABQuery.query(ABSequence.class);
    }

//endregion

//region Accessors

    /**
     * シーケンス値（現在値）を取得します。
     * @return シーケンス値（現在値）
     */
    public long getValue() {
        return get(Field.VALUE);
    }

    /**
     * シーケンス値（現在値）をセットします。
     * @param value シーケンス値（現在値）
     */
    public void setValue(long value) {
        put(Field.VALUE, value);
    }

    /**
     * 初期値を取得します。
     * @return 初期値
     */
    public long getInitialValue() {
        return get(Field.INITIAL_VALUE);
    }

    /**
     * 初期値をセットします。
     * @param value 初期値
     */
    public void setInitialValue(long value) {
        put(Field.INITIAL_VALUE, value);
    }

//endregion

//region Cloneable

    /**
     * シーケンス・オブジェクトを複製します。
     * @return 複製した {@link ABSequence} オブジェクト
     * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
     * @see java.lang.Object#clone()
     */
    public ABSequence clone() throws CloneNotSupportedException {
        return (ABSequence)super.clone();
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
