package com.jeeps.gamecollector.comparators;

import com.jeeps.gamecollector.remaster.data.model.data.Game;
import com.jeeps.gamecollector.remaster.data.model.data.GameHoursStats;

import java.util.Comparator;

public class GameByHoursCompletionistComparator implements Comparator<Game> {
    private boolean desc;

    public GameByHoursCompletionistComparator(boolean desc) {
        this.desc = desc;
    }

    public GameByHoursCompletionistComparator() {
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
                    gameHoursStats1.getGameplayCompletionist(),
                    gameHoursStats2.getGameplayCompletionist());
        return Double.compare(
                gameHoursStats1.getGameplayCompletionist(),
                gameHoursStats2.getGameplayCompletionist()) * -1;
    }
}
