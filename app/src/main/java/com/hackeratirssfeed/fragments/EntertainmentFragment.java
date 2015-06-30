package com.hackeratirssfeed.fragments;

/**
 * Created by user on 6/22/15.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hackeratirssfeed.R;
import com.hackeratirssfeed.activities.ArticleDetailActivity;
import com.hackeratirssfeed.activities.MainActivity;
import com.hackeratirssfeed.objects.Article;
import com.hackeratirssfeed.utilities.CheckNetwork;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class EntertainmentFragment extends Fragment {

    RecyclerView recyclerView;
    static SimpleStringRecyclerViewAdapter adapter;
    SharedPreferences sharedPreferences;
    SwipeRefreshLayout swipeRefreshLayout;

    ProgressWheel progressWheel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        progressWheel = (ProgressWheel) v.findViewById(R.id.progress);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (CheckNetwork.isInternetAvailable(getActivity())) {
                    new GetFeed().execute();
                    progressWheel.spin();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    progressWheel.stopSpinning();
                }
            }
        });

        if (CheckNetwork.isInternetAvailable(getActivity())) {
            new GetFeed().execute();
            progressWheel.spin();
        } else if (!isTablet()) {
            progressWheel.stopSpinning();
        }
        return v;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        adapter = new SimpleStringRecyclerViewAdapter(getActivity());
        recyclerView.setAdapter(adapter);
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue typedValue = new TypedValue();
        private int background;
        private ArrayList<Article> articles;

        public static class ViewHolder extends RecyclerView.ViewHolder {

            public final View mView;
            public final TextView title, author, date;
            public final CircleImageView imageView;
            public final ImageView favorited;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                title = (TextView) view.findViewById(R.id.title);
                author = (TextView) view.findViewById(R.id.author);
                date = (TextView) view.findViewById(R.id.date);
                imageView = (CircleImageView) view.findViewById(R.id.image);
                favorited = (ImageView) view.findViewById(R.id.favorited);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + title.getText();
            }
        }

        public Article getItemAt(int position) {
            return articles.get(position);
        }

        public SimpleStringRecyclerViewAdapter(Context context) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);
            background = typedValue.resourceId;
            articles = Article.EntertainmentArticles;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            view.setBackgroundResource(background);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.title.setText(getItemAt(position).getTitle());
            holder.author.setText(getItemAt(position).getAuthor());
            holder.date.setText(getItemAt(position).getDate());

            if (getItemAt(position).isFavorite())
                holder.favorited.setVisibility(View.VISIBLE);
            else
                holder.favorited.setVisibility(View.INVISIBLE);

            Picasso.with(holder.imageView.getContext())
                    .load(getItemAt(position).getImageURL())
                    .resize(50, 50)
                    .into(holder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            getItemAt(position).setPaletteBitmap(((BitmapDrawable) holder.imageView.getDrawable()).getBitmap());
                        }

                        @Override
                        public void onError() {

                        }
                    });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((holder.mView.getResources().getConfiguration().screenLayout
                            & Configuration.SCREENLAYOUT_SIZE_MASK)
                            >= Configuration.SCREENLAYOUT_SIZE_LARGE) {

                        MainActivity.loadArticle(position, "Entertainment");

                    } else {
                        Intent intent = new Intent(v.getContext(), ArticleDetailActivity.class);
                        intent.putExtra("articleType", "entertainment");
                        intent.putExtra("position", position);
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return articles.size();
        }
    }

    public InputStream getInputStream(URL url) throws IOException {
        return url.openConnection().getInputStream();
    }

    private class GetFeed extends AsyncTask<String, Void, String> {

        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> links = new ArrayList<>();
        ArrayList<String> images = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<String> contents = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            if (isTablet())
                MainActivity.noInternet.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL("http://www.wired.com/category/underwire/feed/");

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser parser = factory.newPullParser();

                parser.setInput(getInputStream(url), "UTF_8");

                boolean isIn = false;

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {

                        if (parser.getName().equalsIgnoreCase("item")) {
                            isIn = true;
                        } else if (parser.getName().equalsIgnoreCase("title")) {
                            if (isIn) {
                                titles.add(parser.nextText());
                            }
                        } else if (parser.getName().equalsIgnoreCase("link")) {
                            if (isIn) {
                                links.add(parser.nextText());
                            }
                        } else if (parser.getName().equalsIgnoreCase("description")) {
                            if (isIn) {
                                String data = parser.nextText();
                                images.add(data.split("\"")[3]);
                                data = data.replaceAll("<!\\[CDATA\\[", "");
                                data = data.replaceAll("\\]\\]>", "");
                                data = data.replaceAll("</p>", "<p>");
                                data = data.split("<p>")[1];
                                contents.add(data);
                            }
                        } else if (parser.getName().equalsIgnoreCase("pubdate")) {
                            if (isIn) {
                                String[] datesArray = parser.nextText().split(" ");
                                datesArray[0] = datesArray[0].replace(",", "");
                                switch (datesArray[0].toLowerCase()) {
                                    case "mon":
                                        datesArray[0] = "Monday";
                                        break;
                                    case "tue":
                                        datesArray[0] = "Tuesday";
                                        break;
                                    case "wed":
                                        datesArray[0] = "Wednesday";
                                        break;
                                    case "thu":
                                        datesArray[0] = "Thursday";
                                        break;
                                    case "fri":
                                        datesArray[0] = "Friday";
                                        break;
                                    case "sat":
                                        datesArray[0] = "Saturday";
                                        break;
                                    case "sun":
                                        datesArray[0] = "Sunday";
                                        break;
                                }
                                dates.add(datesArray[0] + ", " + datesArray[2] + " " + datesArray[1]);
                            }
                        } else if (parser.getName().equalsIgnoreCase("dc:creator")) {
                            if (isIn) {
                                authors.add(parser.nextText());
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                        isIn = false;
                    }
                    eventType = parser.next();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Article.EntertainmentArticles.clear();
            if (titles.size() == links.size() && links.size() == images.size()
                    && images.size() == dates.size() && dates.size() == authors.size())
                for (int i = 0; i < titles.size(); i++)
                    if (sharedPreferences.contains(titles.get(i)))
                        Article.EntertainmentArticles.add(new Article(titles.get(i), links.get(i), images.get(i), dates.get(i), authors.get(i), contents.get(i), true));
                    else
                        Article.EntertainmentArticles.add(new Article(titles.get(i), links.get(i), images.get(i), dates.get(i), authors.get(i), contents.get(i), false));
            else
                Log.e("Error", "ArrayList sizes do not match, investigate");

            setupRecyclerView(recyclerView);
            swipeRefreshLayout.setRefreshing(false);
            progressWheel.stopSpinning();
        }
    }

    @Override
    public void onResume() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
        super.onResume();
    }

    public static void update(int position) {
        adapter.notifyItemChanged(position);
    }

    public boolean isTablet() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
