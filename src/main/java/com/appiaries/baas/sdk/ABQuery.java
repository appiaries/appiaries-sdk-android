//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * クエリ・オブジェクト。
 * <p>アピアリーズ BaaS 上の各種データを検索する際に使用する汎用クエリオブジェクトです。</p>
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="http://docs.appiaries.com/?p=1280">アピアリーズドキュメント &raquo; データ検索における条件式</a>
 */
public class ABQuery<T extends ABModel> implements Cloneable {
    private static String TAG = ABQuery.class.getSimpleName();

    public static final String CONDITION_SEPARATOR = ";";

    public static final String PARAM_SELECTED_FIELDS_KEY = "sel";
    public static final String PARAM_EXCLUDED_FIELDS_KEY = "excpt";
    public static final String PARAM_OBJECT_ONLY_KEY = "oonly";
    public static final String PARAM_GET_KEY = "get";
    public static final String PARAM_PROCESS_KEY = "proc";
    public static final String PARAM_SORT_ORDER_KEY = "order";
    public static final String PARAM_DEPTH_KEY = "depth";
    public static final String PARAM_PROCESS_VALUE_COUNT_KEY = "count";

    public static final String OPERATOR_EXISTS = "exist";
    public static final String OPERATOR_IS = "is";
    public static final String OPERATOR_IS_NOT = "isn";
    public static final String OPERATOR_EQUALS_TO = "eq";
    public static final String OPERATOR_NOT_EQUALS_TO = "neq";
    public static final String OPERATOR_LESS_THAN = "lt";
    public static final String OPERATOR_LESS_THAN_OR_EQUALS_TO = "lte";
    public static final String OPERATOR_GREATER_THAN = "gt";
    public static final String OPERATOR_GREATER_THAN_OR_EQUALS_TO = "gte";
    public static final String OPERATOR_STARTS_WITH = "sw";
    public static final String OPERATOR_STARTS_WITH_CASE_INSENSITIVE = "swi";
    public static final String OPERATOR_NOT_STARTS_WITH = "nsw";
    public static final String OPERATOR_NOT_STARTS_WITH_INSENSITIVE = "nswi";
    public static final String OPERATOR_ENDS_WITH = "ew";
    public static final String OPERATOR_ENDS_WITH_INSENSITIVE = "ewi";
    public static final String OPERATOR_NOT_ENDS_WITH = "new";
    public static final String OPERATOR_NOT_ENDS_WITH_INSENSITIVE = "newi";
    public static final String OPERATOR_LIKE = "li";
    public static final String OPERATOR_LIKE_CASE_INSENSITIVE = "lii";
    public static final String OPERATOR_NOT_LIKE = "nli";
    public static final String OPERATOR_NOT_LIKE_CASE_INSENSITIVE = "nlii";
    protected static final String OPERATOR_BETWEEN = "btw";
    public static final String OPERATOR_CONTAINS = "in";
    public static final String OPERATOR_NOT_CONTAINS = "nin";
    public static final String OPERATOR_WITHIN_CIRCLE = "wic";
    public static final String OPERATOR_WITHIN_BOX = "wib";
    public static final String OPERATOR_WITHIN_POLYGON = "wip";

    /*
     * 条件オペレータ。
     */
    /*public enum ConditionOperator {
        EXISTS,
        IS,
        IS_NOT,
        EQUALS_TO,
        NOT_EQUALS_TO,
        LESS_THAN,
        LESS_THAN_OR_EQUALS_TO,
        GREATER_THAN,
        GREATER_THAN_OR_EQUALS_TO,
        STARTS_WITH,
        STARTS_WITH_CASE_INSENSITIVE,
        NOT_STARTS_WITH,
        NOT_STARTS_WITH_CASE_INSENSITIVE,
        ENDS_WITH,
        ENDS_WITH_CASE_INSENSITIVE,
        NOT_ENDS_WITH,
        NOT_ENDS_WITH_CASE_INSENSITIVE,
        LIKE,
        LIKE_CASE_INSENSITIVE,
        NOT_LIKE,
        NOT_LIKE_CASE_INSENSITIVE,
        BETWEEN,
        CONTAINS,
        NOT_CONTAINS,
        WITHIN_CIRCLE,
        WITHIN_BOX,
        WITHIN_POLYGON,
    }*/

    /**
     * 条件連結子。
     */
    public enum Conjunction {
        /** AND で条件を連結します。 */
        AND,
        /** OR で条件を連結します。 */
        OR
    }

    /**
     * ソート方向。
     */
    public enum SortDirection {
        /** 検索結果を指定キーで昇順ソートします。 */
        ASC,
        /** 検索結果を指定キーで降順ソートします。 */
        DESC
    }

    /**
     * 正規表現オプション。
     */
    public enum RegexOption {
        /** 未指定。(CASE_SENSITIVE が指定されたものと見なされる) */
        NONE,
        /** 文字列比較をする際に大小文字を無視します。 */
        CASE_INSENSITIVE,
        /** 文字列比較をする際に大小文字を認識します。 */
        CASE_SENSITIVE,
    }

    private List<ConditionBundle> mConditionBundles;

    private String mCollectionID;
    private List<String> mSelectedFields;
    private List<String> mExcludedFields;
    private List<String> mOrderByArray;
    private String mProcess;
    private boolean mObjectOnly;
    private long mLimit;
    private long mSkip;
    private int mDepth;

    /**
     * デフォルト・コンストラクタ。
     */
    public ABQuery() {
        mConditionBundles = new ArrayList<>();
        mSelectedFields   = new ArrayList<>();
        mExcludedFields   = new ArrayList<>();
        mOrderByArray     = new ArrayList<>();
        mProcess    = null;
        mObjectOnly = false;
        mLimit = -1;
        mSkip  = -1;
        mDepth = -1;
    }

    /**
     * コンストラクタ。
     * <p>引数にコレクションIDを取ります。</p>
     * @param collectionID コレクションID
     */
    public ABQuery(String collectionID) {
        this();
        mCollectionID = collectionID;
    }

    /**
     * コンストラクタ。
     * <p>引数にモデルクラスを取ります。</p>
     * @param clazz {@link ABModel} クラスの派生クラス
     */
    public ABQuery(Class<? extends ABModel> clazz) {
        this(AB.ClassRepository.getCollectionID(clazz));
    }

    /**
     * インスタンス生成。
     * <p>新しい ABQuery オブジェクトのインスタンスを取得します。</p>
     * @param clazz モデルクラス
     * @param <T> {@link ABModel} クラスの派生クラス
     * @return {@link ABQuery} インスタンス
     */
    public static <T extends ABModel> ABQuery<T> query(Class<T> clazz) {
        ABQuery<T> query = new ABQuery<>(clazz);
        return query;
    }
/*
    public static <T extends ABModel> ABQuery query(Class<T> clazz) {
        ABCollection collection = clazz.getAnnotation(ABCollection.class);
        if (collection != null) {
            return new ABQuery(collection.value());
        } else {
            return null;
        }
    }
*/
    /* FUTURE IMPLEMENTATION: Query を実行する (classメソッド)
    public static void execute(ABQuery query) { }
    */

    /**
     * 取得フィールドの選択。
     * <p>抽出対象データが保持するすべてのフィールドを返却するよう指示します。</p>
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery selectAll() {
        mSelectedFields.clear();
        mExcludedFields.clear();
        mProcess = null;
        mObjectOnly = false;
        return this;
    }

    /**
     * 取得フィールドの選択。
     * <p>指定した除外対象フィールドを除くすべてのフィールドを返却するよう指示します。</p>
     * @param excludedFields 除外対象フィールドリスト
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery selectAllWithExcludedFields(List<String> excludedFields) {
        mSelectedFields.clear();
        mExcludedFields.clear();
        mExcludedFields.addAll(excludedFields);
        mProcess = null;
        mObjectOnly = false;
        return this;
    }

    /**
     * 取得フィールドの選択。
     * <p>指定した選択フィールドのみ返却するよう指示します。</p>
     * @param fields 選択フィールドリスト
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery select(List<String> fields) {
        mSelectedFields.clear();
        mSelectedFields.addAll(fields);
        mExcludedFields.clear();
        mProcess = null;
        mObjectOnly = false;
        return this;
    }

    /**
     * 取得フィールドの選択。
     * <p>すべてのフィールドを含むオブジェクトを取得するよう指示します。<br>検索結果（{@link ABResult}）に付随情報（total/start/end/previous/next）はセットされません。</p>
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery selectAllObjects() {
        selectAll();
        mObjectOnly = true;
        return this;
    }

    /**
     * 取得フィールドの選択。
     * <p>指定した除外フィールドを除く、すべてのフィールドを含むオブジェクトを取得するよう指示します。<br>検索結果（{@link ABResult}）に付随情報（total/start/end/previous/next）はセットされません。</p>
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery selectAllObjectsWithExcludedFields(List<String> excludedFields) {
        selectAllWithExcludedFields(excludedFields);
        mObjectOnly = true;
        return this;
    }

    /**
     * 取得フィールドの選択。
     * <p>指定した選択フィールドのみを含むオブジェクトを取得するよう指示します。<br>検索結果（{@link ABResult}）に付随情報（total/start/end/previous/next）はセットされません。</p>
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery selectObjects(List<String> fields) {
        select(fields);
        mObjectOnly = true;
        return this;
    }

    /**
     * 件数取得を指定します。
     * <p>検索の結果として抽出件数を返却するよう指示します。</p>
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery count() {
        selectAll();
        mProcess = ABQuery.PARAM_PROCESS_VALUE_COUNT_KEY;
        return this;
    }

    /**
     * 取得対象コレクションを指定します。
     * @param collectionID コレクションID
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery from(String collectionID) {
        mCollectionID = collectionID;
        return this;
    }

    /* FUTURE IMPLEMENTATION:  サブクエリ指定の実装
    public ABQuery fromQuery(ABQuery query) {
        mCollectionID = query.getCollectionID();
        return this;
    }
    */

    /**
     * 検索条件のフィールドを指定します。
     * <p>既に他の条件が指定されている場合は、それら条件に AND 連結されます。</p>
     * @param field フィールド
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery.Condition where(ABField field) {
        return new ABQuery.Condition(this, Conjunction.AND, field.getKey());
    }

    /**
     * 検索条件のフィールドを指定します。
     * <p>既に他の条件が指定されている場合は、それら条件に AND 連結されます。</p>
     * @param key フィールド名
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery.Condition where(String key) {
        return new ABQuery.Condition(this, Conjunction.AND, key);
    }

    /**
     * 検索条件のフィールドを指定します。
     * <p>既に他の条件が指定されている場合は、それら条件に AND 連結されます。</p>
     * @param field フィールド
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery.Condition and(ABField field) {
        return new ABQuery.Condition(this, Conjunction.AND, field.getKey());
    }

    /**
     * 検索条件のフィールドを指定します。
     * <p>既に他の条件が指定されている場合は、それら条件に AND 連結されます。</p>
     * @param key フィールド名
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery.Condition and(String key) {
        return new ABQuery.Condition(this, Conjunction.AND, key);
    }

    /**
     * 検索条件のフィールドを指定します。
     * <p>既に他の条件が指定されている場合は、それら条件に OR 連結されます。</p>
     * @param field フィールド
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery.Condition or(ABField field) {
        return new ABQuery.Condition(this, Conjunction.OR, field.getKey());
    }

    /**
     * 検索条件のフィールドを指定します。
     * <p>既に他の条件が指定されている場合は、それら条件に OR 連結されます。</p>
     * @param key フィールド名
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery.Condition or(String key) {
        return new ABQuery.Condition(this, Conjunction.OR, key);
    }

    /**
     * ソート条件を指定します。
     * @param field フィールド
     * @param direction ソート向き
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery orderBy(ABField field, SortDirection direction) {
        orderBy(Arrays.asList(field.getKey()), Arrays.asList(direction));
        return this;
    }

    /**
     * ソート条件を指定します。
     * @param field フィールド名
     * @param direction ソート向き
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery orderBy(String field, SortDirection direction) {
        orderBy(Arrays.asList(field), Arrays.asList(direction));
        return this;
    }

    //TODO: List<ABFiled> をどうするか。。

    /**
     * ソート条件を指定します。
     * @param fields フィールド名リスト
     * @param directions ソート向きリスト
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery orderBy(List<String> fields, List<SortDirection> directions) {
        if (fields == null || directions == null || fields.size() == 0 ||
                directions.size() == 0 || fields.size() != directions.size()) return this;

        mOrderByArray.clear();

        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            ABQuery.SortDirection direction = directions.get(i);
            if (direction == SortDirection.DESC) {
                mOrderByArray.add(String.format("-%s", field)); //TODO: comma 無いけど大丈夫？ (ObjCも)
            } else {
                mOrderByArray.add(field);
            }
        }
        return this;
    }

    /**
     * 取得上限件数を指定します。
     * @param limit 取得上限件数
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery limit(long limit) {
        limit(limit, -1, -1);
        return this;
    }

    /**
     * 取得上限件数、スキップ件数を指定します。
     * @param limit 取得上限件数
     * @param skip スキップ件数（読み飛ばし件数）
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery limit(long limit, long skip) {
        limit(limit, skip, -1);
        return this;
    }

    /*
     * 取得上限件数、スキップ件数、取得データ深度を指定します。
     * @param limit 取得上限件数
     * @param skip スキップ件数（読み飛ばし件数）
     * @param depth 取得データ深度
     * @return {@link ABQuery} オブジェクト
     */
    protected ABQuery limit(long limit, long skip, Integer depth) {
        if (limit > 0) {
            mLimit = limit;
        }
        if (skip > 0) {
            mSkip = skip;
        }
        if (depth > 0) {
            mDepth = depth;
        }
        return this;
    }

    /**
     * スキップ件数（読み飛ばし件数）を指定します。
     * @param skip スキップ件数（読み飛ばし件数）
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery skip(long skip) {
        limit(-1, skip, -1);
        return this;
    }

    /*
     * スキップ件数（読み飛ばし件数）、取得データ深度を指定します。
     * @param skip スキップ件数（読み飛ばし件数）
     * @param depth 取得データ深度
     * @return {@link ABQuery} オブジェクト
     */
    protected ABQuery skip(long skip, int depth) {
        limit(-1, skip, depth);
        return this;
    }

    /**
     * 複数のクエリ条件を束ねます。
     * <p>SQLにおいて複数条件を1つの塊として扱う際に条件全体を「括弧（カッコ）」で括る、条件指定方法に相当します。</p>
     * @param bundler クエリ条件バンドラ
     * @param conjunction 連結子
     * @return {@link ABQuery} オブジェクト
     */
    public ABQuery bundle(ConditionBundler bundler, Conjunction conjunction) {
        if (bundler != null) {
            ConditionBundle conditionBundle = new ConditionBundle(conjunction);
            conditionBundle = bundler.bundle(conditionBundle);
            if (conditionBundle != null) {
                mConditionBundles.add(conditionBundle);
            }
        }
        return this;
    }

    /* FUTURE IMPLEMENTATION: Query を実行する (instanceメソッド)
    public void execute() { }
    */

    /**
     * オブジェクトの文字列表現を取得します。
     * @return オブジェクトの文字列表現
     */
    public String toString() {
        StringBuilder buff = new StringBuilder();
        if (!TextUtils.isEmpty(mCollectionID)) buff.append(String.format("/%s", mCollectionID));
        buff.append(String.format("%s%s", getConditionString(), getQueryString()));
        return buff.toString();
    }

    /**
     * クエリ文字列（リクエストURLの「?」以降の文字列）を取得します。
     * @return クエリ文字列
     */
    public String getQueryString() {
        StringBuilder buff = new StringBuilder();

        //selectedFields
        assert(mSelectedFields != null);
        String selectedFieldsBuff = TextUtils.join(",", mSelectedFields);
        if (!TextUtils.isEmpty(selectedFieldsBuff)) {
            buff.append(buff.length() > 0 ? "&" : "?");
            buff.append(String.format("%s=%s", ABQuery.PARAM_SELECTED_FIELDS_KEY, selectedFieldsBuff));
        }
        //excludedFields
        String excludedFieldsBuff = TextUtils.join(",", mExcludedFields);
        if (!TextUtils.isEmpty(excludedFieldsBuff)) {
            buff.append(buff.length() > 0 ? "&" : "?");
            buff.append(String.format("%s=%s", ABQuery.PARAM_EXCLUDED_FIELDS_KEY, excludedFieldsBuff));
        }
        //process
        if (ABQuery.PARAM_GET_KEY.equals(mProcess) || ABQuery.PARAM_PROCESS_VALUE_COUNT_KEY.equals(mProcess)) {
            buff.append(buff.length() > 0 ? "&" : "?");
            buff.append(String.format("%s=%s", ABQuery.PARAM_PROCESS_KEY, mProcess));
        }
        //orderBy
        String orderByBuff = TextUtils.join(",", mOrderByArray);
        if (!TextUtils.isEmpty(orderByBuff)) {
            buff.append(buff.length() > 0 ? "&" : "?");
            buff.append(String.format("%s=%s", ABQuery.PARAM_SORT_ORDER_KEY, orderByBuff));
        }
        //objectOnly
        if (mObjectOnly) {
            buff.append(buff.length() > 0 ? "&" : "?");
            buff.append(String.format("%s=true", ABQuery.PARAM_OBJECT_ONLY_KEY));
        }
        //depth
        if (mDepth > 0) {
            buff.append(buff.length() > 0 ? "&" : "?");
            buff.append(String.format("%s=%d", ABQuery.PARAM_DEPTH_KEY, mDepth));
        }

        /*
        String ret = null;
        try {
            ret = encodeStringForUrl(buff.toString());
        } catch (UnsupportedEncodingException e) {
            ABLogger.e(TAG, e.getMessage());
        }
        return ret;
        */
        return buff.toString();
    }

    /**
     * 検索条件文字列（エンドポイントのURL末尾に付与する検索条件指定用パス文字列）を取得します。
     * @return 検索条件文字列
     */
    public String getConditionString() {

        // AND条件、OR条件で、それぞれ配列に振り分ける
        List<ConditionBundle> andConditionBundles = new ArrayList<>();
        List<ConditionBundle> orConditionBundles = new ArrayList<>();
        for (ConditionBundle conditionBundle : mConditionBundles) {
            if (conditionBundle.getConjunction() == Conjunction.OR) {
                orConditionBundles.add(conditionBundle);
            } else {
                andConditionBundles.add(conditionBundle);
            }
        }

        StringBuilder buff = new StringBuilder();

        // AND条件組み立て
        for (ConditionBundle conditionBundle : andConditionBundles) {
            StringBuilder bundleBuff = new StringBuilder();

            Conjunction conjunction = conditionBundle.getConjunction();

            //exists
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mExistsArray);
            //equalsTo
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mEqualsToArray);
            //notEqualsTo
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotEqualsToArray);
            //isTrue, isNull
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mIsArray);
            //lessThan
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mLessThanArray);
            //lessThanOrEqualsTo
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mLessThanOrEqualsToArray);
            //greaterThan
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mGreaterThanArray);
            //greaterThanOrEqualsTo
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mGreaterThanOrEqualsToArray);
            //contains
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mContainsArray);
            //notContains
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotContainsArray);
            //startsWith
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mStartsWithArray);
            //notStartsWith
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotStartsWithArray);
            //endsWith
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mEndsWithArray);
            //notEndsWith
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotEndsWithArray);
            //like
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mLikeArray);
            //notLike
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotLikeArray);
            //between
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mBetweenArray);
            //withinCircle
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mWithinCircleArray);
            //withinBox
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mWithinBoxArray);
            //withinPolygon
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mWithinPolygonArray);

            if (bundleBuff.length() > 0) {
                buff.append(bundleBuff.toString());
            }
        }

        // OR条件組み立て
        for (ConditionBundle conditionBundle : orConditionBundles) {
            StringBuilder bundleBuff = new StringBuilder();

            Conjunction conjunction = conditionBundle.getConjunction();

            //exists
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mExistsArray);
            //equalsTo
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mEqualsToArray);
            //notEqualsTo
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotEqualsToArray);
            //isTrue, isNull
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mIsArray);
            //lessThan
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mLessThanArray);
            //lessThanOrEqualsTo
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mLessThanOrEqualsToArray);
            //greaterThan
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mGreaterThanArray);
            //greaterThanOrEqualsTo
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mGreaterThanOrEqualsToArray);
            //contains
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mContainsArray);
            //notContains
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotContainsArray);
            //startsWith
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mStartsWithArray);
            //notStartsWith
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotStartsWithArray);
            //endsWith
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mEndsWithArray);
            //notEndsWith
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotEndsWithArray);
            //like
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mLikeArray);
            //notLike
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mNotLikeArray);
            //between
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mBetweenArray);
            //withinCirclen
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mWithinCircleArray);
            //withinBox
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mWithinBoxArray);
            //withinPolygon
            appendConditionToBuffer(bundleBuff, conjunction, conditionBundle.mWithinPolygonArray);

            if (bundleBuff.length() > 0) {
                //NOTE: 不要な先頭";"を除去除去し、";or{...}"形式の条件文字列を組み立てている。
                buff.append(";or%7B").append(bundleBuff.substring(1)).append("%7D");
            }
        }

        StringBuilder rangeBuff = new StringBuilder();
        if (mLimit >= 0) {
            if (mSkip >= 0) { // limitが指定 && kipが指定されている場合
                rangeBuff.append(String.format("/%d-%d", mSkip + 1, mLimit + mSkip));
            } else { // limitが指定 && skipが未指定の場合
                rangeBuff.append(String.format("/1-%d", mLimit));
            }
        } else {
            if (mSkip >= 0) { // limitが未指定 && skipが指定されている場合
                rangeBuff.append(String.format("/%d-", mSkip + 1));
            } else { // limitが未指定 && skipも未指定の場合
                rangeBuff.append("/-");
            }
        }
        buff.insert(0, rangeBuff.toString());

        return buff.toString(); //NOTE: iOSの方はここでエンコードをかける
    }

    private void appendConditionToBuffer(StringBuilder buff, Conjunction conjunction, List<String> conditionArray) {
        if (conditionArray == null || conditionArray.size() == 0) return;

        if (conjunction == Conjunction.OR) {
            /*buff.append(String.format(";or{%s}", TextUtils.join(";", conditionArray)));*/ //NOTE: 2015.7時点でネストしたORは非対応なので常にAND連結する。
            buff.append(String.format(";%s", TextUtils.join(";", conditionArray)));
        } else { // AND
            buff.append(String.format(";%s", TextUtils.join(";", conditionArray)));
        }
    }

    /////////////////// Accessors ///////////////////////

    /**
     * コレクションIDを取得します。
     * @return コレクションID
     */
    public String getCollectionID() {
        return mCollectionID;
    }

    /**
     * コレクションIDをセットします。
     * @param collectionID コレクションID
     */
    public void setCollectionID(String collectionID) {
        this.mCollectionID = collectionID;
    }

    //////////// Cloneable ////////////////////

    /**
     * クエリオブジェクトを複製します。
     * @return 複製した {@link ABDBObject} オブジェクト
     * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
     * @see java.lang.Object#clone()
     */
    public ABQuery clone() throws CloneNotSupportedException {
        ABQuery clone = (ABQuery)super.clone();
        clone.setCollectionID(mCollectionID);
        clone.mConditionBundles = new ArrayList<>(mConditionBundles);
        clone.mSelectedFields = new ArrayList<>(mSelectedFields);
        clone.mExcludedFields = new ArrayList<>(mExcludedFields);
        clone.mOrderByArray = new ArrayList<>(mOrderByArray);
        clone.mProcess = mProcess;
        clone.mObjectOnly = mObjectOnly;
        clone.mLimit = mLimit;
        clone.mSkip = mSkip;
        /*clone.mDepth = mDepth;*/
        return clone;
    }

    /**
     * クエリ条件。
     * <p>クエリ条件を表現するクラスです。ABQuery を介して利用します。</p>
     */
    public static class Condition {
        private ABQuery mQuery;
        private String mField;
        private Conjunction mConjunction;

        /**
         * コンストラクタ。
         * @param query {@link ABQuery} オブジェクト
         * @param field フィールド名
         */
        public Condition(ABQuery query, String field) {
            mQuery = query;
            mConjunction = Conjunction.AND;
            mField = field;
        }

        /**
         * コンストラクタ。
         * @param query {@link ABQuery} オブジェクト
         * @param conjunction 連結子
         * @param field フィールド名
         */
        public Condition(ABQuery query, Conjunction conjunction, String field) {
            mQuery = query;
            mConjunction = conjunction;
            mField = field;
        }

        /**
         * コンストラクタ。
         * @param query {@link ABQuery} オブジェクト
         * @param field フィールド
         */
        public Condition(ABQuery query, ABField field) {
            mQuery = query;
            mConjunction = Conjunction.AND;
            mField = field.getKey();
        }

        /**
         * コンストラクタ。
         * @param query {@link ABQuery} オブジェクト
         * @param conjunctionOperator 連結子
         * @param field フィールド
         */
        public Condition(ABQuery query, Conjunction conjunctionOperator, ABField field) {
            mQuery = query;
            mConjunction = conjunctionOperator;
            mField = field.getKey();
        }

        /**
         * フィールドが存在するデータを抽出します。
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery exists() {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addExists(mField);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addExists(mField);
            }
            return mQuery;
        }

        /**
         * フィールドが存在しないデータを抽出します。
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notExists() {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotExists(mField);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotExists(mField);
            }
            return mQuery;
        }

        /**
         * フィールドの値がNULLであるデータを抽出します。
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery isNull() {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addIsNull(mField);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addIsNull(mField);
            }
            return mQuery;
        }

        /**
         * フィールドの値が非NULLであるデータを抽出します。
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery isNotNull() {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addIsNotNull(mField);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addIsNotNull(mField);
            }
            return mQuery;
        }

        /**
         * フィールドの値が真であるデータを抽出します。
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery isTrue() {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addIsTrue(mField);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addIsTrue(mField);
            }
            return mQuery;
        }

        /**
         * フィールドの値が偽であるデータを抽出します。
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery isNotTrue() {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addIsNotTrue(mField);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addIsNotTrue(mField);
            }
            return mQuery;
        }

        /**
         * フィールドの値が偽であるデータを抽出します。
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery isFalse() {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addIsFalse(mField);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addIsFalse(mField);
            }
            return mQuery;
        }

        /**
         * フィールドの値が真であるデータを抽出します。
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery isNotFalse() {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addIsNotFalse(mField);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addIsNotFalse(mField);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値と同値であるデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery equalsTo(String value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addEqualsTo(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addEqualsTo(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値と同値であるデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery equalsTo(Number value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addEqualsTo(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addEqualsTo(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値と同値であるデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery equalsTo(Boolean value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addEqualsTo(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addEqualsTo(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値と同値でないデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notEqualsTo(String value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotEqualsTo(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotEqualsTo(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値と同値でないデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notEqualsTo(Number value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotEqualsTo(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotEqualsTo(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値と同値でないデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notEqualsTo(Boolean value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotEqualsTo(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotEqualsTo(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値より小さいデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery lessThan(Number value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addLessThan(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addLessThan(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値と同値かまたは小さいデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery lessThanOrEqualsTo(Number value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addLessThanOrEqualsTo(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addLessThanOrEqualsTo(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値より大きいデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery greaterThan(Number value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addGreaterThan(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addGreaterThan(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が比較対象値と同値かまたは大きいデータを抽出します。
         * @param value 比較対象値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery greaterThanOrEqualsTo(Number value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addGreaterThanOrEqualsTo(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addGreaterThanOrEqualsTo(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値が値リストに含まれるデータを抽出します。
         * @param values 値リスト
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery contains(List<String> values) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addContains(mField, values);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addContains(mField, values);
            }
            return mQuery;
        }

        /**
         * フィールドの値が値リストに含まれるないデータを抽出します。
         * @param values 値リスト
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notContains(List<String> values) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotContains(mField, values);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotContains(mField, values);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がPREFIX文字列と一致するデータを抽出します。
         * @param value PREFIX文字列
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery startsWith(String value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addStartsWith(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addStartsWith(mField, value);
            }
            return mQuery;
        }
        /**
         * フィールドの値の先頭がPREFIX文字列と一致するデータを抽出します。
         * @param value PREFIX文字列
         * @param option オプション
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery startsWith(String value, RegexOption option) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addStartsWith(mField, value, option);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addStartsWith(mField, value, option);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がPREFIX文字列と一致するデータを抽出します。
         * @param value PREFIX文字列
         * @param options オプション・セット
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery startsWith(String value, EnumSet<RegexOption> options) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addStartsWith(mField, value, options);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addStartsWith(mField, value, options);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がPREFIX文字列と一致しないデータを抽出します。
         * @param value PREFIX文字列
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notStartsWith(String value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotStartsWith(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotStartsWith(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がPREFIX文字列と一致しないデータを抽出します。
         * @param value PREFIX文字列
         * @param option オプション
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notStartsWith(String value, RegexOption option) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotStartsWith(mField, value, option);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotStartsWith(mField, value, option);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がPREFIX文字列と一致しないデータを抽出します。
         * @param value PREFIX文字列
         * @param options オプション・セット
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notStartsWith(String value, EnumSet<RegexOption> options) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotStartsWith(mField, value, options);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotStartsWith(mField, value, options);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がSUFFIX文字列と一致するデータを抽出します。
         * @param value SUFFIX文字列
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery endsWith(String value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addEndsWith(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addEndsWith(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がSUFFIX文字列と一致するデータを抽出します。
         * @param value SUFFIX文字列
         * @param option オプション
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery endsWith(String value, RegexOption option) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addEndsWith(mField, value, option);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addEndsWith(mField, value, option);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がSUFFIX文字列と一致するデータを抽出します。
         * @param value SUFFIX文字列
         * @param options オプション・セット
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery endsWith(String value, EnumSet<RegexOption> options) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addEndsWith(mField, value, options);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addEndsWith(mField, value, options);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がSUFFIX文字列と一致しないデータを抽出します。
         * @param value SUFFIX文字列
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notEndsWith(String value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotEndsWith(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotEndsWith(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がSUFFIX文字列と一致しないデータを抽出します。
         * @param value SUFFIX文字列
         * @param option オプション
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notEndsWith(String value, RegexOption option) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotEndsWith(mField, value, option);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotEndsWith(mField, value, option);
            }
            return mQuery;
        }

        /**
         * フィールドの値の先頭がSUFFIX文字列と一致しないデータを抽出します。
         * @param value SUFFIX文字列
         * @param options オプション・セット
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notEndsWith(String value, EnumSet<RegexOption> options) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotEndsWith(mField, value, options);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotEndsWith(mField, value, options);
            }
            return mQuery;
        }

        /**
         * フィールドの値に検索文字列が含まれるデータを抽出します。
         * @param value 検索文字列
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery like(String value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addLike(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addLike(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値に検索文字列が含まれるデータを抽出します。
         * @param value 検索文字列
         * @param option オプション
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery like(String value, RegexOption option) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addLike(mField, value, option);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addLike(mField, value, option);
            }
            return mQuery;
        }

        /**
         * フィールドの値に検索文字列が含まれるデータを抽出します。
         * @param value 検索文字列
         * @param options オプション・セット
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery like(String value, EnumSet<RegexOption> options) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addLike(mField, value, options);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addLike(mField, value, options);
            }
            return mQuery;
        }

        /**
         * フィールドの値に検索文字列が含まれないデータを抽出します。
         * @param value 検索文字列
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notLike(String value) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotLike(mField, value);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotLike(mField, value);
            }
            return mQuery;
        }

        /**
         * フィールドの値に検索文字列が含まれないデータを抽出します。
         * @param value 検索文字列
         * @param option オプション
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notLike(String value, RegexOption option) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotLike(mField, value, option);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotLike(mField, value, option);
            }
            return mQuery;
        }

        /**
         * フィールドの値に検索文字列が含まれないデータを抽出します。
         * @param value 検索文字列
         * @param options オプション・セット
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery notLike(String value, EnumSet<RegexOption> options) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addNotLike(mField, value, options);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addNotLike(mField, value, options);
            }
            return mQuery;
        }

        /**
         * フィールドの値が上下限範囲内のデータを抽出します。
         * @param from 下限値
         * @param to 上限値
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery between(Number from, Number to) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addBetween(mField, from, to);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addBetween(mField, from, to);
            }
            return mQuery;
        }

        /**
         * 中心座標から半径&lt;N&gt;kmの円圏内のデータを抽出します。
         * @param point 中心座標
         * @param kilometers 半径Km
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery withinCircle(ABGeoPoint point, float kilometers) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addWithinCircle(mField, point, kilometers);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addWithinCircle(mField, point, kilometers);
            }
            return mQuery;
        }

        /**
         * 座標1と座標2を角とする矩形圏内のデータを抽出します。
         * @param point1 座標1
         * @param point2 座標2
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery withinBox(ABGeoPoint point1, ABGeoPoint point2) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addWithinBox(mField, point1, point2);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addWithinBox(mField, point1, point2);
            }
            return mQuery;
        }

        /**
         * 座標1と座標2を角とする矩形圏内のデータを抽出します。
         * @param point1 座標1
         * @param point2 座標2
         * @param basePoint 基準座標
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery withinBox(ABGeoPoint point1, ABGeoPoint point2, ABGeoPoint basePoint) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addWithinBox(mField, point1, point2, basePoint);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addWithinBox(mField, point1, point2, basePoint);
            }
            return mQuery;
        }

        /**
         * 座標1〜座標&lt;N&gt;を直線で結んだ多角形圏内のデータを抽出します。
         * @param points 座標リスト
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery withinPolygon(List<ABGeoPoint> points) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addWithinPolygon(mField, points);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addWithinPolygon(mField, points);
            }
            return mQuery;
        }

        /**
         * 座標1〜座標&lt;N&gt;を直線で結んだ多角形圏内のデータを抽出します。
         * @param points 座標リスト
         * @param basePoint 基準座標
         * @return {@link ABQuery} オブジェクト
         */
        public ABQuery withinPolygon(List<ABGeoPoint> points, ABGeoPoint basePoint) {
            if (Conjunction.OR == mConjunction) {
                ConditionBundle newConditionBundle = new ConditionBundle(Conjunction.OR);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                newConditionBundle.addWithinPolygon(mField, points, basePoint);
            } else { //Conjunction.AND
                ConditionBundle conditionBundle = getCurrentConditionBundle();
                conditionBundle.addWithinPolygon(mField, points, basePoint);
            }
            return mQuery;
        }

        private ABQuery.ConditionBundle getCurrentConditionBundle() {
            if (mQuery.mConditionBundles.size() == 0) {
                ABQuery.ConditionBundle newConditionBundle = new ABQuery.ConditionBundle(Conjunction.AND);
                @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                conditionBundles.add(newConditionBundle);
                return newConditionBundle;
            } else {
                ABQuery.ConditionBundle lastConditionBundle = (ABQuery.ConditionBundle)mQuery.mConditionBundles.get(mQuery.mConditionBundles.size() -1);
                if (lastConditionBundle.getConjunction() == Conjunction.OR) {
                    //NOTE: OR条件は bundle() 以外から条件を連結することはないので、条件が混ざらないよう新しいconditionBundleを用意する。
                    ABQuery.ConditionBundle newConditionBundle = new ABQuery.ConditionBundle(Conjunction.AND);
                    @SuppressWarnings("unchecked") List<ConditionBundle> conditionBundles = mQuery.mConditionBundles;
                    conditionBundles.add(newConditionBundle);
                    return newConditionBundle;
                } else {
                    return lastConditionBundle;
                }
            }
        }

    }

    /**
     * クエリ条件連結クラス
     */
    public static class ConditionBundle {

        private Conjunction mConjunction;
        private List<String> mExistsArray;
        private List<String> mIsArray;
        private List<String> mEqualsToArray;
        private List<String> mNotEqualsToArray;
        private List<String> mLessThanArray;
        private List<String> mLessThanOrEqualsToArray;
        private List<String> mGreaterThanArray;
        private List<String> mGreaterThanOrEqualsToArray;
        private List<String> mContainsArray;
        private List<String> mNotContainsArray;
        private List<String> mStartsWithArray;
        private List<String> mNotStartsWithArray;
        private List<String> mEndsWithArray;
        private List<String> mNotEndsWithArray;
        private List<String> mLikeArray;
        private List<String> mNotLikeArray;
        private List<String> mBetweenArray;
        private List<String> mWithinCircleArray;
        private List<String> mWithinBoxArray;
        private List<String> mWithinPolygonArray;

        /**
         * デフォルト・コンストラクタ。
         */
        public ConditionBundle() {
            mConjunction = Conjunction.AND;
            mExistsArray                = new ArrayList<>();
            mIsArray                    = new ArrayList<>();
            mEqualsToArray              = new ArrayList<>();
            mNotEqualsToArray           = new ArrayList<>();
            mLessThanArray              = new ArrayList<>();
            mLessThanOrEqualsToArray    = new ArrayList<>();
            mGreaterThanArray           = new ArrayList<>();
            mGreaterThanOrEqualsToArray = new ArrayList<>();
            mContainsArray              = new ArrayList<>();
            mNotContainsArray           = new ArrayList<>();
            mStartsWithArray            = new ArrayList<>();
            mNotStartsWithArray         = new ArrayList<>();
            mEndsWithArray              = new ArrayList<>();
            mNotEndsWithArray           = new ArrayList<>();
            mLikeArray                  = new ArrayList<>();
            mNotLikeArray               = new ArrayList<>();
            mBetweenArray               = new ArrayList<>();
            mWithinCircleArray          = new ArrayList<>();
            mWithinBoxArray             = new ArrayList<>();
            mWithinPolygonArray         = new ArrayList<>();
        }

        /**
         * コンストラクタ。
         * <p>引数に連結子を取ります。</p>
         * @param conjunction 連結子
         */
        public ConditionBundle(Conjunction conjunction) {
            this();
            mConjunction = conjunction;
        }

/* NOTE: 以下のようにシンプルな add(..) メソッドにしたかったけど、
         ユーザが args に適切なパラメータを渡すことを強要する良い方法が思い浮かばなかったので
         残念な addXxx(...) メソッドにすることにした。

        public void add(ABField field, ConditionOperator operator, Object... args) {
            add(field.getKey(), operator, args);
        }
        public void add(String field, ConditionOperator operator, Object... args) {
            switch (operator) {
                case EXISTS:
                    Boolean value = (Boolean) args[0];
                    String cond = String.format("%s.%s.%s", field, ABQuery.OPERATOR_EXISTS, value ? "true" : "false");
                    if (!mExistsArray.contains(cond)) {
                        mExistsArray.add(cond);
                    }
                    return;
                case IS:                               break;
                case IS_NOT:                           break;
                case EQUALS_TO:                        break;
                case NOT_EQUALS_TO:                    break;
                case LESS_THAN:                        break;
                case LESS_THAN_OR_EQUALS_TO:           break;
                case GREATER_THAN:                     break;
                case GREATER_THAN_OR_EQUALS_TO:        break;
                case STARTS_WITH:                      break;
                case STARTS_WITH_CASE_INSENSITIVE:     break;
                case NOT_STARTS_WITH:                  break;
                case NOT_STARTS_WITH_CASE_INSENSITIVE: break;
                case ENDS_WITH:                        break;
                case ENDS_WITH_CASE_INSENSITIVE:       break;
                case NOT_ENDS_WITH:                    break;
                case NOT_ENDS_WITH_CASE_INSENSITIVE:   break;
                case LIKE:                             break;
                case LIKE_CASE_INSENSITIVE:            break;
                case NOT_LIKE:                         break;
                case NOT_LIKE_CASE_INSENSITIVE:        break;
                case BETWEEN:                          break;
                case CONTAINS:                         break;
                case NOT_CONTAINS:                     break;
                case WITHIN_CIRCLE:                    break;
                case WITHIN_BOX:                       break;
                case WITHIN_POLYGON:                   break;
                default:                               break;
            }
        }
*/
        /**
         * exists 条件を追加します。
         * @param field フィールド
         */
        public void addExists(ABField field) {
            addExists(field.getKey());
        }
        /**
         * exists 条件を追加します。
         * @param field フィールド
         */
        public void addExists(String field) {
            String cond = String.format("%s.%s.true", field, ABQuery.OPERATOR_EXISTS);
            if (!mExistsArray.contains(cond)) {
                mExistsArray.add(cond);
            }
        }

        /**
         * notExists 条件を追加します。
         * @param field フィールド
         */
        public void addNotExists(ABField field) {
            addNotExists(field.getKey());
        }
        /**
         * notExists 条件を追加します。
         * @param field フィールド名
         */
        public void addNotExists(String field) {
            String cond = String.format("%s.%s.false", field, ABQuery.OPERATOR_EXISTS);
            if (!mExistsArray.contains(cond)) {
                mExistsArray.add(cond);
            }
        }

        /**
         * isNull 条件を追加します。
         * @param field フィールド
         */
        public void addIsNull(ABField field) {
            addIsNull(field.getKey());
        }
        /**
         * isNull 条件を追加します。
         * @param field フィールド名
         */
        public void addIsNull(String field) {
            String cond = String.format("%s.%s.null", field, ABQuery.OPERATOR_IS);
            if (!mIsArray.contains(cond)) {
                mIsArray.add(cond);
            }
        }

        /**
         * isNotNull 条件を追加します。
         * @param field フィールド
         */
        public void addIsNotNull(ABField field) {
            addIsNotNull(field.getKey());
        }
        /**
         * isNotNull 条件を追加します。
         * @param field フィールド名
         */
        public void addIsNotNull(String field) {
            String cond = String.format("%s.%s.null", field, ABQuery.OPERATOR_IS_NOT);
            if (!mIsArray.contains(cond)) {
                mIsArray.add(cond);
            }
        }

        /**
         * isTrue 条件を追加します。
         * @param field フィールド
         */
        public void addIsTrue(ABField field) {
            addIsTrue(field.getKey());
        }
        /**
         * isTrue 条件を追加します。
         * @param field フィールド名
         */
        public void addIsTrue(String field) {
            String cond = String.format("%s.%s.true", field, ABQuery.OPERATOR_IS);
            if (!mIsArray.contains(cond)) {
                mIsArray.add(cond);
            }
        }

        /**
         * isNotTrue 条件を追加します。
         * @param field フィールド
         */
        public void addIsNotTrue(ABField field) {
            addIsNotTrue(field.getKey());
        }
        /**
         * isNotTrue 条件を追加します。
         * @param field フィールド名
         */
        public void addIsNotTrue(String field) {
            String cond = String.format("%s.%s.true", field, ABQuery.OPERATOR_IS_NOT);
            if (!mIsArray.contains(cond)) {
                mIsArray.add(cond);
            }
        }

        /**
         * isFalse 条件を追加します。
         * @param field フィールド
         */
        public void addIsFalse(ABField field) {
            addIsFalse(field.getKey());
        }
        /**
         * isFalse 条件を追加します。
         * @param field フィールド名
         */
        public void addIsFalse(String field) {
            String cond = String.format("%s.%s.false", field, ABQuery.OPERATOR_IS);
            if (!mIsArray.contains(cond)) {
                mIsArray.add(cond);
            }
        }

        /**
         * isNotFalse 条件を追加します。
         * @param field フィールド
         */
        public void addIsNotFalse(ABField field) {
            addIsNotFalse(field.getKey());
        }
        /**
         * isNotFalse 条件を追加します。
         * @param field フィールド名
         */
        public void addIsNotFalse(String field) {
            String cond = String.format("%s.%s.false", field, ABQuery.OPERATOR_IS_NOT);
            if (!mIsArray.contains(cond)) {
                mIsArray.add(cond);
            }
        }

        /**
         * equalsTo 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addEqualsTo(ABField field, String value) {
            addEqualsTo(field.getKey(), value);
        }
        /**
         * equalsTo 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addEqualsTo(String field, String value) {
            String cond = String.format("%s.%s.%s", field, ABQuery.OPERATOR_EQUALS_TO, encodeUrlString(value));
            if (!mEqualsToArray.contains(cond)) {
                mEqualsToArray.add(cond);
            }
        }

        /**
         * equalsTo 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addEqualsTo(ABField field, Number value) {
            addEqualsTo(field.getKey(), value);
        }
        /**
         * equalsTo 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addEqualsTo(String field, Number value) {
            String cond = String.format("%s.%s.%sn", field, ABQuery.OPERATOR_EQUALS_TO, value.doubleValue() == 0.0f ? "0" : value.toString());
            if (!mEqualsToArray.contains(cond)) {
                mEqualsToArray.add(cond);
            }
        }

        /**
         * equalsTo 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addEqualsTo(ABField field, Boolean value) {
            addEqualsTo(field.getKey(), value);
        }
        /**
         * equalsTo 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addEqualsTo(String field, Boolean value) {
            String cond = String.format("%s.%s.%s", field, ABQuery.OPERATOR_EQUALS_TO, value.toString());
            if (!mEqualsToArray.contains(cond)) {
                mEqualsToArray.add(cond);
            }
        }

        /**
         * notEqualsTo 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addNotEqualsTo(ABField field, String value) {
            addNotEqualsTo(field.getKey(), value);
        }
        /**
         * notEqualsTo 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addNotEqualsTo(String field, String value) {
            String cond = String.format("%s.%s.%s", field, ABQuery.OPERATOR_NOT_EQUALS_TO, encodeUrlString(value));
            if (!mNotEqualsToArray.contains(cond)) {
                mNotEqualsToArray.add(cond);
            }
        }

        /**
         * notEqualsTo 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addNotEqualsTo(ABField field, Number value) {
            addNotEqualsTo(field.getKey(), value);
        }
        /**
         * notEqualsTo 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addNotEqualsTo(String field, Number value) {
            String cond = String.format("%s.%s.%sn", field, ABQuery.OPERATOR_NOT_EQUALS_TO, value.doubleValue() == 0.0f ? "0" : value.toString());
            if (!mNotEqualsToArray.contains(cond)) {
                mNotEqualsToArray.add(cond);
            }
        }

        /**
         * notEqualsTo 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addNotEqualsTo(ABField field, Boolean value) {
            addNotEqualsTo(field.getKey(), value);
        }
        /**
         * notEqualsTo 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addNotEqualsTo(String field, Boolean value) {
            String cond = String.format("%s.%s.%s", field, ABQuery.OPERATOR_NOT_EQUALS_TO, value.toString());
            if (!mNotEqualsToArray.contains(cond)) {
                mNotEqualsToArray.add(cond);
            }
        }

        /**
         * lessThan 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addLessThan(ABField field, Number value) {
            addLessThan(field.getKey(), value);
        }
        /**
         * lessThan 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addLessThan(String field, Number value) {
            String cond = String.format("%s.%s.%sn", field, ABQuery.OPERATOR_LESS_THAN, value.doubleValue() == 0.0f ? "0" : value.toString());
            if (!mLessThanArray.contains(cond)) {
                mLessThanArray.add(cond);
            }
        }

        /**
         * lessThanOrEqualsTo 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addLessThanOrEqualsTo(ABField field, Number value) {
            addLessThanOrEqualsTo(field.getKey(), value);
        }
        /**
         * lessThanOrEqualsTo 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addLessThanOrEqualsTo(String field, Number value) {
            String cond = String.format("%s.%s.%sn", field, ABQuery.OPERATOR_LESS_THAN_OR_EQUALS_TO, value.doubleValue() == 0.0f ? "0" : value.toString());
            if (!mLessThanOrEqualsToArray.contains(cond)) {
                mLessThanOrEqualsToArray.add(cond);
            }
        }

        /**
         * greaterThan 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addGreaterThan(ABField field, Number value) {
            addGreaterThan(field.getKey(), value);
        }
        /**
         * greaterThan 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addGreaterThan(String field, Number value) {
            String cond = String.format("%s.%s.%sn", field, ABQuery.OPERATOR_GREATER_THAN, value.doubleValue() == 0.0f ? "0" : value.toString());
            if (!mGreaterThanArray.contains(cond)) {
                mGreaterThanArray.add(cond);
            }
        }

        /**
         * greaterThanOrEqualsTo 条件を追加します。
         * @param field フィールド
         * @param value 比較対象値
         */
        public void addGreaterThanOrEqualsTo(ABField field, Number value) {
            addGreaterThanOrEqualsTo(field.getKey(), value);
        }
        /**
         * greaterThanOrEqualsTo 条件を追加します。
         * @param field フィールド名
         * @param value 比較対象値
         */
        public void addGreaterThanOrEqualsTo(String field, Number value) {
            String cond = String.format("%s.%s.%sn", field, ABQuery.OPERATOR_GREATER_THAN_OR_EQUALS_TO, value.doubleValue() == 0.0 ? "0" : value.toString());
            if (!mGreaterThanOrEqualsToArray.contains(cond)) {
                mGreaterThanOrEqualsToArray.add(cond);
            }
        }

        /**
         * contains 条件を追加します。
         * @param field フィールド
         * @param values 値リスト
         */
        public void addContains(ABField field, List<String> values) {
            addContains(field.getKey(), values);
        }
        /**
         * contains 条件を追加します。
         * @param field フィールド名
         * @param values 値リスト
         */
        public void addContains(String field, List<String> values) {
            if (values == null || values.size() == 0) return;
            StringBuilder buff = new StringBuilder();
            for (String val : values) {
                if (buff.length() > 0) buff.append(",");
                buff.append(encodeUrlString(val));
            }
            if (buff.length() > 0) {
                String cond = String.format("%s.%s.%s", field, ABQuery.OPERATOR_CONTAINS, buff.toString());
                if (!mContainsArray.contains(cond)) {
                    mContainsArray.add(cond);
                }
            }
        }

        /**
         * notContains 条件を追加します。
         * @param field フィールド
         * @param values 値リスト
         */
        public void addNotContains(ABField field, List<String> values) {
            addNotContains(field.getKey(), values);
        }
        /**
         * notContains 条件を追加します。
         * @param field フィールド名
         * @param values 値リスト
         */
        public void addNotContains(String field, List<String> values) {
            if (values == null || values.size() == 0) return;
            StringBuilder buff = new StringBuilder();
            for (String val : values) {
                if (buff.length() > 0) buff.append(",");
                buff.append(encodeUrlString(val));
            }
            if (buff.length() > 0) {
                String cond = String.format("%s.%s.%s", field, ABQuery.OPERATOR_NOT_CONTAINS, buff.toString());
                if (!mNotContainsArray.contains(cond)) {
                    mNotContainsArray.add(cond);
                }
            }
        }

        /**
         * startsWith 条件を追加します。
         * @param field フィールド
         * @param value PREFIX文字列
         */
        public void addStartsWith(ABField field, String value) {
            addStartsWith(field.getKey(), value);
        }
        /**
         * startsWith 条件を追加します。
         * @param field フィールド名
         * @param value PREFIX文字列
         */
        public void addStartsWith(String field, String value) {
            addStartsWith(field, value, EnumSet.of(RegexOption.NONE));
        }
        /**
         * startsWith 条件を追加します。
         * @param field フィールド
         * @param value PREFIX文字列
         * @param option オプション
         */
        public void addStartsWith(ABField field, String value, RegexOption option) {
            addStartsWith(field.getKey(), value, option);
        }
        /**
         * startsWith 条件を追加します。
         * @param field フィールド名
         * @param value PREFIX文字列
         * @param option オプション
         */
        public void addStartsWith(String field, String value, RegexOption option) {
            addStartsWith(field, value, EnumSet.of(option));
        }
        /**
         * startsWith 条件を追加します。
         * @param field フィールド
         * @param value PREFIX文字列
         * @param options オプション・セット
         */
        public void addStartsWith(ABField field, String value, EnumSet<RegexOption> options) {
            addStartsWith(field.getKey(), value, options);
        }
        /**
         * startsWith 条件を追加します。
         * @param field フィールド名
         * @param value PREFIX文字列
         * @param options オプション・セット
         */
        public void addStartsWith(String field, String value, EnumSet<RegexOption> options) {
            String operator = options.contains(RegexOption.CASE_INSENSITIVE) ?
                    ABQuery.OPERATOR_STARTS_WITH_CASE_INSENSITIVE : ABQuery.OPERATOR_STARTS_WITH;
            String cond = String.format("%s.%s.%s", field, operator, encodeUrlString(value));
            if (!mStartsWithArray.contains(cond)) {
                mStartsWithArray.add(cond);
            }
        }

        /**
         * notStartsWith 条件を追加します。
         * @param field フィールド
         * @param value PREFIX文字列
         */
        public void addNotStartsWith(ABField field, String value) {
            addNotStartsWith(field.getKey(), value);
        }
        /**
         * notStartsWith 条件を追加します。
         * @param field フィールド名
         * @param value PREFIX文字列
         */
        public void addNotStartsWith(String field, String value) {
            addNotStartsWith(field, value, EnumSet.of(RegexOption.NONE));
        }
        /**
         * notStartsWith 条件を追加します。
         * @param field フィールド
         * @param value PREFIX文字列
         * @param option オプション
         */
        public void addNotStartsWith(ABField field, String value, RegexOption option) {
            addNotStartsWith(field.getKey(), value, option);
        }
        /**
         * notStartsWith 条件を追加します。
         * @param field フィールド名
         * @param value PREFIX文字列
         * @param option オプション
         */
        public void addNotStartsWith(String field, String value, RegexOption option) {
            addNotStartsWith(field, value, EnumSet.of(option));
        }
        /**
         * notStartsWith 条件を追加します。
         * @param field フィールド
         * @param value PREFIX文字列
         * @param options オプション・セット
         */
        public void addNotStartsWith(ABField field, String value, EnumSet<RegexOption> options) {
            addNotStartsWith(field.getKey(), value, options);
        }
        /**
         * notStartsWith 条件を追加します。
         * @param field フィールド名
         * @param value PREFIX文字列
         * @param options オプション・セット
         */
        public void addNotStartsWith(String field, String value, EnumSet<RegexOption> options) {
            String operator = options.contains(RegexOption.CASE_INSENSITIVE) ?
                    ABQuery.OPERATOR_NOT_STARTS_WITH_INSENSITIVE : ABQuery.OPERATOR_NOT_STARTS_WITH;
            String cond = String.format("%s.%s.%s", field, operator, encodeUrlString(value));
            if (!mNotStartsWithArray.contains(cond)) {
                mNotStartsWithArray.add(cond);
            }
        }

        /**
         * endsWith 条件を追加します。
         * @param field フィールド
         * @param value SUFFIX文字列
         */
        public void addEndsWith(ABField field, String value) {
            addEndsWith(field.getKey(), value);
        }
        /**
         * endsWith 条件を追加します。
         * @param field フィールド名
         * @param value SUFFIX文字列
         */
        public void addEndsWith(String field, String value) {
            addEndsWith(field, value, EnumSet.of(RegexOption.NONE));
        }
        /**
         * endsWith 条件を追加します。
         * @param field フィールド
         * @param value SUFFIX文字列
         * @param option オプション
         */
        public void addEndsWith(ABField field, String value, RegexOption option) {
            addEndsWith(field.getKey(), value, option);
        }
        /**
         * endsWith 条件を追加します。
         * @param field フィールド名
         * @param value SUFFIX文字列
         * @param option オプション
         */
        public void addEndsWith(String field, String value, RegexOption option) {
            addEndsWith(field, value, EnumSet.of(option));
        }
        /**
         * endsWith 条件を追加します。
         * @param field フィールド
         * @param value SUFFIX文字列
         * @param options オプション・セット
         */
        public void addEndsWith(ABField field, String value, EnumSet<RegexOption> options) {
            addEndsWith(field.getKey(), value, options);
        }
        /**
         * endsWith 条件を追加します。
         * @param field フィールド名
         * @param value SUFFIX文字列
         * @param options オプション・セット
         */
        public void addEndsWith(String field, String value, EnumSet<RegexOption> options) {
            String operator = options.contains(RegexOption.CASE_INSENSITIVE) ?
                    OPERATOR_ENDS_WITH_INSENSITIVE : OPERATOR_ENDS_WITH;
            String cond = String.format("%s.%s.%s", field, operator, encodeUrlString(value));
            if (!mEndsWithArray.contains(cond)) {
                mEndsWithArray.add(cond);
            }
        }

        /**
         * notEndsWith 条件を追加します。
         * @param field フィールド
         * @param value SUFFIX文字列
         */
        public void addNotEndsWith(ABField field, String value) {
            addNotEndsWith(field.getKey(), value);
        }
        /**
         * notEndsWith 条件を追加します。
         * @param field フィールド名
         * @param value SUFFIX文字列
         */
        public void addNotEndsWith(String field, String value) {
            addNotEndsWith(field, value, EnumSet.of(RegexOption.NONE));
        }
        /**
         * notEndsWith 条件を追加します。
         * @param field フィールド
         * @param value SUFFIX文字列
         * @param option オプション
         */
        public void addNotEndsWith(ABField field, String value, RegexOption option) {
            addNotEndsWith(field.getKey(), value, option);
        }
        /**
         * notEndsWith 条件を追加します。
         * @param field フィールド名
         * @param value SUFFIX文字列
         * @param option オプション
         */
        public void addNotEndsWith(String field, String value, RegexOption option) {
            addNotEndsWith(field, value, EnumSet.of(option));
        }
        /**
         * notEndsWith 条件を追加します。
         * @param field フィールド
         * @param value SUFFIX文字列
         * @param options オプション・セット
         */
        public void addNotEndsWith(ABField field, String value, EnumSet<RegexOption> options) {
            addNotEndsWith(field.getKey(), value, options);
        }
        /**
         * notEndsWith 条件を追加します。
         * @param field フィールド名
         * @param value SUFFIX文字列
         * @param options オプション・セット
         */
        public void addNotEndsWith(String field, String value, EnumSet<RegexOption> options) {
            String operator = options.contains(RegexOption.CASE_INSENSITIVE) ?
                    OPERATOR_NOT_ENDS_WITH_INSENSITIVE : OPERATOR_NOT_ENDS_WITH;
            String cond = String.format("%s.%s.%s", field, operator, encodeUrlString(value));
            if (!mNotEndsWithArray.contains(cond)) {
                mNotEndsWithArray.add(cond);
            }
        }

        /**
         * like 条件を追加します。
         * @param field フィールド
         * @param value 検索文字列
         */
        public void addLike(ABField field, String value) {
            addLike(field.getKey(), value);
        }
        /**
         * like 条件を追加します。
         * @param field フィールド名
         * @param value 検索文字列
         */
        public void addLike(String field, String value) {
            addLike(field, value, EnumSet.of(RegexOption.NONE));
        }
        /**
         * like 条件を追加します。
         * @param field フィールド
         * @param value 検索文字列
         * @param option オプション
         */
        public void addLike(ABField field, String value, RegexOption option) {
            addLike(field.getKey(), value, option);
        }
        /**
         * like 条件を追加します。
         * @param field フィールド名
         * @param value 検索文字列
         * @param option オプション
         */
        public void addLike(String field, String value, RegexOption option) {
            addLike(field, value, EnumSet.of(option));
        }
        /**
         * like 条件を追加します。
         * @param field フィールド
         * @param value 検索文字列
         * @param options オプション・セット
         */
        public void addLike(ABField field, String value, EnumSet<RegexOption> options) {
            addLike(field.getKey(), value, options);
        }
        /**
         * like 条件を追加します。
         * @param field フィールド名
         * @param value 検索文字列
         * @param options オプション・セット
         */
        public void addLike(String field, String value, EnumSet<RegexOption> options) {
            String operator = options.contains(RegexOption.CASE_INSENSITIVE) ?
                    ABQuery.OPERATOR_LIKE_CASE_INSENSITIVE : ABQuery.OPERATOR_LIKE;
            String cond = String.format("%s.%s.%s", field, operator, encodeUrlString(value));
            if (!mLikeArray.contains(cond)) {
                mLikeArray.add(cond);
            }
        }

        /**
         * notLike 条件を追加します。
         * @param field フィールド
         * @param value 検索文字列
         */
        public void addNotLike(ABField field, String value) {
            addNotLike(field.getKey(), value);
        }
        /**
         * notLike 条件を追加します。
         * @param field フィールド名
         * @param value 検索文字列
         */
        public void addNotLike(String field, String value) {
            addNotLike(field, value, EnumSet.of(RegexOption.NONE));
        }
        /**
         * notLike 条件を追加します。
         * @param field フィールド
         * @param value 検索文字列
         * @param option オプション
         */
        public void addNotLike(ABField field, String value, RegexOption option) {
            addNotLike(field.getKey(), value, option);
        }
        /**
         * notLike 条件を追加します。
         * @param field フィールド名
         * @param value 検索文字列
         * @param option オプション
         */
        public void addNotLike(String field, String value, RegexOption option) {
            addNotLike(field, value, EnumSet.of(option));
        }
        /**
         * notLike 条件を追加します。
         * @param field フィールド
         * @param value 検索文字列
         * @param options オプション・セット
         */
        public void addNotLike(ABField field, String value, EnumSet<RegexOption> options) {
            addNotLike(field.getKey(), value, options);
        }
        /**
         * notLike 条件を追加します。
         * @param field フィールド名
         * @param value 検索文字列
         * @param options オプション・セット
         */
        public void addNotLike(String field, String value, EnumSet<RegexOption> options) {
            String operator = options.contains(RegexOption.CASE_INSENSITIVE) ?
                    ABQuery.OPERATOR_NOT_LIKE_CASE_INSENSITIVE : ABQuery.OPERATOR_NOT_LIKE;
            String cond = String.format("%s.%s.%s", field, operator, encodeUrlString(value));
            if (!mNotLikeArray.contains(cond)) {
                mNotLikeArray.add(cond);
            }
        }

        /**
         * between 条件を追加します。
         * @param field フィールド
         * @param from 下限値
         * @param to 上限値
         */
        public void addBetween(ABField field, Number from, Number to) {
            addBetween(field.getKey(), from, to);
        }
        /**
         * between 条件を追加します。
         * @param field フィールド名
         * @param from 下限値
         * @param to 上限値
         */
        public void addBetween(String field, Number from, Number to) {
            String cond;
            Double dblFrom = from.doubleValue();
            Double dblTo = to.doubleValue();
            if (dblFrom.compareTo(dblTo) < 0) {
                cond = String.format("%s.%s.%sn;%s.%s.%sn",
                        field, ABQuery.OPERATOR_GREATER_THAN_OR_EQUALS_TO, from,
                        field, ABQuery.OPERATOR_LESS_THAN_OR_EQUALS_TO, to);
            } else if (dblFrom.compareTo(dblTo) > 0) {
                cond = String.format("%s.%s.%sn;%s.%s.%sn",
                        field, ABQuery.OPERATOR_GREATER_THAN_OR_EQUALS_TO, to,
                        field, ABQuery.OPERATOR_LESS_THAN_OR_EQUALS_TO, from);
            } else {
                cond = String.format("%s.%s.%sn", field, ABQuery.OPERATOR_EQUALS_TO, from);
            }
            if (!mBetweenArray.contains(cond)) {
                mBetweenArray.add(cond);
            }
        }

        /**
         * withinCircle 条件を追加します。
         * @param field フィールド
         * @param point 中心座標
         * @param kilometers 半径Km
         */
        public void addWithinCircle(ABField field, ABGeoPoint point, float kilometers) {
            addWithinCircle(field.getKey(), point, kilometers);
        }
        /**
         * withinCircle 条件を追加します。
         * @param field フィールド名
         * @param point 中心座標
         * @param kilometers 半径Km
         */
        public void addWithinCircle(String field, ABGeoPoint point, float kilometers) {
            String cond = String.format("%s.%s.%.6f,%.6f,%.1f",
                    field, ABQuery.OPERATOR_WITHIN_CIRCLE, point.getLongitude(), point.getLatitude(), kilometers);
            if (!mWithinCircleArray.contains(cond)) {
                mWithinCircleArray.add(cond);
            }
        }

        /**
         * withinBox 条件を追加します。
         * @param field フィールド
         * @param point1 座標1
         * @param point2 座標2
         */
        public void addWithinBox(ABField field, ABGeoPoint point1, ABGeoPoint point2) {
            addWithinBox(field.getKey(), point1, point2);
        }
        /**
         * withinBox 条件を追加します。
         * @param field フィールド名
         * @param point1 座標1
         * @param point2 座標2
         */
        public void addWithinBox(String field, ABGeoPoint point1, ABGeoPoint point2) {
            addWithinBox(field, point1, point2, null);
        }
        /**
         * withinBox 条件を追加します。
         * @param field フィールド
         * @param point1 座標1
         * @param point2 座標2
         * @param basePoint 基準座標
         */
        public void addWithinBox(ABField field, ABGeoPoint point1, ABGeoPoint point2, ABGeoPoint basePoint) {
            addWithinBox(field.getKey(), point1, point2, basePoint);
        }
        /**
         * withinBox 条件を追加します。
         * @param field フィールド名
         * @param point1 座標1
         * @param point2 座標2
         * @param basePoint 基準座標
         */
        public void addWithinBox(String field, ABGeoPoint point1, ABGeoPoint point2, ABGeoPoint basePoint) {
            StringBuilder cond = new StringBuilder(
                    String.format("%s.%s.%.6f,%.6f,%.6f,%.6f", field, ABQuery.OPERATOR_WITHIN_BOX,
                            point1.getLongitude(), point1.getLatitude(),
                            point2.getLongitude(), point2.getLatitude())
            );
            if (basePoint != null) {
                cond.append(String.format(",rc,%.6f,%.6f", basePoint.getLongitude(), basePoint.getLatitude()));
            }
            if (!mWithinBoxArray.contains(cond)) {
                mWithinBoxArray.add(cond.toString());
            }
        }

        /**
         * withinPolygon 条件を追加します。
         * @param field フィールド
         * @param points 座標リスト
         */
        public void addWithinPolygon(ABField field, List<ABGeoPoint> points) {
            addWithinPolygon(field.getKey(), points);
        }
        /**
         * withinPolygon 条件を追加します。
         * @param field フィールド名
         * @param points 座標リスト
         */
        public void addWithinPolygon(String field, List<ABGeoPoint> points) {
            addWithinPolygon(field, points, null);
        }
        /**
         * withinPolygon 条件を追加します。
         * @param field フィールド
         * @param points 座標リスト
         * @param basePoint 基準座標
         */
        public void addWithinPolygon(ABField field, List<ABGeoPoint> points, ABGeoPoint basePoint) {
            addWithinPolygon(field.getKey(), points, basePoint);
        }
        /**
         * withinPolygon 条件を追加します。
         * @param field フィールド名
         * @param points 座標リスト
         * @param basePoint 基準座標
         */
        public void addWithinPolygon(String field, List<ABGeoPoint> points, ABGeoPoint basePoint) {
            if (points == null || points.size() == 0) return;

            StringBuilder chunk = new StringBuilder();
            for (ABGeoPoint point : points) {
                if (chunk.length() > 0) chunk.append(",");
                chunk.append(String.format("%.6f,%.6f", point.getLongitude(), point.getLatitude()));
            }
            if (chunk.length() > 0) {
                StringBuilder cond = new StringBuilder(String.format("%s.%s.%s", field, ABQuery.OPERATOR_WITHIN_POLYGON, chunk));
                if (basePoint != null) {
                    cond.append(String.format(",rc,%.6f,%.6f", basePoint.getLongitude(), basePoint.getLatitude()));
                }
                if (!mWithinPolygonArray.contains(cond)) {
                    mWithinPolygonArray.add(cond.toString());
                }
            }
        }

        //////////// Accessors ////////////////////

        public Conjunction getConjunction() {
            return mConjunction;
        }
        public void setConjunction(Conjunction conjunction) {
            mConjunction = conjunction;
        }

        //////////// Cloneable ////////////////////

        /**
         * クエリオブジェクトを複製します。
         * @return 複製した {@link ABDBObject} オブジェクト
         * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
         * @see java.lang.Object#clone()
         */
        public ConditionBundle clone() throws CloneNotSupportedException {
            ConditionBundle clone = (ConditionBundle)super.clone();
            clone.mExistsArray                = new ArrayList<>(mExistsArray);
            clone.mIsArray                    = new ArrayList<>(mIsArray);
            clone.mEqualsToArray              = new ArrayList<>(mEqualsToArray);
            clone.mNotEqualsToArray           = new ArrayList<>(mNotEqualsToArray);
            clone.mLessThanArray              = new ArrayList<>(mLessThanArray);
            clone.mLessThanOrEqualsToArray    = new ArrayList<>(mLessThanOrEqualsToArray);
            clone.mGreaterThanArray           = new ArrayList<>(mGreaterThanArray);
            clone.mGreaterThanOrEqualsToArray = new ArrayList<>(mGreaterThanOrEqualsToArray);
            clone.mContainsArray              = new ArrayList<>(mContainsArray);
            clone.mNotContainsArray           = new ArrayList<>(mNotContainsArray);
            clone.mStartsWithArray            = new ArrayList<>(mStartsWithArray);
            clone.mNotStartsWithArray         = new ArrayList<>(mNotStartsWithArray);
            clone.mEndsWithArray              = new ArrayList<>(mEndsWithArray);
            clone.mNotEndsWithArray           = new ArrayList<>(mNotEndsWithArray);
            clone.mLikeArray                  = new ArrayList<>(mLikeArray);
            clone.mNotLikeArray               = new ArrayList<>(mNotLikeArray);
            clone.mBetweenArray               = new ArrayList<>(mBetweenArray);
            clone.mWithinCircleArray          = new ArrayList<>(mWithinCircleArray);
            clone.mWithinBoxArray             = new ArrayList<>(mWithinBoxArray);
            clone.mWithinPolygonArray         = new ArrayList<>(mWithinPolygonArray);
            return clone;
        }

        //private String toStringBooleanValue(boolean b) { return b ? "true" : "false"; }

        //ref) http://www.glamenv-septzen.net/view/1170
        private String encodeUrlString(String urlString) {
            try {
                return URLEncoder.encode(urlString, "UTF-8").replace("+", "%20").replace("*", "%2A");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

}
