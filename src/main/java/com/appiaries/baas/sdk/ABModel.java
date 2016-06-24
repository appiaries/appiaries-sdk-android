//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 基底モデル。
 * <p>アピアリーズ BaaS 上で扱われる各種データ（オブジェクト）の基底モデルクラスです。</p>
 * @version 2.0.0
 * @since 2.0.0
 */
@ABCollection("com.appiaries.baas.sdk.ABModel")
public class ABModel implements Cloneable, Serializable {
    private static final String TAG = ABModel.class.getSimpleName();

    private static final long serialVersionUID = -728726262328715167L;

//region Fields
    /**
     * 基底モデル・フィールド。
     * <p>基底モデルが持つフィールドの定数を保持します。</p>
     */
    public static class Field {
        /**
         * オブジェクトID。
         */
        public static final ABField ID = new ABField("_id", String.class);
        /**
         * 作成日時。
         */
        public static final ABField CREATED = new ABField("_cts", Date.class);
        /**
         * 作成者。
         */
        public static final ABField CREATED_BY = new ABField("_cby", String.class);
        /**
         * 更新日時。
         */
        public static final ABField UPDATED = new ABField("_uts", Date.class);
        /**
         * 更新者。
         */
        public static final ABField UPDATED_BY = new ABField("_uby", String.class);
    }
//endregion

//region Properties
    protected String mCollectionID;
    protected Map<String, Object> mEstimatedData;
    protected Map<String, Object> mOriginalData;
    protected Set<String> mUpdatedKeys;
    protected boolean mDirty;
    protected boolean mNew;
//endregion

//region Initialization

    /**
     * デフォルト・コンストラクタ。
     */
    public ABModel() {

        String collectionID;
        Class<? extends ABModel> clazz = this.getClass();
        ABCollection annotation = clazz.getAnnotation(ABCollection.class);
        if (annotation != null && annotation.value().length() > 0) {
            collectionID = annotation.value();
        } else {
            collectionID = clazz.getName();
        }

        Class<? extends ABModel> registeredClass = AB.ClassRepository.getBaaSClass(collectionID);
        if (getClass().equals(ABModel.class) && registeredClass != null && !registeredClass.isInstance(this)) {
            throw new IllegalArgumentException("You must create this type of ABModel using ABModel.create() or the proper subclass.");
        }

        if (getClass().equals(ABModel.class) && !getClass().equals(registeredClass)) {
            throw new IllegalArgumentException("You must register this ABModel subclass before instantiating it.");
        }

        mCollectionID = collectionID;
        mEstimatedData = new HashMap<>();
        mOriginalData  = new HashMap<>();
        mUpdatedKeys = new HashSet<>();
        mDirty = false;
        mNew = true;
    }

    /**
     * コンストラクタ。
     * <p>引数にコレクションIDを取ります。</p>
     * @param collectionID コレクションID
     */
    public ABModel(String collectionID) {

        if (collectionID == null) {
            throw new IllegalArgumentException("You must specify a Appiaries Collection ID when creating a new ABModel.");
        }

        Class<? extends ABModel> registeredClass = AB.ClassRepository.getBaaSClass(collectionID);
        if (getClass().equals(ABModel.class) && registeredClass != null && !registeredClass.isInstance(this)) {
            throw new IllegalArgumentException("You must create this type of ABModel using ABModel.create() or the proper subclass.");
        }

        if (getClass().equals(ABModel.class) && !getClass().equals(registeredClass)) {
            throw new IllegalArgumentException("You must register this ABModel subclass before instantiating it.");
        }

        mCollectionID = collectionID;
        mEstimatedData = new HashMap<>();
        mOriginalData  = new HashMap<>();
        mUpdatedKeys = new HashSet<>();
        mDirty = false;
        mNew = true;
    }

    /**
     * コンストラクタ。
     * <p>引数に Map (アピアリーズ BaaS API の JSON レスポンス) を取ります。</p>
     * @param collectionID コレクションID
     * @param map Map (アピアリーズ BaaS API の JSON レスポンス)
     */
    public ABModel(String collectionID, Map<String, Object> map) {

        if (collectionID == null) {
            throw new IllegalArgumentException("You must specify a Appiaries Collection ID when creating a new ABModel.");
        }

        Class<? extends ABModel> registeredClass = AB.ClassRepository.getBaaSClass(collectionID);
        if (getClass().equals(ABModel.class) && registeredClass != null && !registeredClass.isInstance(this)) {
            throw new IllegalArgumentException("You must create this type of ABModel using ABModel.create() or the proper subclass.");
        }

        if (getClass().equals(ABModel.class) && !getClass().equals(registeredClass)) {
            throw new IllegalArgumentException("You must register this ABModel subclass before instantiating it.");
        }

        mCollectionID = collectionID;
        mEstimatedData = new HashMap<>(map);
        mOriginalData  = new HashMap<>(map);
        mUpdatedKeys = new HashSet<>();
        apply();
        mNew = true;
    }

//endregion

//region Object Manipulation

    /**
     * 保持するプロパティのキーセットを取得します。
     * @return keySet
     */
    public Set<String> keySet() { //iOS: allKeys
        return mEstimatedData.keySet();
    }

    /**
     * 保持するプロパティ値を entrySet として返します。
     * @return entrySet
     */
    public Set<Map.Entry<String, Object>> entrySet() { //iOS: allKeysAndValues
        return mEstimatedData.entrySet();
    }

    /**
     * 指定フィールドの値を取得します。
     * @param field フィールド
     * @param <T> 値のデータ型
     * @return 値
     */
    public <T> T get(ABField field) { //iOS: objectForKey:
        return get(field.getKey());
    }

    /**
     * 指定キーの値を取得します。
     * @param key キー
     * @param <T> 値のデータ型
     * @return 値
     */
    public <T> T get(String key) { //iOS: objectForKey:
        if (key == null) return null;
        Object val = mEstimatedData.get(key);
        return (T)val;
    }

    /**
     * 指定フィールドに値を格納します。
     * @param field フィールド
     * @param value 値
     */
    public void put(ABField field, Object value) { //iOS: setObject:forKey:
        put(field.getKey(), value);
    }

    /**
     * 指定キーに値を格納します。
     * @param key キー
     * @param value 値
     */
    public void put(String key, Object value) { //iOS: setObject:forKey:
        if (key == null) return;

        Object oldVal = mEstimatedData.get(key);
        if (!mOriginalData.keySet().contains(key) && oldVal != null) {
            mOriginalData.put(key, oldVal); //キーが存在しない(=初回)場合のみoriginalへ値を突っ込む
        }
        Object fixedValue = inputDataFilter(key, value);
        mEstimatedData.put(key, fixedValue);
        if (mOriginalData.keySet().contains(key)) {
            mUpdatedKeys.add(key);
        }
        mDirty = true; //XXX: オブジェクトの場合同一視される？かもなのでやっぱり常に立てることにする
    }

    /**
     * 引数エントリに内包するキー／値をすべて取り込みます。
     * @param entry エントリ
     */
    public void put(Map.Entry<String, Object> entry) {
        if (entry == null) return;

        put(entry.getKey(), entry.getValue());
    }

    /**
     * entrySet に内包するキー／値をすべて取り込みます。
     * @param entrySet entrySet
     */
    public void putAll(Set<Map.Entry<String, Object>> entrySet) {
        for (Map.Entry<String, Object> entry : entrySet) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 指定フィールドを削除します。
     * @param field フィールド
     */
    public void remove(ABField field) {
        remove(field.getKey());
    }

    /**
     * 指定キーを削除します。
     * @param key キー
     */
    public void remove(String key) { //iOS: removeObjectForKey:
        if (key == null) return;
        if (mEstimatedData.keySet().contains(key)) {
            mEstimatedData.remove(key);
            mDirty = true;
        }
    }

    /**
     * 引数に指定したキー・リストに含まれるキーをすべて削除します。
     * @param keys キー・リスト
     */
    public void removeAll(Set<String> keys) {
        if (keys == null || keys.size() == 0) return;
        for (String key : keys) {
            remove(key);
            mDirty = true;
        }
    }

    /**
     * すべてのキー／値を削除します。
     */
    public void clear() { //iOS: removeAllObjects
        mEstimatedData.clear();
        mDirty = true;
    }

    /**
     * apply()メソッド実行以降にオブジェクトに追加されたキーを取得します。
     * @return 追加されたキー
     */
    public Set<String> getAddedKeys() {
        Set<String> addedKeys = new HashSet<>();
        for (String key : mEstimatedData.keySet()) {
            addedKeys.add(key);
        }
        addedKeys.removeAll(mOriginalData.keySet());
        return addedKeys;
    }

    /**
     * apply()メソッド実行以降にオブジェクトに追加されたキー／値を取得します。
     * @return 追加されたキー／値
     */
    public Map<String, Object> getAddedKeysAndValues() {
        Set<String> addedKeys = getAddedKeys();
        if (addedKeys.size() == 0) return new HashMap<>(); //empty map

        Map<String, Object> map = new HashMap<>();
        for (String key : addedKeys) {
            Object val = get(key);
            map.put(key, val);
        }
        return map;
    }

    /**
     * apply()メソッド実行以降にオブジェクトから削除されたキーを取得します。
     * @return 削除されたキー
     */
    public Set<String> getRemovedKeys() {
        Set<String> removedKeys = new HashSet<>();
        for (String key : mOriginalData.keySet()) {
            removedKeys.add(key);
        }
        removedKeys.removeAll(mEstimatedData.keySet());
        return removedKeys;
    }

    /**
     * apply()メソッド実行以降にオブジェクトから削除されたキー／値を取得します。
     * @return 削除されたキー／値
     */
    public Map<String, Object> getRemovedKeysAndValues() {
        Set<String> removedKeys = getRemovedKeys();
        if (removedKeys.size() == 0) return new HashMap<>(); //empty map

        Map<String, Object> map = new HashMap<>();
        for (String key : removedKeys) {
            Object val = get(key);
            map.put(key, val);
        }
        return map;
    }

    /**
     * apply()メソッド実行以降に値が更新されたキーを取得します。
     * @return 値が更新されたキー
     */
    public Set<String> getUpdatedKeys() {
        Set<String> updatedKeys = new HashSet<>();
        for (String key : mUpdatedKeys) {
            updatedKeys.add(key);
        }
        return updatedKeys;
    }

    protected void setUpdatedKeys(Set<String> updatedKeys) {
        mUpdatedKeys = updatedKeys;
    }

    /**
     * apply()メソッド実行以降に値が更新されたキー／値を取得します。
     * @return 値が更新されたキー／値
     */
    public Map<String, Object> getUpdatedKeysAndValues() {
        if (mUpdatedKeys.size() == 0) return new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        for (String key : mUpdatedKeys) {
            Object val = get(key);
            map.put(key, val);
        }
        return map;
    }

    /**
     * オブジェクトに加えられた変更（キーの追加／キーの削除／値の更新）をオブジェクトに適用します。
     * <p>
     * その時点のオブジェクトの状態を、オブジェクトに変更が加えられていないオリジナルの状態としてマークします。<br>
     * apply()メソッドを実行すると、isNewフラグおよびisDirtyフラグにはそれぞれ false がセットされます。
     * </p>
     */
    public void apply() {
        mOriginalData.clear();
        for (Map.Entry<String, Object> entry : mEstimatedData.entrySet()) {
            mOriginalData.put(entry.getKey(), entry.getValue());
        }
        mUpdatedKeys.clear();
        mDirty = false;
        mNew = false;
    }

    /**
     * オブジェクトに加えられた変更を破棄します。
     * <p>
     * オブジェクトは、最後にapply()メソッドが実行された時点の状態に復帰します。
     * </p>
     */
    public void revert() {
        mEstimatedData.clear();
        for (Map.Entry<String, Object> entry : mOriginalData.entrySet()) {
            mEstimatedData.put(entry.getKey(), entry.getValue());
        }
        mDirty = false;
    }

    <T extends ABModel> void postRefreshProcessWithFetchedObject(T object) {
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        apply();
        mNew = false;
    }

//endregion

//region Filters

    /**
     * 入力データをフィルタリングします。
     * <p>
     * アピアリーズ BaaS API のレスポンスとして返却されたJSONデータからモデル・インスタンスを生成する直前に実行されます。<br>
     * #outputDataFilter(String, String) と合わせて利用することで、そのままでは BaaS 上に格納することができない
     * ユーザ定義クラスのオブジェクトを、BaaS 上に格納することができるようになります。
     * </p>
     * @param key キー
     * @param value 値
     * @return フィルタ後の値
     */
    public Object inputDataFilter(String key, Object value) {
        Object fixed = value;
        if ("_id".equals(key)) {
            /* NOP */
        } else if ("_cts".equals(key)) {
            fixed = AB.Helper.DateHelper.convert(value);
        } else if ("_cby".equals(key)) {
            /* NOP */
        } else if ("_uts".equals(key)) {
            fixed = AB.Helper.DateHelper.convert(value);
        } else if ("_uby".equals(key)) {
            /* NOP */
        }/* else if ("_coord".equals(key)) {
            List<?> array = (List<?>)value;
            if (array.size() >= 2) {
                fixed = new ABGeoPoint(array.get(1), array.get(0)).doubleValue;
            }
        }*/
        return fixed;
    }

    /**
     * 出力データをフィルタリングします。
     * <p>
     * アピアリーズ BaaS API のリクエストに含めるJSONデータを、モデル・インスタンスから生成する直前に実行されます。<br>
     * #inputDataFilter(String, String) と合わせて利用することで、そのままでは BaaS 上に格納することができない
     * ユーザ定義クラスのオブジェクトを、BaaS 上に格納することができるようになります。
     * </p>
     * @param key キー
     * @param value 値
     * @return フィルタ後の値
     */
    public Object outputDataFilter(String key, Object value) {
        Object fixed = value;
        if ("_id".equals(key)) {
            /* NOP */
        } else if ("_cts".equals(key)) {
            if (value instanceof Date) {
                //fixed = //TODO: covert value from date to long
            }
        } else if ("_cby".equals(key)) {
            /* NOP */
        } else if ("_uts".equals(key)) {
            if (value instanceof Date) {
                //fixed = //TODO: covert value from date to long
            }
        } else if ("_uby".equals(key)) {
            /* NOP */
        }/* else if ("_coord".equals(key)) {
            if (value instanceof ABGeoPoint) {
                ABGeoPoint point = (ABGeoPoint)value;
                fixed = new double[]{ point.getLongitude(), point.getLatitude() };
            }
        }*/
        return fixed;
    }

//endregion

//region Accessors

    public Map<String, Object> getFilteredEstimatedData() {
        Map<String, Object> filteredMap = new HashMap<>();
        for (Map.Entry<String, Object> stringObjectEntry : mEstimatedData.entrySet()) {
            Map.Entry entry = (Map.Entry) stringObjectEntry;
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            Object filteredValue = outputDataFilter(key, value);
            filteredMap.put(key, filteredValue);
        }
        return filteredMap;
    }

    /**
     * 変更適用前データを取得します。
     * @return 変更適用前データ
     */
    public Map<String, Object> getEstimatedData() {
        return mEstimatedData;
    }

    /**
     * 変更適用前データをセットします。
     * @param estimatedData 変更適用前データ
     */
    public void setEstimatedData(Map<String, Object> estimatedData) {
        this.mEstimatedData = new HashMap<>(estimatedData);
    }

    /**
     * オブジェクトのオリジナルデータを取得します。
     * @return オリジナルデータ
     */
    public Map<String, Object> getOriginalData() {
        return mOriginalData;
    }

    /**
     * オブジェクトのオリジナルデータをセットします。
     * @param originalData オリジナルデータ
     */
    public void setOriginalData(Map<String, Object> originalData) {
        this.mOriginalData = new HashMap<>(originalData);
    }

    /**
     * オブジェクトが BaaS 上に登録済みかどうかを取得します。
     * @return true: 登録済み
     */
    public static boolean isRegistered() {
        return false; //TODO: not yet implemented
    }

    /**
     * クラスに紐づくコレクションIDを取得します。
     * @param clazz {@link ABModel} クラスの派生クラス
     * @return コレクションID
     */
    public static String getCollectionID(Class<? extends ABModel> clazz) {
        String collectionID = null;
        ABCollection annotation = clazz.getAnnotation(ABCollection.class);
        if (annotation != null && annotation.value().length() > 0) {
            collectionID = annotation.value();
        }
        return collectionID;
    }

    /**
     * コレクションIDを取得します。
     * @return コレクションID
     */
    public String getCollectionID() {
//        String collectionID =  AB.ClassRepository.getCollectionID(this.getClass());
//        return (collectionID != null) ? collectionID : mCollectionID;
        return mCollectionID != null ? mCollectionID : AB.ClassRepository.getCollectionID(this.getClass());
    }

    protected void setCollectionID(String collectionID) {
        mCollectionID = collectionID;
    }

    /**
     * 新規オブジェクト（BaaS上から取得したものではない）かどうかを取得します。
     * <p>オブジェクトを new した場合は、常に true が返されます。</p>
     * @return true: 新規オブジェクト
     */
    public boolean isNew() {
        return mNew;
    }

    protected void setNew(boolean isNew) {
        mNew = isNew;
    }

    /**
     * 最後にapply()メソッドが実行されたから、オブジェクトに変更が発生したかどうかを取得します。
     * @return true: 変更が発生した
     */
    public boolean isDirty() {
        return mDirty;
    }

    protected void setDirty(boolean isDirty) {
        mDirty = isDirty;
    }

    /**
     * オブジェクトIDを取得します。
     * @return オブジェクトID
     */
    public String getID() {
        return (String)mEstimatedData.get("_id");
    }

    /**
     * オブジェクトIDをセットします。
     * @param ID オブジェクトID
     */
    public void setID(String ID) {
        mEstimatedData.put("_id", ID);
    }

    /**
     * 作成日時を取得します。
     * @return 作成日時
     */
    public Date getCreated() {
        Object val = mEstimatedData.get("_cts");
        if (val instanceof Number) {
            return new Date(((Number)val).longValue() / 1000);
        } else if (val instanceof Date) {
            return (Date)val; //XXX: ホントはここは通っちゃダメ
        } else {
            return null;
        }
    }

    /**
     * 作成日時をセットします。
     * @param created 作成日時
     */
    public void setCreated(Date created) {
        if (created != null) {
            mEstimatedData.put("_cts", created.getTime() * 1000);
        } else {
            mEstimatedData.put("_cts", null);
        }
    }

    /**
     * 作成者を取得します。
     * @return 作成者
     */
    public String getCreatedBy() {
        return (String)mEstimatedData.get("_cby");
    }

    /**
     * 作成者をセットします。
     * @param createdBy 作成者
     */
    public void setCreatedBy(String createdBy) {
        mEstimatedData.put("_cby", createdBy);
    }

    /**
     * 更新日時を取得します。
     * @return 更新日時
     */
    public Date getUpdated() {
        Object val = mEstimatedData.get("_uts");
        if (val instanceof Number) {
            return new Date(((Number)val).longValue() / 1000);
        } else if (val instanceof Date) {
            return (Date)val; //XXX: ホントはここは通っちゃダメ
        } else {
            return null;
        }
    }

    /**
     * 更新日時をセットします。
     * @param updated 更新日時
     */
    public void setUpdated(Date updated) {
        if (updated != null) {
            mEstimatedData.put("_uts", updated.getTime() * 1000);
        } else {
            mEstimatedData.put("_uts", null);
        }
    }

    /**
     * 更新者を取得します。
     * @return 更新者
     */
    public String getUpdatedBy() {
        return (String)mEstimatedData.get("_uby");
    }

    /**
     * 更新者をセットします。
     * @param updatedBy 更新者
     */
    public void setUpdatedBy(String updatedBy) {
        mEstimatedData.put("_uby", updatedBy);
    }

    /**
     * BaaS管理下のクラスを取得します。
     * @return BaaS管理下のクラス
     * @deprecated
     */
    public Class<? super ABModel> getBaaSClass() {
        return ABModel.class;
    }

//endregion

//region Miscellaneous

    /**
     * オブジェクトの文字列表現を取得します。
     * @return オブジェクトの文字列表現
     */
    public String toString() {
        StringBuilder buff = new StringBuilder(this.getClass().getName() + "@" + Integer.toHexString(hashCode()));
        try {
            String jsonString = AB.Helper.ModelHelper.toJson(this);
            buff.append(" : ").append(jsonString);
        } catch (ABException e) {
            ABLog.e(TAG, e.getMessage());
        }
        return buff.toString();
    }

//endregion

//region Cloneable

    /**
     * オブジェクトを複製します。
     * @return 複製した {@link ABModel} オブジェクト
     * @throws CloneNotSupportedException if this object's class does not implement the {@code Cloneable} interface.
     * @see java.lang.Object#clone()
     */
    public ABModel clone() throws CloneNotSupportedException {
        ABModel clone = (ABModel)super.clone();
        clone.setEstimatedData(new HashMap<>(this.getEstimatedData()));
        clone.setOriginalData(new HashMap<>(this.getOriginalData()));
        clone.setUpdatedKeys(this.getUpdatedKeys());
        Set<String> updatedKeys = new HashSet<>();
        updatedKeys.addAll(this.getUpdatedKeys());
        clone.setUpdatedKeys(updatedKeys);
        clone.setNew(this.isNew());
        clone.setDirty(this.isDirty());
        clone.setCollectionID(this.getCollectionID());
        return clone;
    }

//endregion

}
