package com.spleefleague.bridge;

import com.spleefleague.bridge.manager.SessionManager;
import com.spleefleague.bridge.threads.SocketIOThread;
import com.spleefleague.bridge.type.Addon;
import com.spleefleague.bridge.util.ReflectionUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Josh on 21/02/2016.
 */
public class SLBridge {

    private static SLBridge instance;

    private final SocketIOThread socketIOThread;
    private final Set<Addon> addons;

    private SessionManager sessionManager;

    private SLBridge() throws IOException {
        instance = this;

        this.addons = new HashSet<>();
        loadAddons();

        this.sessionManager = new SessionManager();

        this.socketIOThread = new SocketIOThread();
        new Thread(socketIOThread).start();

        System.out.println("SocketIO server started!");
    }

    /**
     * Get singleton SLBridge instance.
     * @return instance.
     */
    public static SLBridge getInstance() {
        return instance;
    }

    /**
     * Get channel addons.
     * Essentially allow you to do whatever you like with info from predefined channels.
     *
     * @return list of addons.
     */
    public Set<Addon> getAddons() {
        return addons;
    }

    /**
     * Get the SocketIO thread.
     * @return socketio thread, shouldn't be null.
     */
    public SocketIOThread getSocketIOThread() {
        return socketIOThread;
    }

    /**
     * Get the session manager.
     * Used for player sessions.
     *
     * @return session manager instance. Shouldn't be null unless something went wrong upon startup.
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Load all channel addons.
     */
    private void loadAddons() {
        ReflectionUtil.find("com.spleefleague.bridge.addons", this.getClass(), null).forEach((Class<?> clazz) -> {
            try {
                addons.add((Addon) clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        try {
            new SLBridge();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
