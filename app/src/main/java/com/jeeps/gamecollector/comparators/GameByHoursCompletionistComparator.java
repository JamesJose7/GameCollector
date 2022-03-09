package com.jeeps.gamecollector.comparators;

import com.jeeps.gamecollector.remaster.data.model.data.games.Game;
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats;

import java.util.Comparator;

public class GameByHoursCompletionistComparator implements Comparator<Game> {
    private final boolean desc;

    public GameByHoursCompletionistComparator(boolean desc) {
        this.desc = desc;
    }

    public GameByHoursCompletionistComparator() {
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
                    gameHoursStats1.getGameplayCompletionist(),
                    gameHoursStats2.getGameplayCompletionist());
        return Double.compare(
                gameHoursStats1.getGameplayCompletionist(),
                gameHoursStats2.getGameplayCompletionist()) * -1;
    }
}
