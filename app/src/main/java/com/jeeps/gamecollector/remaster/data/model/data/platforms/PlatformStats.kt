package com.jeeps.gamecollector.model;

public class PlatformStats {
    private String platformId;
    private String platformName;
    private int physicalTotal;
    private int digitalTotal;
    private int completedGamesTotal;
    private String lastGameCompleted;

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
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
}
