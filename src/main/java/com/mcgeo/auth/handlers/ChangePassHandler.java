package com.mcgeo.auth.handlers;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.db.Database;
import com.mcgeo.auth.models.SessionHandler;
import com.mcgeo.auth.models.User;
import com.mcgeo.auth.utils.EncryptionUtils;
import com.mcgeo.auth.utils.SaveUsers;
import com.mcgeo.auth.utils.SettingsUtil;

public class ChangePassHandler implements CommandExecutor {
    Plugin plugin;
    SaveUsers saveUsers;
    private Database database;
    private SessionHandler sessionHandler;

    public ChangePassHandler(Plugin plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase(); // Assign the database field
        File dataFile = new File(plugin.getDataFolder(), "data.json");
        this.saveUsers = new SaveUsers(dataFile, database);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("changepass") && sender instanceof Player) {
            Player player = (Player) sender;
            String username = player.getName();
            if (player.isOp()) {
                if (args.length < 4) {
                    player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("changePassUsageOP"));
                    return true;
                }

                // /changepass <username> <oldpassword> <newpassword> <newpassword>
                User user = sessionHandler.getUser();
                try {
                    if (user.isActive()) {
                        if (user.getUsername().equals(args[0])) {
                            try {
                                String password = args[1];
                                String hashedPassword = EncryptionUtils.hashSHA256(password);
                                String password2 = args[2];
                                String password3 = args[3];
                                Object currentPassword = user.getPassword();
                                // check if the current password is correct
                                if (currentPassword.equals(hashedPassword)) {
                                    // check if the new passwords match
                                    if (password2.equals(password3)) {
                                        user.setPassword(password2);
                                        database.updateUser(user);
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
                        } else {
                            player.sendMessage(
                                    SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("userNotFound"));
                            return true;
                        }
                    } else {
                        player.sendMessage(
                                SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("notLoggedIn"));
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (args.length < 3) {
                    player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("changePassUsage"));
                    return true;
                }

                try {
                    User user = sessionHandler.getUser();

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
                                        database.updateUser(user);
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
                        player.sendMessage(
                                SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("notLoggedIn"));
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    };
}
