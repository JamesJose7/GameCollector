package com.jeeps.gamecollector.comparators;

import com.jeeps.gamecollector.model.Game;

import java.util.Comparator;

/**
 * Created by jeeps on 1/9/2018.
 */

public class GameByPhysicalComparator implements Comparator<Game> {

    private boolean desc;

    public GameByPhysicalComparator(boolean desc) {
        this.desc = desc;
    }

    public GameByPhysicalComparator() {
        this.desc = false;
    }

    @Override
    public int compare(Game game1, Game game2) {
        if (!desc)
            return Boolean.compare(game1.isPhysical(), game2.isPhysical());
        return Boolean.compare(game1.isPhysical(), game2.isPhysical()) * -1;
    }
}
