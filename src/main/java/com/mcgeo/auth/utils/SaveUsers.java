package com.mcgeo.auth.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.mcgeo.auth.db.Database;
import com.mcgeo.auth.models.User;

public class SaveUsers {
    private File dataFile;
    private Database database;
    private static final Logger logger = Logger.getLogger(SaveUsers.class.getName());

    public SaveUsers(File dataFile, Database database) {
        this.dataFile = dataFile;
        this.database = database;
    }

    public void saveUsers(List<User> users) throws IOException, SQLException {
        if (database.getConnection() != null) {
            saveUsersToDatabase(users);
        } else {
            saveUsersToFile(users);
        }
    }

    private void saveUsersToFile(List<User> users) throws IOException {
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(dataFile);
        gson.toJson(users, writer);
        writer.close();
    }

    private void saveUsersToDatabase(List<User> users) {
        String query = "INSERT INTO users (uuid, username, password, ipAddress, isActive, security, createdAt, lastJoin) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                     + "username=VALUES(username), password=VALUES(password), ipAddress=VALUES(ipAddress), "
                     + "isActive=VALUES(isActive), security=VALUES(security), createdAt=VALUES(createdAt), lastJoin=VALUES(lastJoin)";

        try (Connection conn = database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            for (User user : users) {
                pstmt.setString(1, user.getId().toString()); // Convert UUID to String
                pstmt.setString(2, user.getUsername());
                pstmt.setString(3, (String) user.getPassword()); // Ensure password is a String
                pstmt.setString(4, user.getIpAddress());
                pstmt.setBoolean(5, user.isActive());
                pstmt.setBoolean(6, user.isSecurity());
                pstmt.setString(7, user.getCreatedAt());
                pstmt.setString(8, user.getLastJoin());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save users to database.", e);
        }
    }
}
