package com.jeeps.gamecollector.model.igdb;

import com.google.gson.annotations.SerializedName;

public class GameCoverIG {
    private int id;
    private int game;
    private int height;
    private int width;
    private String url;
    @SerializedName("image_id")
    private String imageId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGame() {
        return game;
    }

    public void setGame(int game) {
        this.game = game;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getUrl() {
        return url.replace("//", "https://").replace("t_thumb", "t_cover_big");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
