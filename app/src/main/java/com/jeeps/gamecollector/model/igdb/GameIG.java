package com.jeeps.gamecollector.model.igdb;

import com.google.gson.annotations.SerializedName;

public class GameIG {
    private int id;
    private int category;
    private int cover;
    @SerializedName("first_release_date")
    private long firstReleaseDate;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getCover() {
        return cover;
    }

    public void setCover(int cover) {
        this.cover = cover;
    }

    public long getFirstReleaseDate() {
        return firstReleaseDate;
    }

    public void setFirstReleaseDate(long firstReleaseDate) {
        this.firstReleaseDate = firstReleaseDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
