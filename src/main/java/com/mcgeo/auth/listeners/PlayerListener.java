package com.mcgeo.auth.listeners;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.db.Database;
import com.mcgeo.auth.models.SessionHandler;
import com.mcgeo.auth.models.User;
import com.mcgeo.auth.utils.EncryptionUtils;
import com.mcgeo.auth.utils.SettingsUtil;
import com.mcgeo.auth.utils.UserList;

public class PlayerListener implements Listener {
    private Plugin plugin;
    private Map<String, BukkitTask> kickTasks = new HashMap<>();
    private Database database;
    private UserList userList;
    private SessionHandler sessionHandler;

    public PlayerListener(Plugin plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        this.userList = new UserList(plugin, database);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String username = player.getName();
                    List<User> users;
                    try {
                        users = userList.readUsers();
                        boolean userExists = false;

                        for (User user : users) {
                            if (user.getUsername().equals(username)) {
                                userExists = true;
                                break;
                            }
                        }

                        if (!userExists) {
                            player.sendMessage(
                                    SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("mustRegister"));
                        } else if (!isActive(player)) {
                            player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("mustLogin"));
                        }
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 80); // 2 seconds (20 ticks per second)
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();
        List<User> users;
        try {
            users = userList.readUsers();
            for (User user : users) {
                if (user.getUsername().equals(username) && user.isActive()) {
                    user.setActive(false);
                    database.updateUser(user); // Update user status in the database
                    break;
                }
            }
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    try {
                        String playerIp = EncryptionUtils.hashSHA256(getCurrentIpAddress());
                        if (user.isSecurity() && !user.getIpAddress().equals(playerIp)) {
                            player.kickPlayer(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("ipMismatch"));
                            return;
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error getting IP address: " + e.getMessage());
                    }
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    scheduleKickTask(username);
                }
            }.runTaskLater(plugin, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();
        User user = sessionHandler.getUser();

        if (user.getUsername().equals(username) && user.isActive()) {
            user.setActive(false);
            database.updateUser(user); // Update user status in the database
        }
        cancelKickTask(username);
    }

    private void scheduleKickTask(String playerName) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(playerName);
                try {
                    if (player != null && !isActive(player)) {
                        player.kickPlayer(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("kickMessage")
                                .replace("%kickTime%", String.valueOf(SettingsUtil.KickTime)));
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(plugin, SettingsUtil.KickTime * 20); // Convert seconds to ticks
        kickTasks.put(playerName, task);
    }

    private void cancelKickTask(String playerName) {
        BukkitTask task = kickTasks.remove(playerName);
        if (task != null) {
            task.cancel();
        }
    }

    private boolean isActive(Player player) throws IOException, SQLException {
        String username = player.getName();
        List<User> users = userList.readUsers();

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user.isActive();
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
}
