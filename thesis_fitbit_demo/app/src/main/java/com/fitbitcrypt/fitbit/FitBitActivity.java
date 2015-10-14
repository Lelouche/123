package com.fitbitcrypt.fitbit;

import java.util.Map;
import java.util.Properties;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fitbitcrypt.AppUtil;
import com.fitbitcrypt.Constants;
import com.fitbitcrypt.R;

public class FitBitActivity extends Activity {
    private final String TAG = FitBitActivity.this.getClass().getSimpleName();
    private WebView webView;
    private ProgressDialog progressBar;
    private String response_type, client_id, redirect_uri, scope, expires_in;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fitbit_activity);

        loadProperties();
        initWebView();
        initProgressBar();
    }

    private void loadProperties() {
        Properties properties = AppUtil.getProperties("fitbit_cred.properties", getAssets());
        response_type = properties.getProperty("response_type");
        client_id = properties.getProperty("client_id");
        redirect_uri = properties.getProperty("redirect_uri");
        scope = properties.getProperty("scope");
        expires_in = properties.getProperty("expires_in");
    }

    private void initWebView() {
        webView = (WebView) findViewById(R.id.webview1);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.getUrl();
                Log.d(TAG, "Final Whole Response " + view.getUrl());
                Log.d(TAG, "check url " + view.getUrl().substring(11, 18));

                if (progressBar.isShowing()) progressBar.dismiss();
                Map<String, String> map = FitbitUtil.getQueryMap(view.getUrl());
                // if the URL directed to does not contain the access_token
                // (login for first time, error loggin in, changed Fitbit website,etc.)
                if (!map.containsKey("access_token")) return;
                // finish the FitBit Activity with the result `access_token`
                Intent returnIntent = new Intent();
                returnIntent.putExtra("data", map.get("access_token"));
                setResult(Constants.RESULT_OK, returnIntent);
                finish();
            }
        });

        webView.loadUrl(FitbitUtil.getAuthorizeUrl(response_type, client_id, redirect_uri, scope, expires_in));
        webView.requestFocus(View.FOCUS_DOWN);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!arg0.hasFocus()) arg0.requestFocus();
                        break;
                }
                return false;
            }
        });
    }

    private void initProgressBar() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Loading...");
        progressBar.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}