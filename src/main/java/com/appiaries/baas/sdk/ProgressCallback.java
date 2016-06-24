//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

/**
 * 処理進捗取得用コールバック。
 */
public abstract class ProgressCallback {

    public abstract void updateProgress(float progress);

    final void internalUpdateProgress(float progress) {
        updateProgress(progress);
    }

}
