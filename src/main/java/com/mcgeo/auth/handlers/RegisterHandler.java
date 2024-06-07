package com.mcgeo.auth.handlers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.net.InetSocketAddress;
import java.net.Socket;

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

public class RegisterHandler implements CommandExecutor {
    private File dataFile;
    Plugin plugin;
    SaveUsers saveUsers;
    public RegisterHandler(Plugin plugin) {
        this.plugin = plugin; // Assign plugin
        this.dataFile = new File(plugin.getDataFolder(), "data.json");
        this.saveUsers = new SaveUsers(new File(plugin.getDataFolder(), "data.json"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("register") && sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length < 2) {
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("registerUsage"));
                return true;
            }

            String passwordArgs = args[0];
            String confirmPassword = args[1];

            if (passwordArgs.length() < SettingsUtil.MIN_PASSWORD_LENGTH) {
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("passwordLength")
                        .replace("%minLength%", Integer.toString(SettingsUtil.MIN_PASSWORD_LENGTH)));
                return true;
            }

            if (!passwordArgs.equals(confirmPassword)) {
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("registerFail"));
                Plugin.LOGGER.warning(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("registerFail"));
                return true;
            }

            try {
                // Create data file if it doesn't exist
                dataFile.getParentFile().mkdirs();
                if (!dataFile.exists()) {
                    dataFile.createNewFile();
                }

                // Read existing users
                List<User> users = new UserList(plugin).readUsers();

                // Check if user already exists
                if (userExistsForRegister(player.getName())) {
                    player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("alreadyRegistered"));
                    return true;
                }

                // Hash the password
                String hashedPassword = EncryptionUtils.hashSHA256(passwordArgs);

                // save the ip address of the player
                String ipAddress = null;

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("google.com", 80));
                ipAddress = socket.getLocalAddress().toString().substring(1);
                String hashedIp = EncryptionUtils.hashSHA256(ipAddress);
                socket.close();
                // Create new user
                UUID uuid = player.getUniqueId();
                User newUser = new User(
                        uuid,
                        player.getName(),
                        hashedPassword,
                        true,
                        hashedIp,
                        false);

                // Add new user to the list
                users.add(newUser);

                // Save updated users list
                saveUsers.saveUsers(users);

                Plugin.LOGGER.info("Data saved to " + dataFile.getAbsolutePath());
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("registerSuccess"));
                Plugin.LOGGER.info("Account registered successfully for " + player.getName());
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("registerError"));
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("encryptionError"));
            }
            return true;
        }
        return false;
    }

    private boolean userExistsForRegister(String username) {
        List<User> users = null;
        try {
            users = new UserList(plugin).readUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (users != null) {
            for (User user : users) {
                if (user.getUsername().equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        return false;
    }
}
