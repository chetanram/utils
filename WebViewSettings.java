package com.tradvysor;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.tradvysor.Utils.ApiController;
import com.tradvysor.Utils.ApiResponseListener;
import com.tradvysor.Utils.Common;
import com.tradvysor.Utils.Constants;

import org.apache.http.impl.client.DefaultHttpClient;

import java.util.HashMap;


/**
 * Created by agc-android on 29/3/17.
 */

public class WebViewSettings extends Fragment implements ApiResponseListener {
    public String CLASS_TAG = KiteZerodhaLoginFragment.class.getSimpleName();
    DefaultHttpClient mClient = MyApplication.getClient();
    private View rootView;
    private Context context;
    private ApiController apiController;
    private HashMap<String, String> params;
    private WebView wv_kite_zerodha;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        apiController = new ApiController(context);
        apiController.apiResponseListener = (ApiResponseListener) KiteZerodhaLoginFragment.this;
        params = new HashMap<>();

    }

    @Override
    public void onResume() {
        super.onResume();
//        clearCookies(context);
    }

    @SuppressWarnings("deprecation")
    public void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
    private void enableHTML5AppCache(WebView mWebView) {

        mWebView.getSettings().setDomStorageEnabled(true);

        // Set cache size to 8 mb by default. should be more than enough
        mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

        // This next one is crazy. It's the DEFAULT location for your app's cache
        // But it didn't work for me without this line
        mWebView.getSettings().setAppCachePath("/data/data/" + getActivity().getPackageName() + "/cache");
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);

        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }
    @SuppressLint("JavascriptInterface")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.kite_zerodha_login, container, false);
        setUpToolbar();
        wv_kite_zerodha = (WebView) rootView.findViewById(R.id.wv_kite_zerodha);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(wv_kite_zerodha, true);
        // enable javascript
        wv_kite_zerodha.getSettings().setJavaScriptEnabled(true);
        wv_kite_zerodha.getSettings().setJavaScriptEnabled(true);
        wv_kite_zerodha.getSettings().setSupportMultipleWindows(true);
        wv_kite_zerodha.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv_kite_zerodha.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
        wv_kite_zerodha.getSettings().setGeolocationEnabled(true);
        wv_kite_zerodha.getSettings().setUseWideViewPort(true);
        wv_kite_zerodha.getSettings().setLoadWithOverviewMode(true);
        wv_kite_zerodha.getSettings().setAllowContentAccess(true);
        wv_kite_zerodha.getSettings().setDatabaseEnabled(true);
        wv_kite_zerodha.getSettings().setLoadsImagesAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wv_kite_zerodha.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        enableHTML5AppCache(wv_kite_zerodha);//        String newUA = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0";
        String newUA = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.63 Safari/537.31";
//        wv_kite_zerodha.getSettings().setUserAgentString(newUA);



//        Common.showProgressDialog(context);
        wv_kite_zerodha.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                super.onPageFinished(view, url);
//                Common.dismissProgressDialog();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d("", "");
//                Common.dismissProgressDialog();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
                super.onReceivedSslError(view, handler, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });

        /*wv_kite_zerodha.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //Make the bar disappear after URL is loaded, and changes string to Loading...


                // Return the app name after finish loading
                if (progress == 100) {
                    Common.dismissProgressDialog();
                }
            }
        });*/
//
        wv_kite_zerodha.loadUrl("your url");
        return rootView;
    }

    private void setUpToolbar() {

        Toolbar toolbar = (Toolbar) ((MainActivity) context).findViewById(R.id.toolbar);
        RelativeLayout rl_header_main = (RelativeLayout) toolbar.findViewById(R.id.rl_header_main);
        ImageView iv_menu_icon = (ImageView) toolbar.findViewById(R.id.iv_menu_icon);
        ImageView iv_back = (ImageView) toolbar.findViewById(R.id.iv_back);
        iv_menu_icon.setVisibility(View.VISIBLE);
        iv_back.setVisibility(View.GONE);
        TextView tv_toolbar_title = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        ImageView iv_search_icon = (ImageView) toolbar.findViewById(R.id.iv_search_icon);

        RelativeLayout rl_search = (RelativeLayout) toolbar.findViewById(R.id.rl_search);
        SearchView searchVew = (SearchView) toolbar.findViewById(R.id.searchVew);
        ImageView iv_header_back = (ImageView) toolbar.findViewById(R.id.iv_header_back);
        tv_toolbar_title.setText(getResources().getString(R.string.login_with_zerodha));
        rl_header_main.setVisibility(View.VISIBLE);
        rl_search.setVisibility(View.GONE);
        iv_search_icon.setVisibility(View.GONE);
    }

    @Override
    public void onSuccessResponse(String response, HashMap<String, String> hashMap) {
        try {
            if (hashMap.get(Constants.PARAM_ACTION) != null && hashMap.get(Constants.PARAM_ACTION).equalsIgnoreCase(Constants.AC_FAQ)) {
                if (response != null) {

                }

            }
        } catch (Exception e) {

        }


    }

    @Override
    public void onErrorResponse(VolleyError error, HashMap<String, String> hashMap) {
        Common.dismissProgressDialog();
    }
}
