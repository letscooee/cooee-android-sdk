package com.letscooee.trigger.inapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.letscooee.R;
import com.letscooee.models.trigger.blocks.BrowserContent;
import com.letscooee.utils.Constants;

public class InAppBrowserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_browser);

        if (getIntent().getExtras() == null) {
            finish();
        }

        BrowserContent browserContent = getIntent().getExtras().getParcelable(Constants.INTENT_BUNDLE_KEY);
        if (browserContent == null) {
            finish();
        }

        assert browserContent != null;
        if (!browserContent.isShowAB()) {
            findViewById(R.id.linearLayout).setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.textViewIABUrl)).setText(browserContent.getUrl());
        findViewById(R.id.imageViewCloseIAB).setOnClickListener(v -> finish());

        WebView webView = findViewById(R.id.webViewIAB);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setTranslationZ(5);
        webView.setId(R.id.web_view);
        webView.loadUrl(browserContent.getUrl());
    }
}