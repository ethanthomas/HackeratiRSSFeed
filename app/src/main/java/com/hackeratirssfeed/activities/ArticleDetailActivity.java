package com.hackeratirssfeed.activities;

/**
 * Created by user on 6/23/15.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.hackeratirssfeed.R;
import com.hackeratirssfeed.objects.Article;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ArticleDetailActivity extends AppCompatActivity {

    static WebView webView;
    String type;
    int position;

    ArrayList<Article> articles = null;
    static Article article = null;

    ProgressWheel progressWheel;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        progressWheel = (ProgressWheel) findViewById(R.id.progress);
        progressWheel.spin();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        type = intent.getStringExtra("articleType");

        if (type.equals("gear"))
            articles = Article.GearArticles;
        else if (type.equals("design"))
            articles = Article.DesignArticles;
        else if (type.equals("science"))
            articles = Article.ScienceArticles;
        else if (type.equals("business"))
            articles = Article.BusinessArticles;
        else if (type.equals("security"))
            articles = Article.SecurityArticles;
        else if (type.equals("entertainment"))
            articles = Article.EntertainmentArticles;
        article = articles.get(position);

        webView = (WebView) findViewById(R.id.webView);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

//        CollapsingToolbarLayout collapsingToolbar =
//                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbar.setTitle(article.getTitle());

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (!article.isFavorite())
            fab.setImageResource(R.mipmap.favorite_border);
        else
            fab.setImageResource(R.mipmap.favorite);

        fab.setBackgroundTintList(ColorStateList.valueOf(articles.get(position).getVibrantDark()));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (articles.get(position).isFavorite()) {
                    fab.setImageResource(R.mipmap.favorite_border);
                    articles.get(position).setFavorite(false);

                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove(articles.get(position).getTitle()).commit();
                } else if (!articles.get(position).isFavorite()) {
                    fab.setImageResource(R.mipmap.favorite);
                    articles.get(position).setFavorite(true);

                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(articles.get(position).getTitle(), "").commit();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(article.getVibrantDark());

        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                webView.setVisibility(View.VISIBLE);
                progressWheel.stopSpinning();
            }
        });
        webView.loadUrl(article.getLink());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

//        background.setBackgroundColor(article.getVibrantLight());

        loadBackdrop();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this article from WIRED");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, article.getTitle() + ": \n" + article.getLink());
                startActivity(Intent.createChooser(sharingIntent, "Share this article"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Picasso.with(this).load(article.getImageURL()).into(imageView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public int calculateHeightInDp(int height) {
        final float scale = getResources().getDisplayMetrics().density;
        int hdp = (int) (height * scale + 0.5f);
        return hdp;
    }

    public boolean isTablet() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
