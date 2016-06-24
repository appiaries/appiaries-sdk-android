//
// Created by Appiaries Corporation on 15/06/05.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//
package com.appiaries.baas.sdk;

import android.util.Log;

final class ABLog {

    static int v(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            return Log.v(tag, msg);
        } else {
            return 0;
        }
    }

    static int v(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            return Log.v(tag, msg, tr);
        } else {
            return 0;
        }
    }

    static int d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            return Log.d(tag, msg);
        } else {
            return 0;
        }
    }

    static int d(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            return Log.d(tag, msg, tr);
        } else {
            return 0;
        }
    }

    static int i(String tag, String msg) {
        return Log.i(tag, msg);
    }

    static int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }

    static int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    static int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    static int w(String tag, Throwable tr) {
        return Log.w(tag, tr);
    }

    static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    static String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }

    static int println(int priority, String tag, String msg) {
        return Log.println(priority, tag, msg);
    }

}
