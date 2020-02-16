package com.jeeps.gamecollector.model;

public class CurrentUser {
    private String uid;
    private String token;
    private String username;

    public CurrentUser() {}

    public CurrentUser(String uid, String token, String username) {
        this.uid = uid;
        this.token = token;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
