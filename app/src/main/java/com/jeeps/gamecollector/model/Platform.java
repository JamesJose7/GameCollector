package com.jeeps.gamecollector.model;

/**
 * Created by jeeps on 12/23/2017.
 */

public class Platform {
    private int mId;
    private String mName;
    private String mImageUri;
    private String mColor;

    public Platform(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public Platform() {}

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUri() {
        return mImageUri;
    }

    public void setImageUri(String imageUri) {
        mImageUri = imageUri;
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String color) {
        mColor = color;
    }
}
