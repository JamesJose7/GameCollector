package com.jeeps.gamecollector.comparators;

import com.jeeps.gamecollector.remaster.data.model.data.games.Game;
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats;

import java.util.Comparator;

public class GameByHoursMainExtraComparator implements Comparator<Game> {
    private final boolean desc;

    public GameByHoursMainExtraComparator(boolean desc) {
        this.desc = desc;
    }

    public GameByHoursMainExtraComparator() {
        this.desc = false;
    }

    @Override
    public int compare(Game game1, Game game2) {
        game1.getGameHoursStats();
        GameHoursStats gameHoursStats1 = game1.getGameHoursStats();

        game2.getGameHoursStats();
        GameHoursStats gameHoursStats2 = game2.getGameHoursStats();
        if (!desc)
            return Double.compare(
                    gameHoursStats1.getGameplayMainExtra(),
                    gameHoursStats2.getGameplayMainExtra());
        return Double.compare(
                gameHoursStats1.getGameplayMainExtra(),
                gameHoursStats2.getGameplayMainExtra()) * -1;
    }
}
