package com.mcgeo.auth.utils;

import com.mcgeo.auth.models.SessionHandler;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private Map<Player, SessionHandler> sessions = new HashMap<>();

    public void addSession(Player player, SessionHandler session) {
        sessions.put(player, session);
    }

    public void removeSession(Player player) {
        sessions.remove(player);
    }

    public SessionHandler getSession(Player player) {
        return sessions.get(player);
    }
}
