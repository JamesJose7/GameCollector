package com.jeeps.gamecollector.model;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jeeps on 12/23/2017.
 */

public class Platform implements Serializable {
    @SerializedName("platformId")
    private String id;
    private String user;
    private String name;
    private String imageUri;
    private String color;

    public Platform() {}

    public Platform(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void jsonToPlatform(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getString("id");
        name = jsonObject.getString("name");
        imageUri = jsonObject.getString("imageUri");
        color = jsonObject.getString("color");
    }
}
