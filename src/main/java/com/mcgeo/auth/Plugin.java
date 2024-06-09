package com.mcgeo.auth;

import java.sql.SQLException;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcgeo.auth.handlers.RegisterHandler;
import com.mcgeo.auth.handlers.SecurityHandler;
import com.mcgeo.auth.handlers.UserdataHandler;
import com.mcgeo.auth.listeners.PlayerListener;
import com.mcgeo.auth.utils.SessionManager;
import com.mcgeo.auth.utils.SettingsUtil;
import com.mcgeo.auth.db.Database;
import com.mcgeo.auth.handlers.ChangePassHandler;
import com.mcgeo.auth.handlers.ConfigHandler;
import com.mcgeo.auth.handlers.LoginHandler;

public class Plugin extends JavaPlugin {
    public static final Logger LOGGER = Logger.getLogger("auth");
    private SettingsUtil settingsUtil;
    private Database database;
    private SessionManager sessionManager;

    @Override
    public void onEnable() {
        LOGGER.info("auth enabled");

        saveDefaultConfig();

        settingsUtil = new SettingsUtil(this);
        settingsUtil.reloadSettings();

        sessionManager = new SessionManager();

        database = new Database(this);
        try {
            database.openConnection();
            database.createTable();
            if (database.isTableExists()) {
                LOGGER.info("[auth] Table 'users' exists.");
            } else {
                LOGGER.severe("[auth] Table 'users' does not exist after creation attempt.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getCommand("register").setExecutor(new RegisterHandler(this));
        getCommand("login").setExecutor(new LoginHandler(this));
        getCommand("security").setExecutor(new SecurityHandler(this));
        getCommand("config").setExecutor(new ConfigHandler(this));
        getCommand("changepass").setExecutor(new ChangePassHandler(this));
        getCommand("userdata").setExecutor(new UserdataHandler(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new SettingsUtil(this), this);
    }

    @Override
    public void onDisable() {
        LOGGER.info("auth disabled");
        if (database != null) {
            database.closeConnection();
        }
    }

    public Database getDatabase() {
        return database;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
