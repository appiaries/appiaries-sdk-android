//
// Created by Appiaries Corporation on 15/05/01.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//
package com.appiaries.baas.sdk;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * プッシュ通知ダイアログ・アクティビティ。
 * <p>プッシュ通知受信時にメッセージを表示するダイアログ・アクティビティです。</p>
 */
public class ABPushDialogActivity extends Activity {
    private static String TAG = ABPushDialogActivity.class.getSimpleName();

    private PowerManager.WakeLock mWakeLock;

    private ABPushConfiguration config;
    private ABPushMessage message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        config  = (ABPushConfiguration) getIntent().getSerializableExtra(AB.EXTRA_KEY_CONFIG);
        if (config == null) {
            AB.Config.Push.getDefaultConfiguration();
        }
        message = (ABPushMessage) getIntent().getSerializableExtra(AB.EXTRA_KEY_PUSH_MESSAGE);

        setup();

        showWhenLocked();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void showWhenLocked() {
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, ABPushDialogActivity.class.getName());
        mWakeLock.acquire();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        long waitBefore = System.currentTimeMillis();
        while (!pm.isScreenOn()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) { }
            long waitAfter = System.currentTimeMillis();
            long delta = waitAfter - waitBefore;
            if (delta > 9999) {
                return;
            }
        }

        final ABPushConfiguration config =
                (ABPushConfiguration)getIntent().getSerializableExtra(AB.EXTRA_KEY_CONFIG);

        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null && config.getVibratePattern() != null) {
            v.vibrate(config.getVibratePattern(), -1);
        }
    }

    private void setup() {
        final ABPushDialogConfiguration dialogConfig = config.getDialogConfiguration();

        //Title View
        //>> Icon
        ImageView icon = null;
        if (config.getIconVisibility()) {
            Drawable drawable;
            icon = new ImageView(this);
            String drawableName = config.getIconName();
            if (drawableName != null && !drawableName.isEmpty()) {
                int id = getResources().getIdentifier(config.getIconName(), "drawable", getPackageName());
                drawable = getResources().getDrawable(id);
            } else {
                drawable = getAppIcon();
            }
            icon.setImageDrawable(drawable);
            icon.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, WRAP_CONTENT));
        }

        //>> titleTextView
        TextView titleTextView = new TextView(this);
        titleTextView.setSingleLine();
        titleTextView.setFocusableInTouchMode(true);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        titleTextView.setTextSize(2, dialogConfig.getTitleViewFontSize());
        titleTextView.setTextColor(dialogConfig.getTitleViewForegroundColor());
        titleTextView.setGravity(Gravity.CENTER_VERTICAL);
        titleTextView.setPadding(dp2px(6), dp2px(0), dp2px(0), dp2px(0));
        titleTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(45)));
        titleTextView.setText(message.getTitle());
        //>> titleLayout
        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        if (icon != null) {
            titleLayout.addView(icon);
        }
        titleLayout.addView(titleTextView);
        LayoutParams titleLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleLayoutParams.setMargins(15, 15, 15, 15);
        titleLayoutParams.gravity = Gravity.END; // deprecated Gravity.RIGHT
        titleLayout.setLayoutParams(titleLayoutParams);
        titleLayout.setPadding(10, 10, 10, 10);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            titleLayout.setBackgroundDrawable(createGradientDrawableForTitleView(dialogConfig));
        } else {
            titleLayout.setBackground(createGradientDrawableForTitleView(dialogConfig));
        }

        //Buttons View
        //>> closeButton
        Button closeButton = new Button(this);
        closeButton.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        closeButton.setTextColor(dialogConfig.getButtonsViewForegroundColor());
        closeButton.setTextSize(2, dialogConfig.getButtonsViewFontSize());
        closeButton.setHeight(dp2px(40));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            closeButton.setBackgroundDrawable(createGradientDrawableForButtonsView(dialogConfig));
        } else {
            closeButton.setBackground(createGradientDrawableForButtonsView(dialogConfig));
        }
        closeButton.setPadding(dp2px(2), dp2px(2), dp2px(2), dp2px(2));
        closeButton.setText(dialogConfig.getNegativeButtonText());
        //>> spacer
        View spacer = new View(this);
        spacer.setLayoutParams(new LayoutParams(dp2px(8), dp2px(1)));
        AlphaAnimation anim = new AlphaAnimation(0.0f, 0.0f);
        anim.setDuration(0);
        anim.setFillAfter(true);
        spacer.startAnimation(anim);
        //>> actionButton
        Button actionButton = new Button(this);
        actionButton.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        actionButton.setTextColor(dialogConfig.getButtonsViewForegroundColor());
        actionButton.setTextSize(2, dialogConfig.getButtonsViewFontSize());
        actionButton.setHeight(dp2px(40));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            actionButton.setBackgroundDrawable(createGradientDrawableForButtonsView(dialogConfig));
        } else {
            actionButton.setBackground(createGradientDrawableForButtonsView(dialogConfig));
        }
        actionButton.setPadding(dp2px(2), dp2px(2), dp2px(2), dp2px(2));
        actionButton.setText(dialogConfig.getPositiveButtonText());
        //>> buttonsLayout
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setPadding(dp2px(6), dp2px(6), dp2px(6), dp2px(6));
        buttonsLayout.addView(closeButton);
        buttonsLayout.addView(spacer);
        buttonsLayout.addView(actionButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Context activity = this;
        final Intent newIntent = new Intent(){{
            putExtra(AB.EXTRA_KEY_CONFIG, config);
            putExtra(AB.EXTRA_KEY_PUSH_MESSAGE, message);
        }};
        actionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
//                AB.PushService.handlePushIfNeeded(activity, newIntent);
/**/
/*
                //開封通知
                String pushId = getIntent().getExtras().getString(AB.EXTRA_KEY_PUSH_ID);
                if (pushId != null) {
                    ABPushMessage message = new ABPushMessage();
                    message.setPushId(Long.parseLong(pushId));
                    AB.PushService.openMessage(message, new ResultCallback<Void>() {
                        @Override
                        public void done(ABResult<Void> result, ABException e) {
                            if (e != null) {
                                ABLogger.e(TAG, e.getMessage());
                            }
                        }
                    });
                }
*/
                //リッチ・プッシュ
                String url = message.getUrl();
                if (config.getLaunchActivityClass() == null && url != null && url.length() > 0) {
                    //ブラウザ(またはユーザ指定のアプリケーション)でURLを開く
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    //LAUNCH_ACTIVITY に指定されたアクティビティを起動する
                    String activityClass = config.getLaunchActivityClass();
                    if (activityClass != null) {
                        Intent launch = new Intent();
                        launch.setClassName(getApplicationContext(), activityClass);
                        launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (url != null && url.length() > 0) {
                            launch.putExtra(AB.EXTRA_KEY_URL, url);
                        }
                        getApplicationContext().startActivity(launch);
                        //getApplicationContext().startActivity(launch);
                        //AB.sApplicationContext.startActivity(launch);
                        //Context appContext = v.getContext().getApplicationContext();
                        //appContext.startActivity(launch);
                        NotificationManager notificationManager = (NotificationManager) v.getContext().getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();
                    }
                }
/**/
            }
        });

        //Base View
        //>> messageTextView
        TextView messageTextView = new TextView(this);
        messageTextView.setEllipsize(TextUtils.TruncateAt.END);
        messageTextView.setTextSize(2, dialogConfig.getBaseViewFontSize());
        messageTextView.setTextColor(dialogConfig.getBaseViewForegroundColor());
        String msg = message.getMessage();
        fixMessageTextViewHeight(messageTextView, msg);
        messageTextView.setText(msg);
        //>> messageLayout
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setPadding(dp2px(6), dp2px(-3), dp2px(6), dp2px(0));
        LayoutParams messageLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        messageLayoutParams.setMargins(15, 10, 15, 10);
        messageLayoutParams.gravity = 5;
        messageLayout.setLayoutParams(messageLayoutParams);
        messageLayout.addView(messageTextView);
        //>> dialogLayout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            dialogLayout.setBackgroundDrawable(createGradientDrawableForBaseView(dialogConfig));
        } else {
            dialogLayout.setBackground(createGradientDrawableForBaseView(dialogConfig));
        }
        FrameLayout.LayoutParams dialogLayoutParams = new FrameLayout.LayoutParams(dp2px(300), WRAP_CONTENT);
        dialogLayoutParams.setMargins(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        dialogLayoutParams.gravity = Gravity.CENTER;
        dialogLayout.setLayoutParams(dialogLayoutParams);
        dialogLayout.addView(titleLayout);
        dialogLayout.addView(messageLayout);
        dialogLayout.addView(buttonsLayout);

        //Base layout
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(Color.parseColor("#A0000000"));
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        frameLayout.addView(dialogLayout);
        setContentView(frameLayout);
    }

    //メッセージの文字数に応じてtextViewの高さを調整する
    private void fixMessageTextViewHeight(TextView textView, String message) {
        if (message == null) return;

        int length = message.length();
        if (length < 40) {
            textView.setLayoutParams(new LayoutParams(dp2px(300), dp2px(50),  1.0f));
        } else if (41 < length && length < 80) {
            textView.setLayoutParams(new LayoutParams(dp2px(300), dp2px(90),  1.0f));
        } else {
            textView.setLayoutParams(new LayoutParams(dp2px(300), dp2px(130), 1.0f));
        }
    }

    private GradientDrawable createGradientDrawableForBaseView(ABPushDialogConfiguration dialogConfig) {
        GradientDrawable drawable;
        if (dialogConfig.getBaseViewBackgroundGradientOrientation() != null && dialogConfig.getBaseViewBackgroundGradientColors() != null) {
            drawable = new GradientDrawable(dialogConfig.getBaseViewBackgroundGradientOrientation(), dialogConfig.getBaseViewBackgroundGradientColors());
        } else {
            drawable = new GradientDrawable();
        }
        drawable.setColor(dialogConfig.getBaseViewBackgroundColor());
        drawable.setCornerRadius(dialogConfig.getBaseViewCornerRadius());
        drawable.setStroke(dp2px(dialogConfig.getBaseViewBorderWidth()), dialogConfig.getBaseViewBorderColor());
        return drawable;
    }

    private GradientDrawable createGradientDrawableForTitleView(ABPushDialogConfiguration dialogConfig) {
        GradientDrawable drawable;
        if (dialogConfig.getTitleViewBackgroundGradientOrientation() != null && dialogConfig.getTitleViewBackgroundGradientColors() != null) {
            drawable = new GradientDrawable(dialogConfig.getTitleViewBackgroundGradientOrientation(), dialogConfig.getTitleViewBackgroundGradientColors());
        } else {
            drawable = new GradientDrawable();
        }
        drawable.setColor(dialogConfig.getTitleViewBackgroundColor());
        drawable.setCornerRadius(dialogConfig.getTitleViewCornerRadius());
        drawable.setStroke(dp2px(dialogConfig.getTitleViewBorderWidth()), dialogConfig.getTitleViewBorderColor());
        return drawable;
    }

    private GradientDrawable createGradientDrawableForButtonsView(ABPushDialogConfiguration dialogConfig) {
        GradientDrawable drawable;
        if (dialogConfig.getButtonsViewBackgroundGradientOrientation() != null && dialogConfig.getButtonsViewBackgroundGradientColors() != null) {
            drawable = new GradientDrawable(dialogConfig.getButtonsViewBackgroundGradientOrientation(), dialogConfig.getButtonsViewBackgroundGradientColors());
        } else {
            drawable = new GradientDrawable();
        }
        drawable.setColor(dialogConfig.getButtonsViewBackgroundColor());
        drawable.setCornerRadius(dialogConfig.getButtonsViewCornerRadius());
        drawable.setStroke(dp2px(dialogConfig.getButtonsViewBorderWidth()), dialogConfig.getButtonsViewBorderColor());
        return drawable;
    }

    private Drawable getAppIcon() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> applications = pm.getInstalledApplications(1);
        Drawable icon = null;
        String packageName = getApplicationContext().getPackageName();
        for (ApplicationInfo app : applications) {
            try {
                if (!packageName.equals(app.packageName)) {
                    continue;
                }
                icon = pm.getApplicationIcon(app.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                return icon;
            }
        }
        return icon;
    }

    private int dp2px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }
}
