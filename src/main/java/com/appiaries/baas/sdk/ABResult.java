//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 結果オブジェクト。
 * <p></p>
 * @param <T>
 * @version 2.0.0
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class ABResult<T> {

    static String EXTRA_KEY_REQUEST = "_request";
    static String EXTRA_KEY_RESPONSE = "_response";
    static String EXTRA_KEY_EXCEPTION = "_exception";
    static String EXTRA_KEY_DOWNLOAD_FILE_PATH = "_download_filepath";

    private T mData;
    private byte[] mRawData;
    private int mCode;
    private Map<String, Object> mExtra;

    private long mTotal;
    private long mStart;
    private long mEnd;
    private boolean mNext;
    private boolean mPrevious;

    /**
     * デフォルト・コンストラクタ。
     */
    public ABResult() {
        mExtra = new HashMap<>();
    }

    /**
     * パース済み JSON レスポンスを取得します。
     * @return パース済み JSON レスポンス
     */
    public T getData() {
        return mData;
    }

    /**
     * パース済み JSON レスポンスをセットします。
     * @param data パース済み JSON レスポンス
     */
    public void setData(T data) {
        this.mData = data;
    }

    /**
     * REST API から返却された未加工のレスポンスBODYデータを取得します。
     * @return 未加工のレスポンスBODYデータ
     * @deprecated use #getDownloadFilePath() instead.
     */
    public byte[] getRawData() {
        return mRawData;
    }

    /**
     * REST API から返却された未加工のレスポンスBODYデータをセットします。
     * Response の Content-Type が "application/json" 以外であった場合に格納されます。
     * @param rawData 未加工のレスポンスBODYデータ
     * @deprecated use #getDownloadFilePath() instead.
     */
    public void setRawData(byte[] rawData) {
        this.mRawData = rawData;
    }

    /**
     * レスポンス・コードを取得します。
     * @return レスポンス・コード
     */
    public int getCode() {
        return mCode;
    }

    /**
     * レスポンス・コードをセットします。
     * @param code レスポンス・コード
     */
    public void setCode(int code) {
        this.mCode = code;
    }

    /**
     * 任意用途で利用可能な拡張データを取得します。
     * @return 拡張データ
     */
    public Map<String, Object> getExtra() {
        return mExtra;
    }

    /**
     * 任意用途で利用可能な拡張データをセットします。
     * @param extra 拡張データ
     */
    public void setExtra(Map<String, Object> extra) {
        this.mExtra = extra;
    }

    /**
     * 任意用途で利用可能な拡張データに指定キー／値をセットします。
     * @param key キー
     * @param value 値
     */
    public void putExtra(String key, Object value) {
        this.mExtra.put(key, value);
    }

    /**
     * ダウンロードファイル・パスを取得します。
     * <p>
     * Context#getCacheDir() (拡張ストレージが存在する場合は Context#getExtendedCacheDir()) にダウンロードされたファイルのパスを返します。<br>
     * アプリからは、FileInputStream などを使用してファイルデータを取得してください。<br>
     * 尚、ファイルのダウンロードや保存に失敗した場合は null が返却されます。
     * </p>
     * @return ダウンロードファイル・パス
     * @since 2.0.8
     */
    public String getDownloadFilePath() {
        if (mExtra.containsKey(ABResult.EXTRA_KEY_DOWNLOAD_FILE_PATH)) {
            return (String)mExtra.get(ABResult.EXTRA_KEY_DOWNLOAD_FILE_PATH);
        }
        return null;
    }

    /**
     * 取得総件数を取得します。
     * @return 取得総件数
     */
    public long getTotal() {
        return mTotal;
    }

    /**
     * 取得総件数をセットします。
     * @param total 取得総件数
     */
    public void setTotal(long total) {
        this.mTotal = total;
    }

    /**
     * 取得データの先頭インデックスを取得します。
     * @return 取得データの先頭インデックス
     */
    public long getStart() {
        return mStart;
    }

    /**
     * 取得データの先頭インデックスをセットします。
     * @param start 取得データの先頭インデックス
     */
    public void setStart(long start) {
        this.mStart = start;
    }

    /**
     * 取得データの末尾インデックスを取得します。
     * @return 取得データの末尾インデックス
     */
    public long getEnd() {
        return mEnd;
    }

    /**
     * 取得データの末尾インデックスをセットします。
     * @param end 取得データの末尾インデックス
     */
    public void setEnd(long end) {
        this.mEnd = end;
    }

    /**
     * 取得データの末尾インデックス以降にデータが存在するかどうかを取得します。
     * @return YES:存在する, NO:存在しない
     */
    public boolean hasNext() {
        return mNext;
    }

    /**
     * 取得データの末尾インデックス以降にデータが存在するかどうかをセットします。
     * @param next YES:存在する, NO:存在しない
     */
    public void setNext(boolean next) {
        this.mNext = next;
    }

    /**
     * 取得データの先頭インデックス以前にデータが存在するかどうかを取得します。
     * @return YES:存在する, NO:存在しない
     */
    public boolean hasPrevious() {
        return mPrevious;
    }

    /**
     * 取得データの先頭インデックス以前にデータが存在するかどうかをセットします。
     * @param previous YES:存在する, NO:存在しない
     */
    public void setPrevious(boolean previous) {
        this.mPrevious = previous;
    }

}
