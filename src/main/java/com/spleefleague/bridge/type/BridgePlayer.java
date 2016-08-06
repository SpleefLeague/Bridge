package com.spleefleague.bridge.type;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by Josh on 04/08/2016.
 */
public class BridgePlayer {

    private final UUID uuid;
    private final String username;
    private String server;
    private Rank rank;

    public BridgePlayer(UUID uuid, String username, String server, Rank rank) {
        this.uuid = uuid;
        this.username = username;
        this.server = server;
        this.rank = rank;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getServer() {
        return server;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public JSONObject serialize() {
        JSONObject result = new JSONObject();
        try {
            result.put("uuid", uuid.toString());
            result.put("username", username);
            result.put("rank", rank.name());
            result.put("playerServer", server);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static BridgePlayer deserialize(JSONObject jsonObject) {
        if(!jsonObject.has("uuid") || !jsonObject.has("username") || !jsonObject.has("rank") || !jsonObject.has("playerServer")) {
            return null;
        }
        Rank rank;
        try {
            rank = Rank.valueOf(jsonObject.getString("rank").toUpperCase());
        } catch (Exception e) {
            rank = Rank.DEFAULT;
        }
        try {
            return new BridgePlayer(UUID.fromString(jsonObject.getString("uuid")), jsonObject.getString("username"), jsonObject.getString("playerServer"), rank);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
