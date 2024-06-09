package com.mcgeo.auth.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.models.User;

public class Database {
    private Connection connection;
    @SuppressWarnings("unused")
    private Plugin plugin;
    private FileConfiguration config;
    private static final Logger logger = Logger.getLogger(Database.class.getName());

    private String host;
    private int port;
    private String database;
    private String user;
    private String password;

    public Database(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        // Initialize configuration fields
        this.host = config.getString("database.host");
        this.port = config.getInt("database.port");
        this.database = config.getString("database.name");
        this.user = config.getString("database.user");
        this.password = config.getString("database.password");
    }

    public void openConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        try {
            if (connection != null && !connection.isClosed()) {
                logger.log(Level.WARNING, "[auth] Database connection is already open.");
                return;
            }

            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    logger.log(Level.WARNING, "[auth] Database connection is already open.");
                    return;
                }

                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, password);
                logger.log(Level.INFO, "[auth] Connected to the database successfully.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[auth] Failed to connect to the database.", e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "[auth] JDBC Driver not found.", e);
        }
    }

    public void updateUser(User user) {
        String query = "UPDATE users SET password = ?, isActive = ?, security = ?, lastJoin = ? WHERE username = ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, user.getPassword());
            pstmt.setBoolean(2, user.isActive());
            pstmt.setBoolean(3, user.isSecurity());
            pstmt.setString(4, user.getLastJoin());
            pstmt.setString(5, user.getUsername());
            pstmt.executeUpdate();
            logger.log(Level.INFO, "[auth] User '" + user.getUsername() + "' updated successfully.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[auth] Failed to update user '" + user.getUsername() + "'.", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            openConnection();
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.log(Level.INFO, "[auth] Database connection closed successfully.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[auth] Failed to close the database connection.", e);
        }
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "uuid VARCHAR(255), "
                + "username VARCHAR(255), "
                + "password VARCHAR(255), "
                + "ipAddress VARCHAR(255), "
                + "isActive BOOLEAN, "
                + "security BOOLEAN, "
                + "createdAt VARCHAR(255), "
                + "lastJoin VARCHAR(255))";
        try (Connection conn = getConnection();
                java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            logger.log(Level.INFO, "[auth] Table 'users' created or already exists.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[auth] Failed to create table 'users'.", e);
        }
    }
}
