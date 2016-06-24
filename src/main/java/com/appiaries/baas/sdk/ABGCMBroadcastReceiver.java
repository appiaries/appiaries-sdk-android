package com.appiaries.baas.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * GCM用ブロードキャスト・レシーバ。
 */
public class ABGCMBroadcastReceiver extends WakefulBroadcastReceiver {
    private static String TAG = ABGCMBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        ABLog.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>> onReceive()");
        ComponentName comp = new ComponentName(context.getPackageName(), ABGCMIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
