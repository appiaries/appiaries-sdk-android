//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import java.util.SortedSet;

/**
 * アピアリーズ BaaS 例外。
 * @see <a href="http://docs.appiaries.com/?p=1140#i-5">アピアリーズドキュメント &raquo; 共通エラー</a>
 */
public class ABException extends Exception {

    private static final long serialVersionUID = 1L;

    private int mCode;
    private SortedSet<String> mDetailCodes;
    private SortedSet<String> mErrorFields;


    public ABException(Throwable t) {
        super(t);
    }

    public ABException(int code) {
        super(getErrorMessage(null, getDefaultMessage(code), code, null, null));
        mCode = code;
    }
    public ABException(int code, String message) {
        super(getErrorMessage(message, getDefaultMessage(code), code, null, null));
        mCode = code;
    }

    public ABException(int code, SortedSet<String> detailCodes) {
        super(getErrorMessage(null, getDefaultMessage(code), code, detailCodes, null));
        mCode = code;
        mDetailCodes = detailCodes;
    }
    public ABException(int code, String message, SortedSet<String> detailCodes) {
        super(getErrorMessage(message, getDefaultMessage(code), code, detailCodes, null));
        mCode = code;
        mDetailCodes = detailCodes;
    }

    public ABException(int code, SortedSet<String> detailCodes, SortedSet<String> errorFields) {
        super(getErrorMessage(null, getDefaultMessage(code), code, detailCodes, errorFields));
        mCode = code;
        mDetailCodes = detailCodes;
        mErrorFields = errorFields;
    }
    public ABException(int code, String message, SortedSet<String> detailCodes, SortedSet<String> errorFields) {
        super(getErrorMessage(message, getDefaultMessage(code), code, detailCodes, errorFields));
        mCode = code;
        mDetailCodes = detailCodes;
        mErrorFields = errorFields;
    }

    public int getCode() {
        return mCode;
    }

    public SortedSet<String> getDetailCodes() {
        return mDetailCodes;
    }

    public SortedSet<String> getErrorFields() {
        return mErrorFields;
    }

    private static String getErrorMessage(String reason, String defaultReason, int code, SortedSet<String> detailCodes, SortedSet<String> errorFields) {
        StringBuilder buff = new StringBuilder();
        if (reason != null && reason.length() > 0) {
            buff.append(reason);
        } else {
            buff.append(defaultReason);
        }
        if (detailCodes != null) {
            if (detailCodes.size() == 0) {
                buff.append(String.format("(%d)", code));
            } else { //count >= 1
                String detailCodesStr = getJoinedDetailCodes(detailCodes);
                if (detailCodesStr.length() > 0) {
                    buff.append("(").append(code).append(") [detail:").append(detailCodesStr).append("]"); //" (%d) [detail: %s]"
                }
            }
        }
        if (errorFields != null && errorFields.size() > 0) {
            String errorFieldsStr = getJoinedErrorFields(errorFields);
            if (errorFieldsStr.length() > 0) {
                buff.append(" [field(s):").append(errorFieldsStr).append("]");
            }
        }
        return buff.toString();
    }
    private static String getJoinedDetailCodes(SortedSet<String> detailCodes) {
        StringBuilder buff = new StringBuilder();
        for (String dCode : detailCodes) {
            if (buff.length() > 0) buff.append(",");
            String dMsg = getDetailMessageForDetailCode(dCode);
            if (dMsg != null) buff.append(dMsg);
            buff.append("(").append(dCode).append(")");
        }
        return buff.length() > 0 ? buff.toString() : null;
    }
    private static String getJoinedErrorFields(SortedSet<String> errorFields) {
        StringBuilder buff = new StringBuilder();
        for (String f : errorFields) {
            if (buff.length() > 0) buff.append(",");
            buff.append(f);
        }
        return buff.length() > 0 ? buff.toString() : null;
    }

    static String getDefaultMessage(int code) {
        if (200 <= code && code <= 399) return null;
        switch (code) {
            case 400   : return "Bad Request";
            case 401   : return "Unauthorized";
            case 402   : return "Payment Required";
            case 403   : return "Forbidden";
            case 404   : return "Not Found";
            case 405   : return "Method Not Allowed";
            case 406   : return "Not Acceptable";
            case 407   : return "Proxy Authentication Required";
            case 408   : return "Request Timeout";
            case 409   : return "Conflict";
            case 410   : return "Gone";
            case 411   : return "Length Required";
            case 412   : return "Precondition Failed";
            case 413   : return "Request Entity Too Large";
            case 414   : return "Request-URI Too Long";
            case 415   : return "Unsupported Media Type";
            case 416   : return "Requested Range Not Satisfiable";
            case 417   : return "Expectation Failed";
            case 418   : return "I'm teapot";
            case 422   : return "Unprocessable Entity";
            case 423   : return "Locked";
            case 424   : return "Failed Dependency";
            case 426   : return "Upgrade Required";
            case 500   : return "Internal Server Error";
            case 501   : return "Not Implemented";
            case 502   : return "Bad Gateway";
            case 503   : return "Service Unavailable";
            case 504   : return "Gateway Timeout";
            case 505   : return "HTTP Version Not Supported";
            case 506   : return "Variant Aso Negotiates";
            case 507   : return "Insufficient Storage";
            case 509   : return "Bandwidth Limit Exceeded";
            case 510   : return "Not Extended";
            // =========== CUSTOM CODES ===========
            case 1001  : return "Network Communication Error";
            case -1    : return "Invalid ID";
            case -1001 : return "Invalid Token";
            case -1002 : return "Invalid Path Segment";
            case -1003 : return "Invalid Argument";
            case -1004 : return "Operation Cancelled";
            // =========== CUSTOM CODES ===========
            default    : return "Unknown Error";
        }
    }

    //以下の詳細エラーコードは、レスポンス・コードが 409,422,423 の場合に返却される
    //TODO: XXX: エラーコードに対応するメッセージから、恐らくエラー原因を特定することはできない。また、エラーコードが網羅されていない。
    private static String getDetailMessageForDetailCode(String detailCode) {
    /* ================================================ Token === */
        if ("0010001".equals(detailCode)) return "path-segments too short";
        if ("0010002".equals(detailCode)) return "invalid path-segment name";
        if ("0010101".equals(detailCode)) return "invalid account"; //認証に失敗しました。
        if ("0010103".equals(detailCode)) return "account locked"; //アカウントロックが発生しました。
        if ("0010104".equals(detailCode)) return "account locking"; //アカウントロック状態です。
        if ("0010105".equals(detailCode)) return "account locking (too much failure)"; //多数の認証失敗が発生したアカウントロック状態です。
        if ("0010304".equals(detailCode)) return "contains invalid character"; //パスワードに使用できない文字が指定されました。
    /* ================================================= User === */
        if ("0020001".equals(detailCode)) return "path-segments too short"; //必須キーが指定されていません。
        if ("0020002".equals(detailCode)) return "invalid path-segment name"; //予約語が指定されています。
        if ("0020003".equals(detailCode)) return "unmodifiable path-segment name"; //更新不可能キーが指定されています。
        if ("0020201".equals(detailCode)) return "blank or null value"; //ログインIDに空文字またはnullが指定されています。
        if ("0020202".equals(detailCode)) return "invalid data type"; //ログインIDに文字列以外のデータ型が指定されています。
        if ("0020203".equals(detailCode)) return "contains special character"; //ログインIDにLF,CR,TABを除く制御文字が含まれています。
        if ("0020209".equals(detailCode)) return "duplicated"; //ログインIDが重複しています。
        if ("0020301".equals(detailCode)) return "blank or null value";  //パスワードに空文字またはnullが指定されています。
        if ("0020302".equals(detailCode)) return "invalid data type"; //パスワードに文字列以外のデータ型が指定されています。
        if ("0020304".equals(detailCode)) return "contains special character"; //パスワードにLF,CR,TABを除く制御文字が含まれています。
        if ("0020401".equals(detailCode)) return "blank or null value";  //メールアドレスに空文字またはnullが指定されています。
        if ("0020402".equals(detailCode)) return "invalid data type"; //メールアドレスに文字列以外のデータ型が指定されています。
        if ("0020405".equals(detailCode)) return "invalid format"; //メールアドレスの形式に誤りがあります。
        if ("0020409".equals(detailCode)) return "duplicated"; //メールアドレスが重複しています。
    /* ============================================= Contract === */
        if ("0030001".equals(detailCode)) return "path-segments too short";
        if ("0030002".equals(detailCode)) return "invalid path-segment name";
        if ("0030003".equals(detailCode)) return "unmodifiable path-segment name";
    /* ============================================== Service === */
        if ("0040001".equals(detailCode)) return "path-segments too short";
        if ("0040002".equals(detailCode)) return "invalid path-segment name";
        if ("0040003".equals(detailCode)) return "unmodifiable path-segment name";
    /* ================================ Sequence / Collection === */
        if ("0050001".equals(detailCode)) return "path-segment too short";
        if ("0050002".equals(detailCode)) return "invalid path-segment name";
    /* ====================================== Document (text) === */
        if ("0060001".equals(detailCode)) return "path-segments too short";
        if ("0060002".equals(detailCode)) return "invalid path-segment name";
        if ("0060003".equals(detailCode)) return "unmodifiable path-segment";
    /* ==================================== Document (binary) === */
        if ("0070001".equals(detailCode)) return "path-segments too short";
        if ("0070002".equals(detailCode)) return "invalid path-segment name";
        if ("0070003".equals(detailCode)) return "unmodifiable path-segment";
    /* ================================================= Push === */
        if ("0080001".equals(detailCode)) return "path-segments too short";
        if ("0080002".equals(detailCode)) return "invalid path-segment name";
    /* ================================================ Scope === */
        if ("0090001".equals(detailCode)) return "path-segments too short";
        if ("0090002".equals(detailCode)) return "invalid path-segment name";
    /* ============================================= Discount === */
        if ("0100001".equals(detailCode)) return "path-segments too short";
        if ("0100002".equals(detailCode)) return "invalid path-segment name";
        if ("0100003".equals(detailCode)) return "unmodifiable path-segment";
    /* =========================================== Anonymous? === */
        if ("0119001".equals(detailCode)) return "unknown error";
    /* ======================================== Payment Order === */
        if ("0150001".equals(detailCode)) return "path-segments too short";
        if ("0150002".equals(detailCode)) return "invalid path-segment name";
    /* ============================================= Sequence === */
        if ("0160001".equals(detailCode)) return "path-segments too short";
        if ("0160002".equals(detailCode)) return "invalid path-segment name";
    /* ============================================= Template === */
        if ("0170001".equals(detailCode)) return "path-segments too short";
        if ("0170002".equals(detailCode)) return "invalid path-segment name";
        if ("0170003".equals(detailCode)) return "unmodifiable path-segment";

        return null;
    }

}
