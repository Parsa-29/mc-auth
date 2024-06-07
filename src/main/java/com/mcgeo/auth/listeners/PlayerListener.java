package com.mcgeo.auth.listeners;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.classes.User;
import com.mcgeo.auth.utils.EncryptionUtils;
import com.mcgeo.auth.utils.SaveUsers;
import com.mcgeo.auth.utils.SettingsUtil;
import com.mcgeo.auth.utils.UserList;

public class PlayerListener implements Listener {
    private Plugin plugin;
    private Map<String, BukkitTask> kickTasks = new HashMap<>();
    private SaveUsers saveUsers;
    
    public PlayerListener(Plugin plugin) {
        this.plugin = plugin;
        // Initialize SaveUsers with a proper file
        this.saveUsers = new SaveUsers(new File(plugin.getDataFolder(), "data.json"));

        // Start the scheduler to prompt players to log in or register every 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Check if the player is username exists, write "/login <password>" to chat if
                    // not exist, and write "/register <password> <confirmPassword>" to chat if not
                    // exist
                    String username = player.getName();
                    List<User> users;
                    try {
                        users = new UserList(plugin).readUsers();
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 80); // 2 seconds (20 ticks per second)
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws IOException {
        String username = event.getPlayer().getName();
        List<User> users = new UserList(plugin).readUsers();

        for (User user : users) {
            if (user.getUsername().equals(username) && user.isActive()) {
                user.setActive(false);
                saveUsers.saveUsers(users);
                break;
            }
        }

        // Check if current IP address is the same as the one in the data file
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                try {
                    String playerIp = EncryptionUtils.hashSHA256(getCurrentIpAddress());
                    if (user.isSecurity() && !user.getIpAddress().equals(playerIp)) {
                        event.getPlayer()
                                .kickPlayer(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS
                                        .get("ipMismatch"));
                        return;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error getting IP address: " + e.getMessage());
                }
            }
        }

        // Schedule a task to kick the player if they don't log in within 15 seconds
        // scheduleKickTask(username);
        // add 0.5 seconds timeout before run function
        new BukkitRunnable() {
            @Override
            public void run() {
                scheduleKickTask(username);
            }
        }.runTaskLater(plugin, 5);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) throws IOException {
        String username = event.getPlayer().getName();
        List<User> users = new UserList(plugin).readUsers();

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                user.setActive(false);
                saveUsers.saveUsers(users);
                break;
            }
        }

        cancelKickTask(username);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws IOException {
        Player player = event.getPlayer();
        if (!isActive(player)) {
            event.setCancelled(true);
            player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("cannotMove"));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) throws IOException {
        Player player = event.getPlayer();
        if (!isActive(player)) {
            event.setCancelled(true);
            player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("cannotBreak"));
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) throws IOException {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!isActive(player)) {
                event.setCancelled(true);
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("cannotPickup"));
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws IOException {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        if (!isActive(player) && !message.startsWith("/login") && !message.startsWith("/register")) {
            event.setCancelled(true);
            player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("cannotCommand"));
        }
    }

    private void scheduleKickTask(String playerName) {
        final String finalPlayerName = playerName;
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(finalPlayerName);
                try {
                    if (player != null && !isActive(player)) {
                        player.kickPlayer(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("kickMessage")
                                .replace("%kickTime%", String.valueOf(SettingsUtil.KickTime)));
                    }
                } catch (IOException e) {
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

    private boolean isActive(Player player) throws IOException {
        String username = player.getName();
        List<User> users = new UserList(plugin).readUsers();

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
