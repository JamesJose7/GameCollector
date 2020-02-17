package com.jeeps.gamecollector.model;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeeps on 12/23/2017.
 */
public class Game implements Serializable {
    @SerializedName("gameId")
    private String id;
    //Game data
    private String user;
    private String dateAdded;
    private String imageUri;
    private boolean isPhysical;
    private String name;
    private String shortName;
    private String platformId;
    private String platform;
    private String publisherId;
    private String publisher;
    private int timesCompleted;

    public Game(String imageUri, boolean isPhysical, String name, String shortName,
                String platformId, String platform, String publisherId, String publisher) {
        this.imageUri = imageUri;
        this.isPhysical = isPhysical;
        this.name = name;
        this.shortName = shortName;
        this.platformId = platformId;
        this.platform = platform;
        this.publisherId = publisherId;
        this.publisher = publisher;
        timesCompleted = 0;
    }

    public Game() {}

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

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isPhysical() {
        return isPhysical;
    }

    public void setPhysical(boolean physical) {
        isPhysical = physical;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getTimesCompleted() {
        return timesCompleted;
    }

    public void setTimesCompleted(int timesCompleted) {
        this.timesCompleted = timesCompleted;
    }

    @Exclude
    public Map<String, Object> toMap() {
        /*//Game data
        private String name;
        private String publisher;
        private String imageUri;
        private String platform;

        //User data
        private int timesCompleted;
        private Date dateAdded;*/
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", id);
        result.put("name", name);
        result.put("publisher", publisher);
        result.put("imageUri", imageUri);
        result.put("platform", platform);
        result.put("timesCompleted", timesCompleted);
        result.put("dateAdded", dateAdded);
        result.put("isPhysical", isPhysical);

        return result;
    }

    public void jsonToGame(JSONObject gameJson) throws JSONException {
        id = gameJson.getString("key");
        dateAdded = gameJson.getString("dateAdded");
        imageUri = gameJson.getString("imageUri");
        isPhysical = gameJson.getBoolean("isPhysical");
        name = gameJson.getString("name");
        platform = gameJson.getString("platform");
        publisher = gameJson.getString("publisher");
        timesCompleted = gameJson.getInt("timesCompleted");
    }

    public static Game mapToGame(HashMap<String, Object> map) {
        Game game = new Game();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case "key":
                    game.setId((String) entry.getValue());
                    break;
                case "imageUri":
                    game.setImageUri((String) entry.getValue());
                    break;
                case "name":
                    game.setName((String) entry.getValue());
                    break;
                case "publisher":
                    game.setPublisher((String) entry.getValue());
                    break;
                case "dateAdded":
                    game.setDateAdded((String) entry.getValue());
                    break;
                case "platform":
                    game.setPlatform((String) entry.getValue());
                    break;
                case "timesCompleted":
                    game.setTimesCompleted(((Long) entry.getValue()).intValue());
                    break;
                case "isPhysical":
                    game.setPhysical((Boolean) entry.getValue());
                default:
            }
        }
        return game;
    }


}
