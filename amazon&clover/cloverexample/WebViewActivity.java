package com.texasbrokers.screensaver;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.texasbrokers.screensaver.R;
import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.PrefUtils;

import java.io.File;

/**
 * Created by chetan on 8/8/17.
 */

public class WebViewActivity extends Activity {
    private Context context;
    private WebView webView;
    private boolean IS_DROP_BOX = false;
    private Toolbar toolBar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        context = WebViewActivity.this;

        toolBar = (Toolbar) findViewById(R.id.my_toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        setUpToolbar();
        webView = (WebView) findViewById(R.id.webView);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            IS_DROP_BOX = bundle.getBoolean(Constants.IS_DROP_BOX, false);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyClient());
        webView.setWebChromeClient(new GoogleClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.clearCache(true);
        webView.clearHistory();

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));


                request.setMimeType(mimetype);


                String cookies = CookieManager.getInstance().getCookie(url);


                request.addRequestHeader("cookie", cookies);


                request.addRequestHeader("User-Agent", userAgent);


                request.setDescription("Downloading file...");


                request.setTitle(URLUtil.guessFileName(url, contentDisposition,
                        mimetype));


                request.allowScanningByMediaScanner();


                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                File file = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS);
                if (!file.exists()) {
                    file.mkdir();
                }
                String contentSplit[] = contentDisposition.split("filename=");
                String filename = contentSplit[1].replace("filename=", "").replace("\"", "").trim();
                filename = filename.split(";")[0];
                request.setDestinationUri(Uri.fromFile(new File(file.toString(), filename)));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File",
                        Toast.LENGTH_LONG).show();
            }
        });
//        String newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
//        String newUA = "Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30";
        String newUA = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0";

        if (IS_DROP_BOX) {
            webView.getSettings().setUserAgentString(newUA);
            webView.loadUrl("https://www.dropbox.com/login");
        } else {
            webView.loadUrl("https://accounts.google.com/signin/v2/identifier?service=wise&passive=true&continue=http%3A%2F%2Fdrive.google.com%2F%3Futm_source%3Den_US&utm_medium=button&utm_campaign=web&utm_content=gotodrive&usp=gtd&ltmpl=drive&urp=https%3A%2F%2Fwww.google.co.in%2F&flowName=GlifWebSignIn&flowEntry=ServiceLogin");
        }
    }

    private void setUpToolbar() {
        TextView tv_back_app = (TextView) toolBar.findViewById(R.id.tv_back_app);
        tv_back_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, true);
        PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false);
        PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
    }

    class MyClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String Url) {
            view.loadUrl(Url);
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            resend.sendToTarget();
        }
    }

    class GoogleClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setVisibility(View.VISIBLE);
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            }

        }
    }
}
