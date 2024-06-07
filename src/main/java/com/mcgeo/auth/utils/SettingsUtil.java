package com.mcgeo.auth.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.mcgeo.auth.Plugin;

public class SettingsUtil implements Listener {
    private final Plugin plugin;
    private File dataFile;
    public static String PREFIX = "";
    public static Map<String, String> TRANSLATED_STRINGS = new HashMap<>();
    public static int MAX_ATTEMPTS;
    public static int MIN_PASSWORD_LENGTH;
    public static int KickTime;

    public SettingsUtil(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.json");
        reloadSettings();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Create the data file if it doesn't exist
        dataFile.getParentFile().mkdirs();
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                FileWriter writer = new FileWriter(dataFile);
                writer.write("[]");
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Set the prefix
        SettingsUtil.PREFIX = this.plugin.getConfig().getString("Prefix");
        if (SettingsUtil.PREFIX == null) {
            System.out.println("PREFIX is null");
        } else {
            // System.out.println("PREFIX: " + SettingsUtil.PREFIX);
        }

        // Load translated strings
        for (String key : this.plugin.getConfig().getConfigurationSection("Translate").getKeys(false)) {
            String value = this.plugin.getConfig().getString("Translate." + key);
            if (value == null) {
                System.out.println("Translation for key " + key + " is null");
            } else {
                SettingsUtil.TRANSLATED_STRINGS.put(key, value);
                // System.out.println("Loaded translation: " + key + " = " + value);
            }
        }

        SettingsUtil.MAX_ATTEMPTS = this.plugin.getConfig().getInt("MaxAttempts");
        if (SettingsUtil.MAX_ATTEMPTS == 0) {
            System.out.println("MAX_ATTEMPTS is 0");
        } else {
            // System.out.println("MAX_ATTEMPTS: " + SettingsUtil.MAX_ATTEMPTS);
        }

        SettingsUtil.MIN_PASSWORD_LENGTH = this.plugin.getConfig().getInt("MinPasswordLength");
        if (SettingsUtil.MIN_PASSWORD_LENGTH == 0) {
            System.out.println("MIN_PASSWORD_LENGTH is 0");
        } else {
            // System.out.println("MIN_PASSWORD_LENGTH: " + SettingsUtil.MIN_PASSWORD_LENGTH);
        }

        SettingsUtil.KickTime = this.plugin.getConfig().getInt("KickTime");
        if (SettingsUtil.KickTime == 0) {
            System.out.println("KickTime is 0");
        } else {
            // System.out.println("KickTime: " + SettingsUtil.KickTime);
        }
    }

    public void reloadSettings() {
        FileConfiguration config = this.plugin.getConfig();

        // Set the prefix
        SettingsUtil.PREFIX = config.getString("Prefix", "§9[§bAuth§9]§c ");

        // Load translated strings
        SettingsUtil.TRANSLATED_STRINGS.clear();
        for (String key : config.getConfigurationSection("Translate").getKeys(false)) {
            String value = config.getString("Translate." + key, "");
            SettingsUtil.TRANSLATED_STRINGS.put(key, value);
        }

        // Load other settings
        SettingsUtil.MAX_ATTEMPTS = config.getInt("MaxAttempts", 3);
        SettingsUtil.MIN_PASSWORD_LENGTH = config.getInt("MinPasswordLength", 6);
        SettingsUtil.KickTime = config.getInt("KickTime", 15);
    }

}
