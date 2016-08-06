package com.spleefleague.bridge.threads;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.spleefleague.bridge.SLBridge;
import com.spleefleague.bridge.type.Addon;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;

/**
 * Created by Josh on 21/02/2016.
 */
public class SocketIOThread implements Runnable {

    private final HashMap<String, SocketIOClient> servers;
    private final SocketIOServer socketIOServer;

    public SocketIOThread() {
        Configuration configuration = new Configuration();
        configuration.setPort(9092);
        SocketConfig socketConfig = configuration.getSocketConfig();
        socketConfig.setReuseAddress(true);
        configuration.setSocketConfig(socketConfig);
        this.socketIOServer = new SocketIOServer(configuration);
        this.servers = new HashMap<>();
        this.socketIOServer.addEventListener("global", JsonNode.class, (socketIOClient, jsonNode, ackRequest) -> {
            JSONObject jsonObject = new JSONObject(new JSONTokener(jsonNode.toString()));
            String channel = jsonObject.getString("channel");
            if(channel.equalsIgnoreCase("connect")) {
                servers.put(jsonObject.getString("name").toLowerCase(), socketIOClient);
                return;
            }
            Addon addon = getAddon(channel);
            if(addon == null) {
                servers.values().forEach(s -> s.sendEvent("global", jsonNode, jsonNode.toString()));
            } else {
                addon.handle(jsonObject);
                if(addon.forward()) {
                    servers.values().forEach(s -> s.sendEvent("global", jsonNode, jsonNode.toString()));
                }
            }
        });
        this.socketIOServer.addDisconnectListener(socketIOClient -> {
            if(servers.containsValue(socketIOClient)) {
                servers.values().remove(socketIOClient);
            }
        });
    }

    @Override
    public void run() {
        this.socketIOServer.start();
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    /**
     * Get current SocketIOServer instance.
     *
     * @return instance of SocketIOServer class, shouldn't be null.
     */
    public SocketIOServer getSocketIOServer() {
        return socketIOServer;
    }

    /**
     * Get a SocketIOClient by server name.
     *
     * @param name server name.
     * @return SocketIOClient instance if present/found, null if not.
     */
    public SocketIOClient getServer(String name) {
        name = name.toLowerCase();
        if(servers.containsKey(name)) {
            return servers.get(name);
        }
        return null;
    }

    /**
     * Send a packet to a SocketIOClient.
     *
     * @param socketIOClient recipient client.
     * @param channel channel to send packet on.
     * @param object JSONObject to send.
     */
    public void send(SocketIOClient socketIOClient, String channel, JSONObject object) {
        try {
            if(!object.has("channel")) {
                object.put("channel", channel);
            }
            if(!object.has("server")) {
                object.put("server", "SOCKET_SERVER");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketIOClient.sendEvent("global", object, object.toString());
    }

    /**
     * Broadcast a packet to each connected server.
     *
     * @param channel channel to broadcast on.
     * @param object object to send.
     */
    public void broadcast(String channel, JSONObject object) {
        try {
            if(!object.has("channel")) {
                object.put("channel", channel);
            }
            if(!object.has("server")) {
                object.put("server", "SOCKET_SERVER");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        servers.values().forEach(s -> s.sendEvent("global", object, object.toString()));
    }

    /**
     * Stop the SocketIOServer.
     */
    public void stop() {
        this.socketIOServer.stop();
    }

    /**
     * Get an addon by its channel.
     *
     * @param channel channel to find addon for.
     * @return Addon if present, null if not.
     */
    private Addon getAddon(String channel) {
        for (Addon addon : SLBridge.getInstance().getAddons()) {
            if(addon.channel().equalsIgnoreCase(channel)) {
                return addon;
            }
        }
        return null;
    }

}
