package com.jeeps.gamecollector.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeeps on 12/23/2017.
 */
@IgnoreExtraProperties
public class Game /*implements Comparable<Game>*/ {
    //Game data
    private String mName;
    private String mPublisher;
    private String mImageUri;
    private String mPlatform;

    //User data
    private int mTimesCompleted;
    private String mDateAdded;

    public Game(String name, String publisher, String imageUri, String platform) {
        mName = name;
        mPublisher = publisher;
        mImageUri = imageUri;
        mPlatform = platform;
        mTimesCompleted = 0;
        mDateAdded = (new Date()).getTime() + "";
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

    public String getDateAdded() {
        return mDateAdded;
    }

    @Exclude
    public Map<String, Object> toMap() {
        /*//Game data
        private String mName;
        private String mPublisher;
        private String mImageUri;
        private String mPlatform;

        //User data
        private int mTimesCompleted;
        private Date mDateAdded;*/
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", mName);
        result.put("publisher", mPublisher);
        result.put("imageUri", mImageUri);
        result.put("platform", mPlatform);
        result.put("timesCompleted", mTimesCompleted);
        result.put("dateAdded", mDateAdded);

        return result;
    }
}
