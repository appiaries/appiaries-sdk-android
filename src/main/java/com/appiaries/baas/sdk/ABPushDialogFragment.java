package com.appiaries.baas.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * プッシュ通知ダイアログ・フラグメント。
 * <p>プッシュ通知受信時にメッセージを表示するダイアログ・フラグメントです。</p>
 */
public class ABPushDialogFragment extends DialogFragment {

    public static ABPushDialogFragment newInstance(ABPushMessage message) {
        return newInstance(message, AB.Config.Push.getDefaultConfiguration());
    }
    public static ABPushDialogFragment newInstance(ABPushMessage message, ABPushConfiguration configuration) {
        ABPushDialogFragment instance = new ABPushDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(AB.EXTRA_KEY_PUSH_MESSAGE, message);
        args.putSerializable(AB.EXTRA_KEY_CONFIG, configuration);
        instance.setArguments(args);
        return instance;
    }

    private Dialog internalDialog;

    /**
     * ダイアログを取得する。
     * @return ダイアログ
     */
    public Dialog getDialog() {
        return internalDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
/*
        ABPushMessage message = (ABPushMessage) getArguments().getSerializable(AB.EXTRA_KEY_PUSH_MESSAGE);
        ABPushConfiguration config = (ABPushConfiguration) getArguments().getSerializable(AB.EXTRA_KEY_CONFIG);
        ABPushDialogConfiguration dialogConfig = config.getDialogConfiguration();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(message.getTitle())
                .setMessage(message.getMessage())
                .setCancelable(false) //NOTE: バックボタンでダイアログをキャンセルできないようにする
                .setPositiveButton(dialogConfig.getPositiveButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        listener.doPositiveClick();
                        dismiss();
                    }
                }).setNegativeButton(dialogConfig.getNegativeButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        listener.doNegativeClick();
                        dismiss();
                    }
                });
        return builder.create();
*/
        return createDialog();
    }

    private Dialog createDialog() {

        final ABPushMessage message = (ABPushMessage) getArguments().getSerializable(AB.EXTRA_KEY_PUSH_MESSAGE);
        final ABPushConfiguration config = (ABPushConfiguration) getArguments().getSerializable(AB.EXTRA_KEY_CONFIG);
        final ABPushDialogConfiguration dialogConfig = config.getDialogConfiguration();

        final Activity activity = getActivity();

        internalDialog = new Dialog(activity);
        internalDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        internalDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        internalDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //Title View
        //>> Icon
        ImageView icon = null;
        if (config.getIconVisibility()) {
            Drawable drawable;
            icon = new ImageView(activity);
            String drawableName = config.getIconName();
            if (drawableName != null && !drawableName.isEmpty()) {
                int id = getResources().getIdentifier(config.getIconName(), "drawable", activity.getPackageName());
                drawable = getResources().getDrawable(id);
            } else {
                drawable = getAppIcon();
            }
            icon.setImageDrawable(drawable);
            icon.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, WRAP_CONTENT));
        }

        //>> titleTextView
        TextView titleTextView = new TextView(activity);
        titleTextView.setSingleLine();
        titleTextView.setFocusableInTouchMode(true);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        titleTextView.setTextSize(2, dialogConfig.getTitleViewFontSize());
        titleTextView.setTextColor(dialogConfig.getTitleViewForegroundColor());
        titleTextView.setGravity(Gravity.CENTER_VERTICAL);
        titleTextView.setPadding(dp2px(6), dp2px(0), dp2px(0), dp2px(0));
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp2px(45)));
        titleTextView.setText(message.getTitle());
        //>> titleLayout
        LinearLayout titleLayout = new LinearLayout(activity);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        if (icon != null) {
            titleLayout.addView(icon);
        }
        titleLayout.addView(titleTextView);
        LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
        Button closeButton = new Button(activity);
        closeButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
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
        View spacer = new View(activity);
        if (config.getDialogConfiguration().getStyle() == AB.PushDialogStyle.FLAT) {
            spacer.setLayoutParams(new LinearLayout.LayoutParams(dp2px(0), dp2px(0)));
        } else {
            spacer.setLayoutParams(new LinearLayout.LayoutParams(dp2px(8), dp2px(1)));
        }
        AlphaAnimation anim = new AlphaAnimation(0.0f, 0.0f);
        anim.setDuration(0);
        anim.setFillAfter(true);
        spacer.startAnimation(anim);
        //>> actionButton
        Button actionButton = new Button(activity);
        actionButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        actionButton.setTextColor(dialogConfig.getButtonsViewForegroundColor());
        actionButton.setTextSize(2, dialogConfig.getButtonsViewFontSize());
        actionButton.setHeight(dp2px(40));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            GradientDrawable drawable = createGradientDrawableForButtonsView(dialogConfig);
            if (config.getDialogConfiguration().getStyle() == AB.PushDialogStyle.FLAT) {
                LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable});
                layerDrawable.setLayerInset(0, - dp2px(dialogConfig.getButtonsViewBorderWidth()), 0, 0, 0); //FLATの場合はボタン隣接辺のボーダーの重なりを打ち消すためオフセットを指定する
                actionButton.setBackgroundDrawable(layerDrawable);
            } else {
                actionButton.setBackgroundDrawable(drawable);
            }
        } else {
            GradientDrawable drawable = createGradientDrawableForButtonsView(dialogConfig);
            if (config.getDialogConfiguration().getStyle() == AB.PushDialogStyle.FLAT) {
                LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable});
                layerDrawable.setLayerInset(0, - dp2px(dialogConfig.getButtonsViewBorderWidth()), 0, 0, 0); //FLATの場合はボタン隣接辺のボーダーの重なりを打ち消すためオフセットを指定する
                actionButton.setBackground(layerDrawable);
            } else {
                actionButton.setBackground(drawable);
            }
        }
        actionButton.setPadding(dp2px(2), dp2px(2), dp2px(2), dp2px(2));
        actionButton.setText(dialogConfig.getPositiveButtonText());
        //>> buttonsLayout
        LinearLayout buttonsLayout = new LinearLayout(activity);
        buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        if (config.getDialogConfiguration().getStyle() == AB.PushDialogStyle.FLAT) {
            buttonsLayout.setPadding(dp2px(0), dp2px(0), dp2px(0), dp2px(0));
        } else {
            buttonsLayout.setPadding(dp2px(6), dp2px(6), dp2px(6), dp2px(6));
        }
        buttonsLayout.addView(closeButton);
        buttonsLayout.addView(spacer);
        buttonsLayout.addView(actionButton);

        if (dialogConfig.getNegativeButtonOnClickListener() == null) {
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    internalDialog.dismiss();
                }
            });
        } else {
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogConfig.getNegativeButtonOnClickListener().onClick(v);
                    internalDialog.dismiss();
                }
            });
        }

        final Intent newIntent = new Intent(){{
            putExtra(AB.EXTRA_KEY_CONFIG, config);
            putExtra(AB.EXTRA_KEY_PUSH_MESSAGE, message);
        }};

        if (dialogConfig.getPositiveButtonOnClickListener() == null) {
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    internalDialog.dismiss();
                    if (config.isOpenMessage()) {
                        //開封通知
                        message.open();
                    }
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
                            launch.setClassName(activity, activityClass);
                            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (url != null && url.length() > 0) {
                                launch.putExtra(AB.EXTRA_KEY_URL, url);
                            }
                            activity.startActivity(launch);
                            //getApplicationContext().startActivity(launch);
                            //AB.sApplicationContext.startActivity(launch);
                            //Context appContext = v.getContext().getApplicationContext();
                            //appContext.startActivity(launch);
                            NotificationManager notificationManager = (NotificationManager) v.getContext().getSystemService(Service.NOTIFICATION_SERVICE);
                            notificationManager.cancelAll();
                        }
                    }
                }
            });
        } else {
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogConfig.getPositiveButtonOnClickListener().onClick(v);
                    internalDialog.dismiss();
                }
            });
        }

        //Base View
        //>> messageTextView
        TextView messageTextView = new TextView(activity);
        messageTextView.setEllipsize(TextUtils.TruncateAt.END);
        messageTextView.setTextSize(2, dialogConfig.getBaseViewFontSize());
        messageTextView.setTextColor(dialogConfig.getBaseViewForegroundColor());
        String msg = message.getMessage();
        fixMessageTextViewHeight(messageTextView, msg);
        messageTextView.setText(msg);
        //>> messageLayout
        LinearLayout messageLayout = new LinearLayout(activity);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setPadding(dp2px(6), dp2px(-3), dp2px(6), dp2px(0));
        LinearLayout.LayoutParams messageLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        messageLayoutParams.setMargins(15, 10, 15, 10);
        messageLayoutParams.gravity = 5;
        messageLayout.setLayoutParams(messageLayoutParams);
        messageLayout.addView(messageTextView);
        //>> dialogLayout
        LinearLayout dialogLayout = new LinearLayout(activity);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            dialogLayout.setBackgroundDrawable(createGradientDrawableForBaseView(dialogConfig));
        } else {
            dialogLayout.setBackground(createGradientDrawableForBaseView(dialogConfig));
        }
        FrameLayout.LayoutParams dialogLayoutParams = new FrameLayout.LayoutParams(dp2px(300), WRAP_CONTENT);
        dialogLayoutParams.setMargins(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialogLayoutParams.gravity = Gravity.CENTER;
        dialogLayout.setLayoutParams(dialogLayoutParams);
        dialogLayout.addView(titleLayout);
        dialogLayout.addView(messageLayout);
        dialogLayout.addView(buttonsLayout);

        //Base layout
        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.setBackgroundColor(Color.parseColor("#A0000000"));
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        frameLayout.addView(dialogLayout);
        internalDialog.setContentView(frameLayout);

        return internalDialog;
    }

    //メッセージの文字数に応じてtextViewの高さを調整する
    private void fixMessageTextViewHeight(TextView textView, String message) {
        if (message == null) return;

        int length = message.length();
        if (length < 40) {
            textView.setLayoutParams(new LinearLayout.LayoutParams(dp2px(300), dp2px(50),  1.0f));
        } else if (41 < length && length < 80) {
            textView.setLayoutParams(new LinearLayout.LayoutParams(dp2px(300), dp2px(90),  1.0f));
        } else {
            textView.setLayoutParams(new LinearLayout.LayoutParams(dp2px(300), dp2px(130), 1.0f));
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
        Activity activity = getActivity();
        PackageManager pm = activity.getPackageManager();
        List<ApplicationInfo> applications = pm.getInstalledApplications(1);
        Drawable icon = null;
        String packageName = activity.getApplicationContext().getPackageName();
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

    /*
    @Override
    public AlertDialog getDialog() {
        return dialog;
    }*/
    /*
    public void setDialogListener(DialogListener listener) {
        this.listener = listener;
    }
    public void removeDialogListener() {
        this.listener = null;
    }

    private static interface DialogListener extends EventListener {
        public void doPositiveClick();
        public void doNegativeClick();
    }
    */
}
