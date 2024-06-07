package com.mcgeo.auth;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcgeo.auth.handlers.RegisterHandler;
import com.mcgeo.auth.handlers.SecurityHandler;
import com.mcgeo.auth.listeners.PlayerListener;
import com.mcgeo.auth.utils.SettingsUtil;
import com.mcgeo.auth.handlers.ChangePassHandler;
import com.mcgeo.auth.handlers.ConfigHandler;
import com.mcgeo.auth.handlers.LoginHandler;

/*
 * auth java plugin
 */
public class Plugin extends JavaPlugin {
  public static final Logger LOGGER = Logger.getLogger("auth");
  private SettingsUtil settingsUtil;

  @Override
  public void onEnable() {
    LOGGER.info("auth enabled");
    saveDefaultConfig();
    settingsUtil = new SettingsUtil(this);
    settingsUtil.reloadSettings();

    getCommand("register").setExecutor(new RegisterHandler(this));
    getCommand("login").setExecutor(new LoginHandler(this));
    getCommand("security").setExecutor(new SecurityHandler(this));
    getCommand("config").setExecutor(new ConfigHandler(this));
    getCommand("changepass").setExecutor(new ChangePassHandler(this));
    getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    getServer().getPluginManager().registerEvents(new SettingsUtil(this), this);
  }

  public void onDisable() {
    LOGGER.info("auth disabled");
  }
}
