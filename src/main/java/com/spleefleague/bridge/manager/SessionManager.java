package com.spleefleague.bridge.manager;

import com.spleefleague.bridge.SLBridge;
import com.spleefleague.bridge.type.BridgePlayer;
import com.spleefleague.bridge.type.Rank;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Josh on 04/08/2016.
 */
public class SessionManager {

    private final HashMap<UUID, BridgePlayer> onlinePlayerCache;

    public SessionManager() {
        this.onlinePlayerCache = new HashMap<>();

        startRefreshTask();
    }

    /**
     * Add a player to the player cache.
     *
     * @param bridgePlayer player to add.
     */
    public void cachePlayer(BridgePlayer bridgePlayer) {
        if(hasPlayer(bridgePlayer.getUUID())) {
            return;
        }
        onlinePlayerCache.put(bridgePlayer.getUUID(), bridgePlayer);
    }

    /**
     * Check whether a player is cached (aka online).
     *
     * @param uuid uuid of player.
     * @return true = cached (object exists), false if not.
     */
    public boolean hasPlayer(UUID uuid) {
        return onlinePlayerCache.containsKey(uuid);
    }

    /**
     * Get a cached player.
     *
     * @param uuid uuid of player.
     * @return BridgePlayer instance if present - null if not.
     */
    public BridgePlayer getPlayer(UUID uuid) {
        if(!hasPlayer(uuid)) {
            return null;
        }
        return onlinePlayerCache.get(uuid);
    }

    /**
     * Remove a player from the local cache (i.e. when they're leaving).
     *
     * @param uuid uuid of player to remove.
     */
    public void removePlayer(UUID uuid) {
        if(hasPlayer(uuid)) {
            onlinePlayerCache.remove(uuid);
        }
    }

    /**
     * Get players online by rank.
     *
     * @param rank rank to check for.
     * @param plus include players with ranks above the rank given.
     * @return set of players. Shouldn't be null, might be empty.
     */
    public Set<BridgePlayer> getPlayersByRank(Rank rank, boolean plus) {
        return onlinePlayerCache.values().stream()
                .filter(bridgePlayer -> (plus ? bridgePlayer.getRank().ordinal() >= rank.ordinal() : bridgePlayer.getRank().ordinal() == rank.ordinal()))
                .collect(Collectors.toSet());
    }

    /**
     * Start pending update task.
     */
    private void startRefreshTask() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            onlinePlayerCache.values().stream().filter((BridgePlayer bridgePlayer) -> bridgePlayer.getServer().equalsIgnoreCase("PENDING")).forEach((BridgePlayer bridgePlayer) -> {
                JSONObject request = new JSONObject();
                try {
                    request.put("uuid", bridgePlayer.getUUID().toString());
                    request.put("action", "REQUEST_UPDATE");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SLBridge.getInstance().getSocketIOThread().broadcast("sessions", request);
            });
        }, 5, 30, TimeUnit.SECONDS);
    }

}
