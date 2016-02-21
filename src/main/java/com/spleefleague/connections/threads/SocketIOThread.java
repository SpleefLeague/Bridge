package com.spleefleague.connections.threads;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.spleefleague.connections.Connections;
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
            } else if(Connections.getInstance().getRedirects().containsKey(channel.toLowerCase())) {
                Connections.getInstance().getRedirects().get(channel.toLowerCase()).stream().filter(servers::containsKey).forEach((String server) -> servers.get(server).sendEvent("global", jsonNode));
            } else {
                servers.values().forEach(s -> s.sendEvent("global", jsonNode));
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

    public SocketIOServer getSocketIOServer() {
        return socketIOServer;
    }

    /**
     * Stop the SocketIOServer.
     */
    public void stop() {
        this.socketIOServer.stop();
    }

}
