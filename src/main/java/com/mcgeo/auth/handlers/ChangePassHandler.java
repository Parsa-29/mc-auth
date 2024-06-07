package com.mcgeo.auth.handlers;

import java.io.File;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.classes.User;
import com.mcgeo.auth.utils.EncryptionUtils;
import com.mcgeo.auth.utils.SaveUsers;
import com.mcgeo.auth.utils.SettingsUtil;
import com.mcgeo.auth.utils.UserList;

public class ChangePassHandler implements CommandExecutor {
    Plugin plugin;
    SaveUsers saveUsers;

    public ChangePassHandler(Plugin plugin) {
        this.plugin = plugin; // Assign plugin
        this.saveUsers = new SaveUsers(new File(plugin.getDataFolder(), "data.json"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("changepass") && sender instanceof Player) {
            Player player = (Player) sender;
            String username = player.getName();
            List<User> users;

            if (args.length < 3) {
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("changePassUsage"));
                return true;
            }

            try {
                users = new UserList(plugin).readUsers();
                for (User user : users) {
                    if (user.isActive()) {
                        if (user.getUsername().equals(username)) {
                            try {
                                String password = args[0];
                                String hashedPassword = EncryptionUtils.hashSHA256(password);
                                String password2 = args[1];
                                String password3 = args[2];
                                Object currentPassword = user.getPassword();
                                // check if the current password is correct
                                if (currentPassword.equals(hashedPassword)) {
                                    // check if the new passwords match
                                    if (password2.equals(password3)) {
                                        user.setPassword(password2);
                                        saveUsers.saveUsers(users);
                                        player.sendMessage(SettingsUtil.PREFIX
                                                + SettingsUtil.TRANSLATED_STRINGS.get("passwordChanged"));
                                        return true;
                                    } else {
                                        player.sendMessage(SettingsUtil.PREFIX
                                                + SettingsUtil.TRANSLATED_STRINGS.get("passwordsDoNotMatch"));
                                        return true;
                                    }
                                } else {
                                    player.sendMessage(SettingsUtil.PREFIX
                                            + SettingsUtil.TRANSLATED_STRINGS.get("incorrectPasswordChange"));
                                    return true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("notLoggedIn"));
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    };
}
