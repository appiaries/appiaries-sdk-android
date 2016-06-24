//
// Created by Appiaries Corporation on 15/07/10.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//
package com.appiaries.baas.sdk;

/**
 * クエリ条件を連結する。
 */
public abstract class ConditionBundler {

    public abstract ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle);

}
