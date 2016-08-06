package com.spleefleague.connections;

import com.spleefleague.connections.threads.SocketIOThread;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Josh on 21/02/2016.
 */
public class Connections {

    private static Connections instance;
    private final SocketIOThread socketIOThread;
    private final HashMap<String, List<String>> redirects;

    private Connections() throws IOException {
        instance = this;
        this.redirects = new HashMap<>();
        this.socketIOThread = new SocketIOThread();
        new Thread(socketIOThread).start();

        System.out.println("SocketIO server started!");
    }

    /**
     * Get singleton Connections instance.
     *
     * @return instance.
     */
    public static Connections getInstance() {
        return instance;
    }

    /**
     * Get server redirects.
     *
     * @return hashmap of server redirects - keys = channels and values = list
     * of servers to send to. If undefined then default to all.
     */
    public HashMap<String, List<String>> getRedirects() {
        return redirects;
    }

    /**
     * Get the SocketIO thread.
     *
     * @return socketio thread, shouldn't be null.
     */
    public SocketIOThread getSocketIOThread() {
        return socketIOThread;
    }

    public static void main(String[] args) {
        try {
            new Connections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
