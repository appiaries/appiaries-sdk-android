//
// Created by Appiaries Corporation on 15/04/07.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

interface AsyncCallback<T> {
    void onPreExecute();
    void onPostExecute(ABResult<T> result, ABException e);
    void onProgressUpdate(int progress);
    void onCancelled();
}
