package com.mcgeo.auth.handlers;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mcgeo.auth.utils.UserList;

public class LoginHandler implements CommandExecutor {
    private Map<String, Integer> failedAttempts = new HashMap<>();
    Plugin plugin;
    SaveUsers saveUsers;
    private UserList userList;
    private Database database;

    public LoginHandler(Plugin plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase(); // Initialize the database
        File dataFile = new File(plugin.getDataFolder(), "data.json");
        this.saveUsers = new SaveUsers(dataFile, database);
        this.userList = new UserList(plugin, database); // Updated to match constructor
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("bePlayer"));
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("login")) {
            if (args.length < 1) {
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("loginUsage"));
                return true;
            }

            String username = player.getName();
            String passwordArgs = args[0];

            try {
                List<User> users = userList.readUsers();

                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        if (user.isActive()) {
                            player.sendMessage(
                                    SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("alreadyLoggedIn"));
                            return true;
                        }

                        // Check IP if security is enabled
                        if (user.isSecurity()) {
                            String playerIp = null;
                            try {
                                playerIp = EncryptionUtils.hashSHA256(getCurrentIpAddress());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                            // Login successful, update user's status and last join timestamp
                            user.setActive(true);

                            // Create a session for the user
                            SessionHandler session = new SessionHandler(player, user);
                            plugin.getSessionManager().addSession(player, session);

                            Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
                            user.setLastJoin(timeStamp);

                            // Update user's data in the database
                            database.updateUser(user);

                            failedAttempts.remove(username); // Reset failed attempts on successful login

                            player.sendMessage(
                                    SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("loginSuccess"));
                            return true;
                        } else {
                            handleFailedAttempt(player);
                            return true;
                        }
                    }
                }

                // User not found
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("userNotFound"));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("loginError"));
                return true;
            }
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
