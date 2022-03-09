package com.jeeps.gamecollector.comparators;

import com.jeeps.gamecollector.remaster.data.model.data.Game;

import java.util.Comparator;

/**
 * Created by jeeps on 1/9/2018.
 */

public class GameByTimesPlayedComparator implements Comparator<Game> {

    private boolean desc;

    public GameByTimesPlayedComparator(boolean desc) {
        this.desc = desc;
    }

    public GameByTimesPlayedComparator() {
        this.desc = false;
    }

    @Override
    public int compare(Game game1, Game game2) {
        if (!desc)
            return Integer.compare(game1.getTimesCompleted(), game2.getTimesCompleted());
        return Integer.compare(game1.getTimesCompleted(), game2.getTimesCompleted()) * -1;
    }
}

