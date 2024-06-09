package com.mcgeo.auth.utils;

import com.mcgeo.auth.db.Database;
import com.mcgeo.auth.models.User;
import com.mcgeo.auth.Plugin;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserList {
    private File dataFile;
    private Database database;

    public UserList(Plugin plugin, Database database) {
        this.dataFile = new File(plugin.getDataFolder(), "data.json");
        this.database = database;
    }

    public List<User> readUsers() throws IOException {
        if (database != null) {
            return readUsersFromDatabase();
        } else {
            return readUsersFromFile();
        }
    }

    private List<User> readUsersFromFile() throws IOException {
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

    public List<User> readUsersFromDatabase() {
        List<User> users = new ArrayList<>();

        try {
            // Some code that might throw an exception
            // For example, database operations that can throw SQLException
            Connection conn = database.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getBoolean("isActive"),
                        rs.getString("ipAddress"),
                        rs.getBoolean("security"),
                        rs.getTimestamp("createdAt"),
                        rs.getTimestamp("lastJoin"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getUser(String username) {
        List<User> users;
        try {
            users = readUsers();
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    return user;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // User not found
    }
}
