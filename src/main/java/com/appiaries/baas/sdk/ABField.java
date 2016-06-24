//
// Created by Appiaries Corporation on 15/04/13.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

/**
 * フィールド。
 * <p>モデルが持つフィールドのキーとデータ型を定義する際に使用します。</p>
 * @version 2.0.0
 * @since 2.0.0
 */
public class ABField {

    public static final String UNSTABLE_KEY = "";

    private String mKey;
    private Class mType;
    //private Object mInputFilter;
    //private Object mOutputFilter;

    public ABField(String key, Class type) {
        mKey = key;
        mType = type;
    }

    public String getKey() {
        return mKey;
    }

    public Class getType() {
        return mType;
    }
/*
    public Object getInputFilter() {
        return mInputFilter;
    }

    public Object getOutputFilter() {
        return mOutputFilter;
    }
*/
    public boolean equals(Object o) {
        return o instanceof ABField
                && mKey.equals(((ABField) o).getKey())
                && mType.equals(((ABField) o).getType());
    }

    public String toString() {
        return "Key: '" + "'" + mKey + ", Type: '" + mType.toString() + "'";
    }

}
