package com.jeeps.gamecollector.model.hltb;

import com.jeeps.gamecollector.model.GameHoursStats;

import java.util.List;

public class GameplayHoursStats {
    private String id;
    private String name;
    private String description;
    private List<Object> platforms;
    private String imageUrl;
    private List<List<String>> timeLabels;
    private double gameplayMain;
    private double gameplayMainExtra;
    private double gameplayCompletionist;
    private double similarity;
    private String searchTerm;
    private List<Object> playableOn;

    public GameplayHoursStats(GameHoursStats gameHoursStats) {
        gameplayMain = gameHoursStats.getGameplayMain();
        gameplayMainExtra = gameHoursStats.getGameplayMainExtra();
        gameplayCompletionist = gameHoursStats.getGameplayCompletionist();
    }

    public GameplayHoursStats() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Object> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<Object> platforms) {
        this.platforms = platforms;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<List<String>> getTimeLabels() {
        return timeLabels;
    }

    public void setTimeLabels(List<List<String>> timeLabels) {
        this.timeLabels = timeLabels;
    }

    public double getGameplayMain() {
        return gameplayMain;
    }

    public void setGameplayMain(double gameplayMain) {
        this.gameplayMain = gameplayMain;
    }

    public double getGameplayMainExtra() {
        return gameplayMainExtra;
    }

    public void setGameplayMainExtra(double gameplayMainExtra) {
        this.gameplayMainExtra = gameplayMainExtra;
    }

    public double getGameplayCompletionist() {
        return gameplayCompletionist;
    }

    public void setGameplayCompletionist(double gameplayCompletionist) {
        this.gameplayCompletionist = gameplayCompletionist;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public List<Object> getPlayableOn() {
        return playableOn;
    }

    public void setPlayableOn(List<Object> playableOn) {
        this.playableOn = playableOn;
    }
}
