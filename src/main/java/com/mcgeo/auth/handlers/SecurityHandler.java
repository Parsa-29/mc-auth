package com.mcgeo.auth.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.db.Database;
import com.mcgeo.auth.models.SessionHandler;
import com.mcgeo.auth.models.User;
import com.mcgeo.auth.utils.SaveUsers;
import com.mcgeo.auth.utils.SettingsUtil;

import java.io.File;

public class SecurityHandler implements CommandExecutor {
    private Plugin plugin;
    SaveUsers saveUsers;
    private Database database;
    private SessionHandler sessionHandler;

    public SecurityHandler(Plugin plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase(); // Assign the database field
        File dataFile = new File(plugin.getDataFolder(), "data.json");
        this.saveUsers = new SaveUsers(dataFile, database);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("security") && sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length < 1) {
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("securityUsage"));
                return true;
            }

            String option = args[0];
            User user = sessionHandler.getUser();
            if (user.getUsername().equals(player.getName())) {
                if (option.equalsIgnoreCase("on")) {
                    user.setSecurity(true);
                    player.sendMessage(
                            SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("securityEnabled"));
                } else if (option.equalsIgnoreCase("off")) {
                    user.setSecurity(false);
                    player.sendMessage(
                            SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("securityDisabled"));
                } else {
                    player.sendMessage(
                            SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("securityUsage"));
                    return true;
                }

                database.updateUser(user);
                return true;
            }
            player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("userNotFound"));
            return true;
        }
        return false;
    }
}
