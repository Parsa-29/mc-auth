package com.mcgeo.auth.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.classes.User;

public class UserList {
    private File dataFile;

    public UserList(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.dataFile = new File(plugin.getDataFolder(), "data.json");
    }

    public List<User> readUsers() throws IOException {
        if (dataFile.length() == 0) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        FileReader reader = new FileReader(dataFile);
        List<User> users = gson.fromJson(reader, new TypeToken<List<User>>() {
        }.getType());
        reader.close();
        return users;
    }

    public User getUser(String username) {
        // use User.getUser() to get user by username
        List<User> users = new ArrayList<>();
        try {
            users = readUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
