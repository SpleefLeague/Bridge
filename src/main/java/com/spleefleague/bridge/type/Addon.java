package com.spleefleague.bridge.type;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Josh on 04/08/2016.
 */
public abstract class Addon {

    public abstract boolean forward();

    public abstract String channel();

    public abstract void handle(JSONObject jsonObject) throws Exception;

    public final JSONObject handleResponse(JSONObject sent, JSONObject response) {
        if(sent.has("responseID")) {
            try {
                response.put("responseID", sent.get("responseID"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // For ease
        return response;
    }

}
