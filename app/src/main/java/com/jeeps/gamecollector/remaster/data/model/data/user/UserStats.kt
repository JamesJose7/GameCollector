package com.jeeps.gamecollector.model;

import com.google.firebase.firestore.PropertyName;
import com.google.gson.annotations.SerializedName;
import com.jeeps.gamecollector.remaster.data.model.data.platforms.PlatformStats;

import java.util.List;

public class UserStats {
    @SerializedName("statsId")
    private String id;
    private String user;
    private String userId;

    private int physicalTotal;
    private int digitalTotal;
    private int completedGamesTotal;
    private String lastGameCompleted;

    @SerializedName("platforms")
    private List<PlatformStats> platformStats;


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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPhysicalTotal() {
        return physicalTotal;
    }

    public void setPhysicalTotal(int physicalTotal) {
        this.physicalTotal = physicalTotal;
    }

    public int getDigitalTotal() {
        return digitalTotal;
    }

    public void setDigitalTotal(int digitalTotal) {
        this.digitalTotal = digitalTotal;
    }

    public int getCompletedGamesTotal() {
        return completedGamesTotal;
    }

    public void setCompletedGamesTotal(int completedGamesTotal) {
        this.completedGamesTotal = completedGamesTotal;
    }

    public String getLastGameCompleted() {
        return lastGameCompleted;
    }

    public void setLastGameCompleted(String lastGameCompleted) {
        this.lastGameCompleted = lastGameCompleted;
    }

    @PropertyName("platforms")
    public List<PlatformStats> getPlatformStats() {
        return platformStats;
    }

    @PropertyName("platforms")
    public void setPlatformStats(List<PlatformStats> platformStats) {
        this.platformStats = platformStats;
    }
}
