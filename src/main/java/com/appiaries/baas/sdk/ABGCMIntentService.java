package com.appiaries.baas.sdk;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.List;

/**
 * GCM用インテント・サービス。
 */
public class ABGCMIntentService extends IntentService {
    private static final String TAG = ABGCMIntentService.class.getSimpleName();

    public ABGCMIntentService() {
        super("ABGCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ABLog.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>> onHandleIntent()");

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String messageType = gcm.getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification(intent);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification(intent);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification(intent);
            }
        }
        ABGCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Intent intent) {

        ABPushMessage message = new ABPushMessage(intent);

        if (message.isValid()) {

            PackageManager pm = getPackageManager();
            String packageName = getApplicationContext().getPackageName();
            Intent i = pm.getLaunchIntentForPackage(packageName);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtras(intent);

            boolean foreground = isForeground(packageName);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            boolean locked = km.inKeyguardRestrictedInputMode();

            if (foreground && !locked) {
                //アプリがフォアグラウンドかつ非ロック状態であればそのままダイアログを LAUNCHER Activity に表示する
                startActivity(i); //NOTE: これがうまくいくのは MainActivity が singleTask だから？
                //NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                //nm.cancelAll(); //XXX: 必要かどうかよくわからない
                return;
            }

            i.putExtra("via_notification", true); //NOTE: 通知バー経由でのintentを識別させるためのマーカー
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

            ABPushConfiguration config = AB.Config.Push.getDefaultConfiguration();

            int iconId;
            String drawableName = config.getIconName();
            if (drawableName != null && !drawableName.isEmpty()) {
                iconId = this.getResources().getIdentifier(config.getIconName(), "drawable", this.getPackageName());
            } else {
                iconId = getAppIconID(this);
            }

            long when = System.currentTimeMillis();
            String title = message.getTitle();
            String msg = message.getMessage();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            if (foreground) {
                builder.setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setWhen(when);

                //NOTE: フォアグラウンド時はサウンド・バイブレーション・ライトなどの知覚効果は使用しない。

            } else {
                builder.setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg)
                        .setTicker(title) //5.0からstatusBar表示には影響しなくなったので冗長か
                        .setFullScreenIntent(contentIntent, true)
                        //.setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setWhen(when);

                int defaults = 0;

                if (config.getVibratePattern() != null) {
                    builder.setVibrate(config.getVibratePattern());
                } else {
                    defaults |= NotificationCompat.DEFAULT_VIBRATE;
                }

                if (config.getSound() != null) {
                    Uri uri = Uri.parse(config.getSound());
                    builder.setSound(uri);
                } else {
                    defaults |= NotificationCompat.DEFAULT_SOUND;
                }

                if (config.getLights() != null) {
                    int[] lights = config.getLights();
                    if (lights.length == 3) {
                        builder.setLights(lights[0], lights[1], lights[2]);
                    }
                } else {
                    defaults |= NotificationCompat.DEFAULT_LIGHTS;
                }

                builder.setDefaults(defaults);
            }
            builder.setContentIntent(contentIntent);

            NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify((int)when, builder.build()); //unsafe cast
        }
    }

    private static int getAppIconID(Context context) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getApplicationContext().getPackageName();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return appInfo.icon;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    //see http://androidblog.reindustries.com/check-if-an-android-activity-is-currently-running/
    private boolean isForeground(String PackageName) {
        // Get the Activity Manager
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        // Get a list of running tasks, we are only interested in the last one,
        // the top most so we give a 1 as parameter so we only get the topmost.
        List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(1);

        // Get the info we need for comparison.
        ComponentName componentInfo = task.get(0).topActivity;

        // Check if it matches our package name.
        if (componentInfo.getPackageName().equals(PackageName)) return true;

        // If not then our app is not on the foreground.
        return false;
    }
}
