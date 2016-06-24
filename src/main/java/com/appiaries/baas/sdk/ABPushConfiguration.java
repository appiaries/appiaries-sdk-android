package com.appiaries.baas.sdk;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.Serializable;

/**
 * プッシュ通知設定。
 * <p>アピアリーズのプッシュ通知を利用するための各種設定を保持します。</p>
 */
public class ABPushConfiguration implements Serializable, Cloneable {
    private static final long serialVersionUID = -1067724579752898538L;

    private String mSenderID;
    private AB.PushMode mMode;
    private String mAction;
    private boolean mIconVisibility;
    private String mIconName;
    private long[] mVibratePattern;
    private String mSound;
    private int[] mLights;
    private boolean mOpenMessage; //開封通知フラグ
    private String mLaunchActivityClass; //_openUrlで指定されたWebコンテンツを表示するActivity (default=null で、nullの場合は暗黙的インテントでWebコンテンツを表示する)
    private ABPushDialogConfiguration mDialogConfig;

    public static AB.PushMode DEFAULT_MODE = AB.PushMode.NOTIFICATION;

    public ABPushConfiguration() {
        mSenderID = null;
        mMode = DEFAULT_MODE;
        mAction = Intent.ACTION_MAIN;
        mIconVisibility = true;
        mIconName = null;
        mVibratePattern = new long[]{100, 200, 100, 500};
        mSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString();
        mLights = null;
        mOpenMessage = false;
        mLaunchActivityClass = null;
        mDialogConfig = new ABPushDialogConfiguration();
    }


    public String getSenderID() {
        return mSenderID;
    }
    public void setSenderID(String senderID) {
        mSenderID = senderID;
    }

    public AB.PushMode getMode() {
        return mMode;
    }
    public void setMode(AB.PushMode mode) {
        mMode = mode;
    }

    public boolean isOpenMessage() {
        return mOpenMessage;
    }
    public void setOpenMessage(boolean flag) {
        mOpenMessage = flag;
    }

    public String getAction() {
        return mAction;
    }
    public void setAction(String action) {
        mAction = action;
    }

    public String getLaunchActivityClass() {
        return mLaunchActivityClass;
    }
    public void setLaunchActivityClass(String activityClass) {
        mLaunchActivityClass = activityClass;
    }

    public ABPushDialogConfiguration getDialogConfiguration() {
        return mDialogConfig;
    }
    public void setDialogConfiguration(ABPushDialogConfiguration configuration) {
        mDialogConfig = configuration;
    }

    public boolean getIconVisibility() {
        return mIconVisibility;
    }

    public void setIconVisibility(boolean visible) {
        this.mIconVisibility = visible;
    }

    public String getIconName() {
        return mIconName;
    }

    public void setIconName(String name) {
        this.mIconName = name;
    }

    public long[] getVibratePattern() {
        return mVibratePattern;
    }
    public void setVibratePattern(long[] pattern) {
        mVibratePattern = pattern;
    }

    public String getSound() {
        return mSound;
    }
    public void setSound(String sound) {
        mSound = sound;
    }

    public int[] getLights() {
        return mLights;
    }
    public void setLights(int[] lights) {
        mLights = lights;
    }

//region Cloneable
    public ABPushConfiguration clone() throws CloneNotSupportedException {
        ABPushConfiguration clone = (ABPushConfiguration)super.clone();
        clone.setSenderID(mSenderID);
        clone.setMode(mMode);
        clone.setAction(mAction);
        clone.setIconVisibility(mIconVisibility);
        clone.setIconName(mIconName);
        clone.setVibratePattern(mVibratePattern);
        clone.setSound(mSound);
        clone.setLights(mLights);
        clone.setOpenMessage(mOpenMessage);
        clone.setLaunchActivityClass(mLaunchActivityClass);
        clone.setDialogConfiguration(mDialogConfig.clone());
        return clone;
    }
//endregion
}
