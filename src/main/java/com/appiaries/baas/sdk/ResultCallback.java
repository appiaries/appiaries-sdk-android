//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

/**
 * 結果取得用コールバック。
 * @param <T>
 */
public abstract class ResultCallback<T> {

    public abstract void done(ABResult<T> result, ABException e);

    final void internalDone(ABResult<T> result, ABException e) {
        done(result, e);
    }
}
