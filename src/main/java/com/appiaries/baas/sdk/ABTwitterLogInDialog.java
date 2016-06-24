package com.appiaries.baas.sdk;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


class ABTwitterLogInDialog extends Dialog {

    private static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private final String callbackUrl;
    private final String requestUrl;
    private final String serviceUrlIdentifier;
    private final FlowResultHandler handler;
    private ProgressDialog progressDialog;
    private ImageView closeImage;
    private LinearLayout webBackView;
    private FrameLayout content;

    public ABTwitterLogInDialog(Context context, String requestUrl, String callbackUrl, String serviceUrlIdentifier, FlowResultHandler resultHandler) {
        super(context, android.R.style.Theme_Holo_Wallpaper_NoTitleBar);
        //super(context, android.R.style.Theme_Translucent_NoTitleBar);
        //super(context, android.R.style.Theme_Dialog);
        this.requestUrl = requestUrl;
        this.callbackUrl = callbackUrl;
        this.serviceUrlIdentifier = serviceUrlIdentifier;
        this.handler = resultHandler;

        setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                ABTwitterLogInDialog.this.handler.onCancel();
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.progressDialog = new ProgressDialog(getContext());
        this.progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.progressDialog.setMessage("Loading...");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.content = new FrameLayout(getContext());

        createCloseImage();

        int webViewMargin = this.closeImage.getDrawable().getIntrinsicWidth() / 2;
        setUpWebView(webViewMargin);

        this.content.addView(this.closeImage, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addContentView(this.content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void createCloseImage() {
        this.closeImage = new ImageView(getContext());

        this.closeImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ABTwitterLogInDialog.this.cancel();
            }
        });
        Drawable closeDrawable = getContext().getResources().getDrawable(android.R.drawable.btn_dialog);
//        Drawable closeDrawable = getContext().getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
//        Drawable closeDrawable = getContext().getResources().getDrawable(android.R.drawable.ic_notification_clear_all);

        this.closeImage.setImageDrawable(closeDrawable);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        int width = disp.getWidth();
        int closeImageWidth = this.closeImage.getDrawable().getIntrinsicWidth();

//        this.closeImage.setPadding(width - closeImageWidth, 0, 0, 0);
        this.closeImage.setPadding(0, 0, 0, 0);
        this.closeImage.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView(int margin) {
        this.webBackView = new LinearLayout(getContext());
        LinearLayout webViewContainer = new LinearLayout(getContext());

        WebView webView = new WebView(getContext());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new OAuth1WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(this.requestUrl);
        webView.setLayoutParams(FILL);

        this.webBackView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.webBackView.setBackgroundColor(Color.DKGRAY);
        this.webBackView.setPadding(3, 3, 3, 3);
        this.webBackView.addView(webView);
        this.webBackView.setVisibility(View.INVISIBLE);
        webViewContainer.setPadding(margin, margin, margin, margin);
        webViewContainer.addView(this.webBackView);
        this.content.addView(webViewContainer);
    }


    public static abstract interface FlowResultHandler {

        public abstract void onCancel();

        public abstract void onError(int paramInt, String paramString1, String paramString2);

        public abstract void onComplete(String paramString);
    }


    private class OAuth1WebViewClient extends WebViewClient {

        private OAuth1WebViewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(ABTwitterLogInDialog.this.callbackUrl)) {
                ABTwitterLogInDialog.this.dismiss();
                ABTwitterLogInDialog.this.handler.onComplete(url);
                return true;
            }
            if (url.contains(ABTwitterLogInDialog.this.serviceUrlIdentifier)) {
                return false;
            }

            ABTwitterLogInDialog.this.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            ABTwitterLogInDialog.this.dismiss();
            ABTwitterLogInDialog.this.handler.onError(errorCode, description, failingUrl);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            ABTwitterLogInDialog.this.progressDialog.show();
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            try {
                ABTwitterLogInDialog.this.progressDialog.dismiss();
            } catch (IllegalArgumentException ignored) {
            }

            ABTwitterLogInDialog.this.content.setBackgroundColor(0);
            ABTwitterLogInDialog.this.webBackView.setVisibility(View.VISIBLE);
            ABTwitterLogInDialog.this.closeImage.setVisibility(View.VISIBLE);
        }

    }

}
