package com.example.junmung.hangangparksmap;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.daum.mf.map.api.MapPOIItem;

public class CultureWebViewClient extends WebViewClient {
    private boolean isLoaded, isHtmlClear;
    private String keyword;
    private MapPOIItem item;

    public CultureWebViewClient(MapPOIItem item, String keyword) {
        isLoaded = false;
        this.keyword = keyword;
        this.item = item;
        isHtmlClear = false;
    }

    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);
        if(isLoaded){
            webView.setVisibility(View.VISIBLE);
            CulturePoint point = (CulturePoint)item.getUserObject();
            point.setUrl(url);
            item.setUserObject(point);
            isHtmlClear = true;
            webView.loadUrl("javascript:(" +
                    "function($) {" +
                    "$('#wrapper').children().not('#container').remove();" +
                    "}" +
                    ")(jQuery)");
            Log.d("isLoaded", "들어옴");

        } else if (isHtmlClear) {

        } else {
            webView.loadUrl("javascript:(" +
                    "function($) {" +
                    "window.location.href = $(\".cnt-theme h4 a span:contains('" + keyword + "')\").parent().attr('href');" +
                    "}" +
                    ")(jQuery)");

            isLoaded = true;
        }
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return true;
    }
}
