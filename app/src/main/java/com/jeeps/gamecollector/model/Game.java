package com.jeeps.gamecollector.model;

import java.util.Date;

/**
 * Created by jeeps on 12/23/2017.
 */

public class Game {
    //Game data
    private String mName;
    private String mPublisher;
    private String mImageUri;
    private String mPlatform;

    //User data
    private int mTimesCompleted;
    private Date mDateAdded;

    public Game(String name, String publisher, String imageUri, String platform) {
        mName = name;
        mPublisher = publisher;
        mImageUri = imageUri;
        mPlatform = platform;
        mTimesCompleted = 0;
        mDateAdded = new Date();
    }

    public Game() {}

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPublisher() {
        return mPublisher;
    }

    public void setPublisher(String publisher) {
        mPublisher = publisher;
    }

    public String getImageUri() {
        return mImageUri;
    }

    public void setImageUri(String imageUri) {
        mImageUri = imageUri;
    }

    public String getPlatform() {
        return mPlatform;
    }

    public void setPlatform(String platform) {
        mPlatform = platform;
    }

    public int getTimesCompleted() {
        return mTimesCompleted;
    }

    public void addCompletion() {
        mTimesCompleted++;
    }

    public void removeCompletion() {
        mTimesCompleted--;
    }

    public Date getDateAdded() {
        return mDateAdded;
    }
}
