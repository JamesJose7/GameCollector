package com.jeeps.gamecollector.model;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
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

    public Game(String key, String name, String publisher, String imageUri, String platform, boolean isPhysical) {
        id = key;
        this.name = name;
        this.publisher = publisher;
        this.imageUri = imageUri;
        this.platform = platform;
        timesCompleted = 0;
        dateAdded = (new Date()).getTime() + "";
        this.isPhysical = isPhysical;
    }

    public Game() {}

    public String getId() {
        return id;
    }

    public void setId(String key) {
        id = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getTimesCompleted() {
        return timesCompleted;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setTimesCompleted(int timesCompleted) {
        this.timesCompleted = timesCompleted;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public boolean isPhysical() {
        return isPhysical;
    }

    public void setPhysical(boolean physical) {
        isPhysical = physical;
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
