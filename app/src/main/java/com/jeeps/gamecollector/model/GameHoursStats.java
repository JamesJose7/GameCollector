package com.jeeps.gamecollector.model;

import com.jeeps.gamecollector.model.hltb.GameplayHoursStats;

import java.io.Serializable;

public class GameHoursStats implements Serializable {
    private double gameplayMain;
    private double gameplayMainExtra;
    private double gameplayCompletionist;

    public GameHoursStats(double gameplayMain, double gameplayMainExtra, double gameplayCompletionist) {
        this.gameplayMain = gameplayMain;
        this.gameplayMainExtra = gameplayMainExtra;
        this.gameplayCompletionist = gameplayCompletionist;
    }

    public GameHoursStats(GameplayHoursStats gameplayHoursStats) {
        this.gameplayMain = gameplayHoursStats.getGameplayMain();
        this.gameplayMainExtra = gameplayHoursStats.getGameplayMainExtra();
        this.gameplayCompletionist = gameplayHoursStats.getGameplayCompletionist();
    }

    public GameHoursStats() {
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
}
