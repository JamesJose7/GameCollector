package com.jeeps.gamecollector.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jeeps on 12/23/2017.
 */

public class Publisher {
    @SerializedName("publisherId")
    private String id;
    private String name;

    public Publisher(String name) {
        this.name = name;
    }

    public Publisher() {}

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
}
