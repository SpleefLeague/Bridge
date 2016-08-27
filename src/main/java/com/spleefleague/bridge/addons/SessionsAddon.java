package com.spleefleague.bridge.addons;

import com.corundumstudio.socketio.SocketIOClient;
import com.spleefleague.bridge.SLBridge;
import com.spleefleague.bridge.type.Addon;
import com.spleefleague.bridge.type.BridgePlayer;
import com.spleefleague.bridge.type.Rank;
import org.json.JSONObject;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Josh on 04/08/2016.
 */
public class SessionsAddon extends Addon {

    @Override
    public boolean forward() {
        return false;
    }

    @Override
    public String channel() {
        return "sessions";
    }

    @Override
    public void handle(JSONObject jsonObject) throws Exception {
        if(!jsonObject.has("action")) {
            return;
        }
        Action action = Action.valueOf(jsonObject.getString("action").toUpperCase());
        switch (action) {
            case ADD_PLAYER: {
                BridgePlayer bridgePlayer = BridgePlayer.deserialize(jsonObject);
                if(bridgePlayer != null) {
                    SLBridge.getInstance().getSessionManager().cachePlayer(bridgePlayer);
                }
                break;
            }
            case REMOVE_PLAYER: {
                UUID uuid = UUID.fromString(jsonObject.getString("uuid"));
                if(SLBridge.getInstance().getSessionManager().hasPlayer(uuid)) {
                    BridgePlayer bridgePlayer = SLBridge.getInstance().getSessionManager().getPlayer(uuid);
                    if(bridgePlayer.getRank().ordinal() >= Rank.MODERATOR.ordinal()) {
                        JSONObject send = new JSONObject();
                        // Quick workaround for now, requested feature..
                        send.put("message", "§4[§c-§4]§e " + bridgePlayer.getUsername() + " left.");
                        SLBridge.getInstance().getSocketIOThread().broadcast("staff", send);
                    }

                    SLBridge.getInstance().getSessionManager().removePlayer(uuid);
                }
                break;
            }
            case GET_PLAYER: {
                UUID uuid = UUID.fromString(jsonObject.getString("uuid"));
                SocketIOClient socketIOClient = SLBridge.getInstance().getSocketIOThread().getServer(jsonObject.getString("server"));
                if(socketIOClient == null) {
                    return;
                }
                if(SLBridge.getInstance().getSessionManager().hasPlayer(uuid)) {
                    SLBridge.getInstance().getSocketIOThread().send(socketIOClient, channel(),
                            handleResponse(jsonObject, SLBridge.getInstance().getSessionManager().getPlayer(uuid).serialize()));
                } else {
                    JSONObject response = new JSONObject();
                    response.put("uuid", uuid.toString());
                    response.put("playerServer", "OFFLINE");
                    SLBridge.getInstance().getSocketIOThread().send(socketIOClient, channel(), handleResponse(jsonObject, response));
                }
                break;
            }
            case GET_STAFF: {
                JSONObject result = new JSONObject();
                result.put("staff", SLBridge.getInstance().getSessionManager().getPlayersByRank(Rank.MODERATOR, true).stream().map(BridgePlayer::serialize).collect(Collectors.toList()));

                SocketIOClient socketIOClient = SLBridge.getInstance().getSocketIOThread().getServer(jsonObject.getString("server"));
                if(socketIOClient != null) {
                    SLBridge.getInstance().getSocketIOThread().send(socketIOClient, channel(), handleResponse(jsonObject, result));
                }
                break;
            }
            case UPDATE_INFO: {
                UUID uuid = UUID.fromString(jsonObject.getString("uuid"));
                if(SLBridge.getInstance().getSessionManager().hasPlayer(uuid)) {
                    BridgePlayer bridgePlayer = SLBridge.getInstance().getSessionManager().getPlayer(uuid);
                    if(bridgePlayer != null) {
                        Rank rank;
                        try {
                            rank = Rank.valueOf(jsonObject.getString("rank").toUpperCase());
                        } catch (Exception e) {
                            rank = Rank.DEFAULT;
                        }

                        if(rank.ordinal() >= Rank.MODERATOR.ordinal() && bridgePlayer.getServer().equalsIgnoreCase("PENDING")) {
                            JSONObject send = new JSONObject();
                            // Quick workaround for now, requested feature..
                            send.put("message", "§2[§a+§2]§e " + bridgePlayer.getUsername() + " joined.");
                            SLBridge.getInstance().getSocketIOThread().broadcast("staff", send);
                        }
                        bridgePlayer.setRank(rank);
                        bridgePlayer.setServer(jsonObject.getString("server"));
                    }
                }
                break;
            }
        }
    }

    public enum Action {

        ADD_PLAYER, REMOVE_PLAYER, GET_PLAYER, GET_STAFF, UPDATE_INFO;

    }

}
