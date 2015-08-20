package com.webviewtest;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class MainActivity extends Activity {
    WebView webView;
    WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("TEST", "onPageFinished:" + url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("TEST", "onPageStarted:" + url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if (url.indexOf("test.js") > 00) {
                url = url.replace("test.js", "cache.js");
                super.onLoadResource(view, url);
            } else
                super.onLoadResource(view, url);

            Log.d("TEST", "onLoadResource:" + url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d("TEST", "onReceivedError: errorCode:" + errorCode + " ,description: " + description + ",failingUrl: " + failingUrl);
            Toast.makeText(MainActivity.this,"页面加载错误",Toast.LENGTH_LONG).show();
            onBackPressed();
        }

        /**
         * 超链接是否在 webview 打开
         * @param view
         * @param url
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("TEST", "shouldOverrideUrlLoading:" + url);
            return super.shouldOverrideUrlLoading(view, url);

        }

        /**
         * 按键事件
         * @param view
         * @param event
         * @return
         */
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            Log.d("TEST", "shouldOverrideKeyEvent");
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.d("TEST", "shouldInterceptRequest:  " + url);
            if (url.indexOf(".js") >= 0) {
                try {
                    FileInputStream fileInputStream = getAssets().openFd("cache.js").createInputStream();
                    return new WebResourceResponse("text/javascript", "UTF-8", fileInputStream);
                } catch (FileNotFoundException e) {
                    return super.shouldInterceptRequest(view, url);
                } catch (IOException e) {
                    return super.shouldInterceptRequest(view, url);

                }
            } else
                return super.shouldInterceptRequest(view, url);
        }

        /**
         * 处理 https 请求
         * @param view
         * @param handler
         * @param error
         */
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d("TEST", "onReceivedSslError");
            super.onReceivedSslError(view, handler, error);
        }


    };

    TelephonyManager tm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new WebAppInterface(this), "PingppTest");
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        webView.loadUrl("http://192.168.1.134:8080/about.html");

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        //强制清空 dom 缓存
        webView.clearCache(true);
        tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
//
//        webSettings.setAppCacheEnabled(true);
//        webSettings.setDomStorageEnabled(false);
//        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
//        webSettings.setAppCachePath(appCachePath);
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }

    @Override
    // 设置回退
    // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 模拟支付控件 js 交互
     */
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void testmodeResult(String result) {
            Log.d("TEST", result);
//            webView.evaluateJavascript("call()", new ValueCallback<String>() {
//                @Override
//                public void onReceiveValue(String s) {
//                    Log.d("TEST", s);
//                }
//            });
            webView.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("TEST", tm.getDeviceId());
                    String call = "javascript:call(\""+tm.getDeviceId()+"\")";
                    webView.loadUrl(call);

//                    webView.evaluateJavascript("call()", new ValueCallback<String>() {
//                        @Override
//                        public void onReceiveValue(String s) {
//                            Log.d("TEST",s);
//                        }
//                    });

                }
            });
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.default_anim_in, R.anim.default_anim_out);
    }
}
