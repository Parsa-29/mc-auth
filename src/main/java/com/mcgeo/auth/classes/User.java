package com.mcgeo.auth.classes;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mcgeo.auth.utils.EncryptionUtils;

public class User {
    private String username;
    private String password;
    private UUID uuid;
    private boolean isActive;
    private String ipAddress;
    private boolean security;
    private String createdAt;
    private String lastJoin;

    public User(UUID uuid, String username, String password, boolean isActive, String ipAddress, boolean security,
            String timeStamp, String timeStamp2) {
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.isActive = isActive;
        this.ipAddress = ipAddress;
        this.security = false;
        this.createdAt = timeStamp;
        this.lastJoin = timeStamp2;
    }

    public String getUsername() {
        return username;
    }

    public Object getPassword() {
        return password;
    }

    public void setPassword(String password) {
        String newPassword = password;
        String hashedInputPassword = EncryptionUtils.hashSHA256(newPassword);
        this.password = hashedInputPassword;
    }

    public UUID getId() {
        return uuid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isSecurity() {
        return security;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLastJoin() {
        return lastJoin;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastJoin(String lastJoin) {
        this.lastJoin = lastJoin;
    }

    public List<String> getUser(String username) {
        List<String> user = new ArrayList<>();
        user.add(username);
        user.add(uuid.toString());
        user.add(String.valueOf(isActive));
        user.add(security ? "on" : "off");
        return user;
    }
}