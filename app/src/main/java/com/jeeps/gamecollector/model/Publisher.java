package com.jeeps.gamecollector.model;

/**
 * Created by jeeps on 12/23/2017.
 */

public class Publisher {
    private String mName;

    public Publisher(String name) {
        mName = name;
    }

    public Publisher() {}

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
