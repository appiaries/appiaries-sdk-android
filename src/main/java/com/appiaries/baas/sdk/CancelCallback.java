package com.appiaries.baas.sdk;

/**
 * キャンセル処理用コールバック。
 */
public abstract class CancelCallback {

    public abstract void done(boolean success);

    final void internalDone(boolean success) {
        done(success);
    }
}
