package com.mcgeo.auth.models;

import org.bukkit.entity.Player;

public class SessionHandler {
    private Player player;
    private User user;

    public SessionHandler(Player player, User user) {
        this.player = player;
        this.user = user;
    }

    public Player getPlayer() {
        return player;
    }

    public User getUser() {
        return user;
    }
}
