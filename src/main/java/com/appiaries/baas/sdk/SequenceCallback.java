package com.appiaries.baas.sdk;

/**
 * シーケンス取得用コールバック。
 */
public abstract class SequenceCallback {

    public abstract void done(long value, ABException e);

    final void internalDone(long value, ABException e) {
        done(value, e);
    }
}
