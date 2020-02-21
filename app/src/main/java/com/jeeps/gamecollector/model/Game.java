package com.jeeps.gamecollector.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by jeeps on 12/23/2017.
 */
public class Game implements Serializable {
    @SerializedName("gameId")
    private String id;
    //Game data
    private String user;
    private String dateAdded;
    private String imageUri;
    private boolean isPhysical;
    private String name;
    private String shortName;
    private String platformId;
    private String platform;
    private String publisherId;
    private String publisher;
    private int timesCompleted;

    public Game(String imageUri, boolean isPhysical, String name, String shortName,
                String platformId, String platform, String publisherId, String publisher) {
        this.imageUri = imageUri;
        this.isPhysical = isPhysical;
        this.name = name;
        this.shortName = shortName;
        this.platformId = platformId;
        this.platform = platform;
        this.publisherId = publisherId;
        this.publisher = publisher;
        timesCompleted = 0;
    }

    public Game() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isPhysical() {
        return isPhysical;
    }

    public void setPhysical(boolean physical) {
        isPhysical = physical;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getTimesCompleted() {
        return timesCompleted;
    }

    public void setTimesCompleted(int timesCompleted) {
        this.timesCompleted = timesCompleted;
    }
}
