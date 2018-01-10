package com.jeeps.gamecollector.comparators;

import com.jeeps.gamecollector.model.Game;

import java.util.Comparator;

/**
 * Created by jeeps on 1/9/2018.
 */

public class GameByNameComparator implements Comparator<Game> {

    private boolean desc;

    public GameByNameComparator(boolean desc) {
        this.desc = desc;
    }

    public GameByNameComparator() {
        this.desc = false;
    }

    @Override
    public int compare(Game game1, Game game2) {
        if (!desc)
            return game1.getName().compareTo(game2.getName());
        return (game1.getName().compareTo(game2.getName())) * -1;
    }
}
