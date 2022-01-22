package com.jeeps.gamecollector.comparators;

import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.GameHoursStats;

import java.util.Comparator;

public class GameByHoursStoryComparator implements Comparator<Game> {
    private boolean desc;

    public GameByHoursStoryComparator(boolean desc) {
        this.desc = desc;
    }

    public GameByHoursStoryComparator() {
        this.desc = false;
    }

    @Override
    public int compare(Game game1, Game game2) {
        GameHoursStats gameHoursStats1 = game1.getGameHoursStats() != null ?
                game1.getGameHoursStats() : new GameHoursStats();

        GameHoursStats gameHoursStats2 = game2.getGameHoursStats() != null ?
                game2.getGameHoursStats() : new GameHoursStats();
        if (!desc)
            return Double.compare(
                    gameHoursStats1.getGameplayMain(),
                    gameHoursStats2.getGameplayMain());
        return Double.compare(
                gameHoursStats1.getGameplayMain(),
                gameHoursStats2.getGameplayMain()) * -1;
    }
}
