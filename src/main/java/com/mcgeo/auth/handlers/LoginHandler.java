package com.mcgeo.auth.handlers;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class LoginHandler implements CommandExecutor {
    private Map<String, Integer> failedAttempts = new HashMap<>();
    Plugin plugin;
    SaveUsers saveUsers;

    public LoginHandler(Plugin plugin) {
        this.plugin = plugin; // Assign plugin
        this.saveUsers = new SaveUsers(new File(plugin.getDataFolder(), "data.json"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("login") && sender instanceof Player) {

            if (args.length < 1) {
                sender.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("loginUsage"));
                return true;
            }

            Player player = (Player) sender;
            String username = player.getName();
            String passwordArgs = args[0];
            List<User> users;
            try {
                users = new UserList(plugin).readUsers();
                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        try {

                            if (user.isActive()) {
                                player.sendMessage(
                                        SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("alreadyLoggedIn"));
                                return true;
                            }
                            // Check IP if security is enabled
                            if (user.isSecurity()) {
                                String playerIp = EncryptionUtils.hashSHA256(getCurrentIpAddress());
                                if (!user.getIpAddress().equals(playerIp)) {
                                    String message = SettingsUtil.TRANSLATED_STRINGS.get("ipMismatch");
                                    player.sendMessage(SettingsUtil.PREFIX + message);
                                    return true;
                                }
                            }
                            // Hash the provided password
                            String hashedInputPassword = EncryptionUtils.hashSHA256(passwordArgs);

                            // Compare hashed passwords
                            if (user.getPassword().equals(hashedInputPassword)) {
                                player.sendMessage(
                                        SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("loginSuccess"));
                                user.setActive(true);
                                // Save the updated user list
                                saveUsers.saveUsers(users);
                                failedAttempts.remove(username); // Reset failed attempts on successful login
                                return true;
                            } else {
                                handleFailedAttempt(player);
                                return true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("loginError"));
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("userNotFound"));
            return true;
        } else {
            sender.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("bePlayer"));
        }
        return false;
    }

    private String getCurrentIpAddress() throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String ipAddress = socket.getLocalAddress().toString().substring(1);
        socket.close();
        return ipAddress;
    }

    private void handleFailedAttempt(Player player) {
        String username = player.getName();
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);

        if (attempts >= SettingsUtil.MAX_ATTEMPTS) {
            player.kickPlayer(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("tooManyAttempts"));
            failedAttempts.remove(username); // Reset attempts after kicking the player
        } else {
            player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("incorrectPassword")
                    .replace("%attempt%", String.valueOf(attempts))
                    .replace("%maxAttempts%", String.valueOf(SettingsUtil.MAX_ATTEMPTS)));
        }
    }
}
