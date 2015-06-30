package com.hackeratirssfeed.objects;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import java.util.ArrayList;

/**
 * Created by user on 6/22/15.
 */
public class Article {

    private String title, link, imageURL, date, author, fullContent;
    private int vibrant = 0, vibrantLight = 0, vibrantDark = 0, muted = 0, mutedLight = 0, mutedDark = 0;
    private boolean isFavorite;

    public static ArrayList<Article> DesignArticles = new ArrayList<>(),
            GearArticles = new ArrayList<>(), ScienceArticles = new ArrayList<>(),
            SecurityArticles = new ArrayList<>(), EntertainmentArticles = new ArrayList<>(),
            BusinessArticles = new ArrayList<>(), FavoritedArticles = new ArrayList<>();

    public Article(String title, String link, String imageURL, String date, String author, String fullContent, boolean isFavorite) {
        this.title = title;
        this.link = link;
        this.imageURL = imageURL;
        this.date = date;
        this.author = author;
        this.fullContent = fullContent;
        this.isFavorite = isFavorite;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }


    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    // Palette colors ------------------

    public void setVibrant(int vibrant) {
        this.vibrant = vibrant;
    }

    public void setVibrantLight(int vibrantLight) {
        this.vibrantLight = vibrantLight;
    }

    public int getVibrantDark() {
        return vibrantDark;
    }

    public void setVibrantDark(int vibrantDark) {
        this.vibrantDark = vibrantDark;
    }

    public void setMuted(int muted) {
        this.muted = muted;
    }

    public void setMutedLight(int mutedLight) {
        this.mutedLight = mutedLight;
    }

    public int getMutedDark() {
        return mutedDark;
    }

    public void setMutedDark(int mutedDark) {
        this.mutedDark = mutedDark;
    }

    public void setPaletteBitmap(Bitmap bitmap) {
        Palette palette = Palette.generate(bitmap);
        setVibrant(palette.getVibrantColor(0x000000));
        setVibrantLight(palette.getLightVibrantColor(0x000000));
        setVibrantDark(palette.getDarkVibrantColor(0x000000));
        setMuted(palette.getMutedColor(0x000000));
        setMutedLight(palette.getLightMutedColor(0x000000));
        setMutedDark(palette.getDarkMutedColor(0x000000));
    }
}
