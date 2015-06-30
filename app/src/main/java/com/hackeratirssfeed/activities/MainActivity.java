package com.hackeratirssfeed.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hackeratirssfeed.R;
import com.hackeratirssfeed.fragments.BusinessFragment;
import com.hackeratirssfeed.fragments.DesignFragment;
import com.hackeratirssfeed.fragments.EntertainmentFragment;
import com.hackeratirssfeed.fragments.GearFragment;
import com.hackeratirssfeed.fragments.ScienceFragment;
import com.hackeratirssfeed.fragments.SecurityFragment;
import com.hackeratirssfeed.objects.Article;
import com.hackeratirssfeed.utilities.CheckNetwork;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static Toolbar toolbar;
    ViewPager viewPager;
    static FloatingActionButton fab;
    TabLayout tabLayout;

    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    static int pos;

    //--- Tablet Layout Variables ---
    static ImageView background;
    static WebView webView;
    static ProgressWheel progressWheel;

    static ArrayList<Article> articles;
    static Article currentArticle;
    public static LinearLayout noInternet;
    SwipeRefreshLayout swipeRefreshLayout;
    Adapter adapter = new Adapter(getSupportFragmentManager());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getResources().getBoolean(R.bool.portrait))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        initializeViews();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
    }

    public void initializeViews() {

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (isTablet()) {
            noInternet = (LinearLayout) findViewById(R.id.noInternet);
            webView = (WebView) findViewById(R.id.webView);
            webView.clearCache(true);
            webView.clearFormData();
            webView.clearHistory();
            webView.clearSslPreferences();
            background = (ImageView) findViewById(R.id.backdrop);
            progressWheel = (ProgressWheel) findViewById(R.id.progress);
            progressWheel.spin();
            fab = (FloatingActionButton) findViewById(R.id.fab);
            if (CheckNetwork.isInternetAvailable(getApplicationContext())) {
                webView.setWebViewClient(new WebViewClient() {

                    public void onPageFinished(WebView view, String url) {
                        webView.setVisibility(View.VISIBLE);
                        progressWheel.stopSpinning();
                    }
                });
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.getSettings().setDisplayZoomControls(false);
            } else {
                noInternet.setVisibility(View.VISIBLE);
                progressWheel.stopSpinning();
            }
            setupToolbar();
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (CheckNetwork.isInternetAvailable(getApplicationContext())) {
                        noInternet.setVisibility(View.INVISIBLE);
                        progressWheel.spin();
                        swipeRefreshLayout.setEnabled(false);
                        adapter.clear();
                        setupViewPager(viewPager);
                        webView.setWebViewClient(new WebViewClient() {

                            public void onPageFinished(WebView view, String url) {
                                webView.setVisibility(View.VISIBLE);
                                progressWheel.stopSpinning();
                            }
                        });
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
    }

    public static void loadArticle(final int position, final String type) {

        if (type.equals("Gear"))
            articles = Article.GearArticles;
        else if (type.equals("Design"))
            articles = Article.DesignArticles;
        else if (type.equals("Science"))
            articles = Article.ScienceArticles;
        else if (type.equals("Business"))
            articles = Article.BusinessArticles;
        else if (type.equals("Security"))
            articles = Article.SecurityArticles;
        else if (type.equals("Entertainment"))
            articles = Article.EntertainmentArticles;
        currentArticle = articles.get(position);

        pos = position;
        webView.loadUrl(articles.get(pos).getLink());
        webView.setVisibility(View.INVISIBLE);
        progressWheel.spin();
        Picasso.with(background.getContext()).load(articles.get(pos).getImageURL()).into(background);
        toolbar.setTitle(type);

        if (currentArticle.isFavorite())
            fab.setImageResource(R.mipmap.favorite);
        else
            fab.setImageResource(R.mipmap.favorite_border);

        if (currentArticle.getVibrantDark() != 0x000000)
            fab.setBackgroundTintList(ColorStateList.valueOf(currentArticle.getVibrantDark()));
        else
            fab.setBackgroundTintList(ColorStateList.valueOf(currentArticle.getMutedDark()));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (articles.get(pos).isFavorite()) {
                    fab.setImageResource(R.mipmap.favorite_border);

                    articles.get(pos).setFavorite(false);

                    editor.remove(articles.get(pos).getTitle());
                    editor.commit();

                    if (type.equals("Gear"))
                        GearFragment.update(position);
                    else if (type.equals("Design"))
                        DesignFragment.update(position);
                    else if (type.equals("Science"))
                        ScienceFragment.update(position);
                    else if (type.equals("Business"))
                        BusinessFragment.update(position);
                    else if (type.equals("Security"))
                        SecurityFragment.update(position);
                    else if (type.equals("Entertainment"))
                        EntertainmentFragment.update(position);

                } else if (!articles.get(pos).isFavorite()) {
                    fab.setImageResource(R.mipmap.favorite);
                    articles.get(pos).setFavorite(true);
                    editor.putString(articles.get(pos).getTitle(), "");
                    editor.commit();

                    if (type.equals("Gear"))
                        GearFragment.update(position);
                    else if (type.equals("Design"))
                        DesignFragment.update(position);
                    else if (type.equals("Science"))
                        ScienceFragment.update(position);
                    else if (type.equals("Business"))
                        BusinessFragment.update(position);
                    else if (type.equals("Security"))
                        SecurityFragment.update(position);
                    else if (type.equals("Entertainment"))
                        EntertainmentFragment.update(position);
                }
            }
        });
    }

    public static void loadInitialArticle(final int position, final String type) {
        if (type.equals("Gear"))
            articles = Article.GearArticles;
        else if (type.equals("Design"))
            articles = Article.DesignArticles;
        else if (type.equals("Science"))
            articles = Article.ScienceArticles;
        else if (type.equals("Business"))
            articles = Article.BusinessArticles;
        else if (type.equals("Security"))
            articles = Article.SecurityArticles;
        else if (type.equals("Entertainment"))
            articles = Article.EntertainmentArticles;
        currentArticle = articles.get(position);

        pos = position;
        webView.loadUrl(articles.get(pos).getLink());
        webView.setVisibility(View.INVISIBLE);
        progressWheel.spin();
        Picasso.with(background.getContext()).load(articles.get(pos).getImageURL()).into(background);
        toolbar.setTitle(type);

        if (currentArticle.isFavorite())
            fab.setImageResource(R.mipmap.favorite);
        else
            fab.setImageResource(R.mipmap.favorite_border);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (articles.get(pos).isFavorite()) {
                    fab.setImageResource(R.mipmap.favorite_border);
                    articles.get(pos).setFavorite(false);

                    editor.remove(articles.get(pos).getTitle());
                    editor.commit();

                    if (type.equals("Gear"))
                        GearFragment.update(position);
                    else if (type.equals("Design"))
                        DesignFragment.update(position);
                    else if (type.equals("Science"))
                        ScienceFragment.update(position);
                    else if (type.equals("Business"))
                        BusinessFragment.update(position);
                    else if (type.equals("Security"))
                        SecurityFragment.update(position);
                    else if (type.equals("Entertainment"))
                        EntertainmentFragment.update(position);

                } else if (!articles.get(pos).isFavorite()) {
                    fab.setImageResource(R.mipmap.favorite);
                    articles.get(pos).setFavorite(true);

                    editor.putString(articles.get(pos).getTitle(), "");
                    editor.commit();

                    if (type.equals("Gear"))
                        GearFragment.update(position);
                    else if (type.equals("Design"))
                        DesignFragment.update(position);
                    else if (type.equals("Science"))
                        ScienceFragment.update(position);
                    else if (type.equals("Business"))
                        BusinessFragment.update(position);
                    else if (type.equals("Security"))
                        SecurityFragment.update(position);
                    else if (type.equals("Entertainment"))
                        EntertainmentFragment.update(position);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.share:
                if (CheckNetwork.isInternetAvailable(getApplicationContext())) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this article from WIRED");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentArticle.getTitle() + ": \n" + currentArticle.getLink());
                    startActivity(Intent.createChooser(sharingIntent, "Share this article"));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter.addFragment(new BusinessFragment(), "Business");
        adapter.addFragment(new DesignFragment(), "Design");
        adapter.addFragment(new EntertainmentFragment(), "Entertainment");
        adapter.addFragment(new GearFragment(), "Gear");
        adapter.addFragment(new ScienceFragment(), "Science");
        adapter.addFragment(new SecurityFragment(), "Security");

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(7);
    }

    static class Adapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments = new ArrayList<>();
        private List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        public void clear() {
            mFragments = new ArrayList<>();
            mFragmentTitles = new ArrayList<>();

        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    public boolean isTablet() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
