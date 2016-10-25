package com.shakiemsaunders.nytimessearch.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shakiemsaunders.nytimessearch.Models.NewsArticle;
import com.shakiemsaunders.nytimessearch.R;
import com.shakiemsaunders.nytimessearch.Utils.AppConstants;

import org.parceler.Parcels;

public class DetailedArticleActivity extends AppCompatActivity {

    private WebView webView;
    private NewsArticle article;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_article);

        Toolbar toolbar = (Toolbar)findViewById(R.id.articleDetailToolbar);
        setSupportActionBar(toolbar);

        webView = (WebView) findViewById(R.id.articleWebView);
        article = (NewsArticle) Parcels.unwrap(getIntent().getParcelableExtra(AppConstants.ARTICLE_KEY));

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(article.getWebUrl());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detailed_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, article.getWebUrl());
            startActivity(Intent.createChooser(shareIntent, "Share link using"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
