package com.mcgeo.auth.classes;

import java.util.UUID;

import com.mcgeo.auth.utils.EncryptionUtils;

public class User {
    private String username;
    private String password;
    private UUID uuid;
    private boolean isActive;
    private String ipAddress;
    private boolean security;

    public User(UUID uuid, String username, String password, boolean isActive, String ipAddress, boolean security) {
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.isActive = isActive;
        this.ipAddress = ipAddress;
        this.security = false;

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
}