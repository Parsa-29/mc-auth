package com.mcgeo.auth.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.classes.User;
import com.mcgeo.auth.utils.SaveUsers;
import com.mcgeo.auth.utils.SettingsUtil;
import com.mcgeo.auth.utils.UserList;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SecurityHandler implements CommandExecutor {
    private Plugin plugin;
    SaveUsers saveUsers;
    public SecurityHandler(Plugin plugin) {
        this.plugin = plugin; // Assign plugin
        this.saveUsers = new SaveUsers(new File(plugin.getDataFolder(), "data.json"));
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
            try {
                List<User> users = new UserList(plugin).readUsers();

                for (User user : users) {
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

                        saveUsers.saveUsers(users);
                        return true;
                    }
                }

                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("userNotFound"));
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("errorOccurred"));
            }
            return true;
        }
        return false;
    }
}
