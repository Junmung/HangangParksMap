package com.example.junmung.hangangparksmap;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CultureWebViewClient extends WebViewClient {
    private boolean isLoaded;
    private String keyword;

    public CultureWebViewClient(String keyword) {
        isLoaded = false;
        this.keyword = keyword;
    }

    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);
        if(isLoaded){
            webView.setVisibility(View.VISIBLE);
        }
        else {
            webView.loadUrl("javascript:(" +
                    "function($) {" +
                    "window.location.href = $(\".cnt-theme h4 a span:contains('"+keyword+"')\").parent().attr('href');" +
                    "}"+
                    ")(jQuery)");

            isLoaded = true;
        }
    }
}
