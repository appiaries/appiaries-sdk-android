package com.appiaries.baas.sdk;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

import java.io.Serializable;

/**
 * プッシュ通知ダイアログ設定。
 * <p>プッシュ通知ダイアログに関する設定を保持します。</p>
 */
public class ABPushDialogConfiguration implements Serializable, Cloneable {

    private static final long serialVersionUID = -412107491117963513L;
    private AB.PushDialogStyle mStyle;
    private String mTitle;
    private String mMessage;

    private String mPositiveButtonText;
    private String mNegativeButtonText;
    private View.OnClickListener mPositiveButtonOnClickListener;
    private View.OnClickListener mNegativeButtonOnClickListener;

    private int mBaseViewBackgroundColor;
    private int[] mBaseViewBackgroundGradientColors;
    private GradientDrawable.Orientation mBaseViewBackgroundGradientOrientation;
    private int mBaseViewBorderColor;
    private int mBaseViewBorderWidth;
    private int mBaseViewForegroundColor;
    private float mBaseViewFontSize;
    private float mBaseViewCornerRadius;

    private int mTitleViewBackgroundColor;
    private int[] mTitleViewBackgroundGradientColors;
    private GradientDrawable.Orientation mTitleViewBackgroundGradientOrientation;
    private int mTitleViewBorderColor;
    private int mTitleViewBorderWidth;
    private int mTitleViewForegroundColor;
    private float mTitleViewFontSize;
    private float mTitleViewCornerRadius;

    private int mButtonsViewBackgroundColor;
    private int[] mButtonsViewBackgroundGradientColors;
    private GradientDrawable.Orientation mButtonsViewBackgroundGradientOrientation;
    private int mButtonsViewBorderColor;
    private int mButtonsViewBorderWidth;
    private int mButtonsViewForegroundColor;
    private float mButtonsViewFontSize;
    private float mButtonsViewCornerRadius;

    public static final AB.PushDialogStyle DEFAULT_STYLE = AB.PushDialogStyle.SIMPLE;



    public ABPushDialogConfiguration() {
        this(DEFAULT_STYLE);
    }
    public ABPushDialogConfiguration(AB.PushDialogStyle style) {
        mStyle = style;


        switch (style) {
            case POP:
                //Base View
                mBaseViewBackgroundColor = 0xffffffff;
                mBaseViewBackgroundGradientColors = null;
                mBaseViewBackgroundGradientOrientation = null;
                mBaseViewCornerRadius = 12.0f;
                mBaseViewBorderColor = 0xffffffff;
                mBaseViewBorderWidth = 2;
                mBaseViewForegroundColor = 0xff3e3a39;
                mBaseViewFontSize = 15.0f;
                //Title View
                mTitleViewBackgroundColor = 0xffec6866;
                mTitleViewBackgroundGradientColors = null;
                mTitleViewBackgroundGradientOrientation = null;
                mTitleViewCornerRadius = 12.0f;
                mTitleViewBorderColor = 0xffec6866;
                mTitleViewBorderWidth = 2;
                mTitleViewForegroundColor = 0xffffffff;
                mTitleViewFontSize = 17.0f;
                //Buttons View
                mButtonsViewBackgroundColor = 0xffe2d6c7;
                mButtonsViewBackgroundGradientColors = new int[]{0xffffffff, 0xfff5f5f5};
                mButtonsViewBackgroundGradientOrientation = GradientDrawable.Orientation.BOTTOM_TOP;
                mButtonsViewCornerRadius = 12.0f;
                mButtonsViewBorderColor = 0xffe2d6c7;
                mButtonsViewBorderWidth = 2;
                mButtonsViewForegroundColor = 0xff3e3a39;
                mButtonsViewFontSize = 14.0f;
                //MISC
                mPositiveButtonText = "OK";
                mNegativeButtonText = "Cancel";
                break;
            case FLAT:
                //Base Layer
                //mBaseViewBackgroundColor   = 0xffffffff; //no set
                mBaseViewBackgroundColor   = 0xffdcdddd;
                mBaseViewBackgroundGradientColors = new int[]{0xffdcdddd, 0xfff7f8f8};
                mBaseViewBackgroundGradientOrientation = GradientDrawable.Orientation.BOTTOM_TOP;
                mBaseViewCornerRadius = 4.0f;
                mBaseViewBorderColor = 0xff9fa0a0;
                mBaseViewBorderWidth = 1;
                mBaseViewForegroundColor = 0xff3e3a39;
                mBaseViewFontSize = 15.0f;
                //Title layer
                //mTitleViewBackgroundColor  = 0xffffffff; //no set
                mTitleViewCornerRadius = 4.0f;
                mTitleViewBackgroundGradientColors = null;
                mTitleViewBackgroundGradientOrientation = null;
                mTitleViewCornerRadius = 2.0f;
                mTitleViewBorderColor = 0xffdcdddd;
                mTitleViewBorderWidth = 1;
                mTitleViewForegroundColor = 0xff3e3a39;
                mTitleViewFontSize = 17.0f;
                //Button Layer
                mButtonsViewBackgroundColor = 0xffdcdddd;
                mButtonsViewBackgroundGradientColors = null;
                mButtonsViewBackgroundGradientOrientation = null;
                mButtonsViewCornerRadius = 1.0f;
                mButtonsViewBorderColor = 0xff9fa0a0;
                mButtonsViewBorderWidth = 1;
                mButtonsViewForegroundColor = 0xff3e3a39;
                mButtonsViewFontSize = 14.0f;
                //MISC
                mPositiveButtonText = "OK";
                mNegativeButtonText = "Cancel";
                break;
            case COOL:
                //Base Layer
                mBaseViewBackgroundColor = 0xffffffff;
                mBaseViewBackgroundGradientColors = null;
                mBaseViewBackgroundGradientOrientation = null;
                mBaseViewCornerRadius = 2.0f;
                mBaseViewBorderColor = 0xffffffff;
                mBaseViewBorderWidth = 2;
                mBaseViewForegroundColor = 0xff3e3a39;
                mBaseViewFontSize = 15.0f;
                //Title layer
                mTitleViewBackgroundColor = 0xff444d6d;
                mTitleViewBackgroundGradientColors = null;
                mTitleViewBackgroundGradientOrientation = null;
                mTitleViewCornerRadius = 2.0f;
                mTitleViewBorderColor = 0xff444d6d;
                mTitleViewBorderWidth = 2;
                mTitleViewForegroundColor = 0xffffffff;
                mTitleViewFontSize = 17.0f;
                //Button Layer
                mButtonsViewBackgroundColor = 0xff7095c1;
                mButtonsViewBackgroundGradientColors = null;
                mButtonsViewBackgroundGradientOrientation = null;
                mButtonsViewCornerRadius = 2.0f;
                mButtonsViewBorderColor = 0xff7095c1;
                mButtonsViewBorderWidth = 2;
                mButtonsViewForegroundColor = 0xffffffff;
                mButtonsViewFontSize = 14.0f;
                //MISC
                mPositiveButtonText = "OK";
                mNegativeButtonText = "Cancel";
                break;
            case SIMPLE:
            default:
                //Base Layer
                mBaseViewBackgroundColor = 0xffefefef;
                mBaseViewBackgroundGradientColors = null;
                mBaseViewBackgroundGradientOrientation = null;
                mBaseViewCornerRadius = 2.0f;
                mBaseViewBorderColor = 0xffffffff;
                mBaseViewBorderWidth = 2;
                mBaseViewForegroundColor = 0xff3e3a39;
                mBaseViewFontSize = 15.0f;
                //Title layer
                mTitleViewBackgroundColor = 0xff3e3a39;
                mTitleViewBackgroundGradientColors = null;
                mTitleViewBackgroundGradientOrientation = null;
                mTitleViewCornerRadius = 2.0f;
                mTitleViewBorderColor = 0xff3e3a39;
                mTitleViewBorderWidth = 2;
                mTitleViewForegroundColor = 0xffffffff;
                mTitleViewFontSize = 17.0f;
                //Button Layer
                //mButtonsViewBackgroundColor = 0xffffffff; //no set
                mButtonsViewBackgroundGradientColors = new int[]{0xffffffff, 0xfff5f5f5};
                mButtonsViewBackgroundGradientOrientation = GradientDrawable.Orientation.BOTTOM_TOP;
                mButtonsViewCornerRadius = 2.0f;
                mButtonsViewBorderColor = 0xff3e3a39;
                mButtonsViewBorderWidth = 2;
                mButtonsViewForegroundColor = 0xff231815;
                mButtonsViewFontSize = 14.0f;
                //MISC
                mPositiveButtonText = "OK";
                mNegativeButtonText = "Cancel";
                break;
        }
    }


    public AB.PushDialogStyle getStyle() {
        return mStyle;
    }

    public void setStyle(AB.PushDialogStyle style) {
        this.mStyle = style;
    }

    public String getTitle() {
        return mTitle;
    }

    void setTitle(String title) {
        this.mTitle = title;
    }

    public String getMessage() {
        return mMessage;
    }

    void setMessage(String message) {
        this.mMessage = message;
    }

    public String getPositiveButtonText() {
        return mPositiveButtonText;
    }

    public void setPositiveButtonText(String text) {
        this.mPositiveButtonText = text;
    }

    public View.OnClickListener getPositiveButtonOnClickListener() {
        return this.mPositiveButtonOnClickListener;
    }

    public void setPositiveButtonOnClickListener(View.OnClickListener listener) {
        this.mPositiveButtonOnClickListener = listener;
    }

    public String getNegativeButtonText() {
        return mNegativeButtonText;
    }

    public void setNegativeButtonText(String text) {
        this.mNegativeButtonText = text;
    }

    public View.OnClickListener getNegativeButtonOnClickListener() {
        return this.mNegativeButtonOnClickListener;
    }

    public void setNegativeButtonOnClickListener(View.OnClickListener listener) {
        this.mNegativeButtonOnClickListener = listener;
    }

    public int getBaseViewBackgroundColor() {
        return mBaseViewBackgroundColor;
    }

    public void setBaseViewBackgroundColor(int color) {
        this.mBaseViewBackgroundColor = color;
    }
    public int[] getBaseViewBackgroundGradientColors() {
        return mBaseViewBackgroundGradientColors;
    }

    public void setBaseViewBackgroundGradientColors(int[] colors) {
        this.mBaseViewBackgroundGradientColors = colors;
    }
    public GradientDrawable.Orientation getBaseViewBackgroundGradientOrientation() {
        return mBaseViewBackgroundGradientOrientation;
    }

    public void setBaseViewBackgroundGradientOrientation(GradientDrawable.Orientation orientation) {
        this.mBaseViewBackgroundGradientOrientation = orientation;
    }

    public int getBaseViewBorderColor() {
        return mBaseViewBorderColor;
    }

    public void setBaseViewBorderColor(int color) {
        this.mBaseViewBorderColor = color;
    }

    public int getBaseViewBorderWidth() {
        return mBaseViewBorderWidth;
    }

    public void setBaseViewBorderWidth(int width) {
        this.mBaseViewBorderWidth = width;
    }

    public int getBaseViewForegroundColor() {
        return mBaseViewForegroundColor;
    }

    public void setBaseViewForegroundColor(int color) {
        this.mBaseViewForegroundColor = color;
    }

    public float getBaseViewFontSize() {
        return mBaseViewFontSize;
    }

    public void setBaseViewFontSize(float size) {
        this.mBaseViewFontSize = size;
    }

    public float getBaseViewCornerRadius() {
        return mBaseViewCornerRadius;
    }

    public void setBaseViewCornerRadius(float radius) {
        this.mBaseViewCornerRadius = radius;
    }

    public int getTitleViewBackgroundColor() {
        return mTitleViewBackgroundColor;
    }

    public void setTitleViewBackgroundColor(int color) {
        this.mTitleViewBackgroundColor = color;
    }

    public int[] getTitleViewBackgroundGradientColors() {
        return mTitleViewBackgroundGradientColors;
    }

    public void setTitleViewBackgroundGradientColors(int[] colors) {
        this.mTitleViewBackgroundGradientColors = colors;
    }

    public GradientDrawable.Orientation getTitleViewBackgroundGradientOrientation() {
        return mTitleViewBackgroundGradientOrientation;
    }

    public void setTitleViewBackgroundGradientOrientation(GradientDrawable.Orientation orientation) {
        this.mTitleViewBackgroundGradientOrientation = orientation;
    }

    public int getTitleViewBorderColor() {
        return mTitleViewBorderColor;
    }

    public void setTitleViewBorderColor(int color) {
        this.mTitleViewBorderColor = color;
    }

    public int getTitleViewBorderWidth() {
        return mTitleViewBorderWidth;
    }

    public void setTitleViewBorderWidth(int width) {
        this.mTitleViewBorderWidth = width;
    }

    public int getTitleViewForegroundColor() {
        return mTitleViewForegroundColor;
    }

    public void setTitleViewForegroundColor(int color) {
        this.mTitleViewForegroundColor = color;
    }

    public float getTitleViewFontSize() {
        return mTitleViewFontSize;
    }

    public void setTitleViewFontSize(float size) {
        this.mTitleViewFontSize = size;
    }

    public float getTitleViewCornerRadius() {
        return mTitleViewCornerRadius;
    }

    public void setTitleViewCornerRadius(float radius) {
        this.mTitleViewCornerRadius = radius;
    }

    public int getButtonsViewBackgroundColor() {
        return mButtonsViewBackgroundColor;
    }

    public void setButtonsViewBackgroundColor(int color) {
        this.mButtonsViewBackgroundColor = color;
    }

    public int[] getButtonsViewBackgroundGradientColors() {
        return mButtonsViewBackgroundGradientColors;
    }

    public void setButtonsViewBackgroundGradientColors(int[] colors) {
        this.mButtonsViewBackgroundGradientColors = colors;
    }

    public GradientDrawable.Orientation getButtonsViewBackgroundGradientOrientation() {
        return mButtonsViewBackgroundGradientOrientation;
    }

    public void setButtonsViewBackgroundGradientOrientation(GradientDrawable.Orientation orientation) {
        this.mButtonsViewBackgroundGradientOrientation = orientation;
    }

    public int getButtonsViewBorderColor() {
        return mButtonsViewBorderColor;
    }

    public void setButtonsViewBorderColor(int color) {
        this.mButtonsViewBorderColor = color;
    }

    public int getButtonsViewBorderWidth() {
        return mButtonsViewBorderWidth;
    }

    public void setButtonsViewBorderWidth(int width) {
        this.mButtonsViewBorderWidth = width;
    }

    public int getButtonsViewForegroundColor() {
        return mButtonsViewForegroundColor;
    }

    public void setButtonsViewForegroundColor(int color) {
        this.mButtonsViewForegroundColor = color;
    }

    public float getButtonsViewFontSize() {
        return mButtonsViewFontSize;
    }

    public void setButtonsViewFontSize(float size) {
        this.mButtonsViewFontSize = size;
    }

    public float getButtonsViewCornerRadius() {
        return mButtonsViewCornerRadius;
    }

    public void setButtonsViewCornerRadius(float radius) {
        this.mButtonsViewCornerRadius = radius;
    }

    //region Cloneable
    public ABPushDialogConfiguration clone() throws CloneNotSupportedException {
        ABPushDialogConfiguration clone = (ABPushDialogConfiguration)super.clone();
        clone.setStyle(mStyle);
        clone.setTitle(mTitle);
        clone.setMessage(mMessage);
        clone.setPositiveButtonText(mPositiveButtonText);
        clone.setNegativeButtonText(mNegativeButtonText);
        clone.setBaseViewBackgroundColor(mBaseViewBackgroundColor);
        clone.setBaseViewBackgroundGradientColors(mBaseViewBackgroundGradientColors);
        clone.setBaseViewBackgroundGradientOrientation(mBaseViewBackgroundGradientOrientation);
        clone.setBaseViewBorderColor(mBaseViewBorderColor);
        clone.setBaseViewBorderWidth(mBaseViewBorderWidth);
        clone.setBaseViewForegroundColor(mBaseViewForegroundColor);
        clone.setBaseViewFontSize(mBaseViewFontSize);
        clone.setBaseViewCornerRadius(mBaseViewCornerRadius);
        clone.setTitleViewBackgroundColor(mTitleViewBackgroundColor);
        clone.setTitleViewBackgroundGradientColors(mTitleViewBackgroundGradientColors);
        clone.setTitleViewBackgroundGradientOrientation(mTitleViewBackgroundGradientOrientation);
        clone.setTitleViewBorderColor(mTitleViewBorderColor);
        clone.setTitleViewBorderWidth(mTitleViewBorderWidth);
        clone.setTitleViewForegroundColor(mTitleViewForegroundColor);
        clone.setTitleViewFontSize(mTitleViewFontSize);
        clone.setTitleViewCornerRadius(mTitleViewCornerRadius);
        clone.setButtonsViewBackgroundColor(mButtonsViewBackgroundColor);
        clone.setButtonsViewBackgroundGradientColors(mButtonsViewBackgroundGradientColors);
        clone.setButtonsViewBackgroundGradientOrientation(mButtonsViewBackgroundGradientOrientation);
        clone.setButtonsViewBorderColor(mButtonsViewBorderColor);
        clone.setButtonsViewBorderWidth(mButtonsViewBorderWidth);
        clone.setButtonsViewForegroundColor(mButtonsViewForegroundColor);
        clone.setButtonsViewFontSize(mButtonsViewFontSize);
        clone.setButtonsViewCornerRadius(mButtonsViewCornerRadius);
        return clone;
    }
//endregion
}
