package com.mcgeo.auth.handlers;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.classes.User;
import com.mcgeo.auth.utils.SaveUsers;
import com.mcgeo.auth.utils.SettingsUtil;
import com.mcgeo.auth.utils.UserList;

public class UserdataHandler implements CommandExecutor {
    Plugin plugin;
    SaveUsers saveUsers;

    public UserdataHandler(Plugin plugin) {
        this.plugin = plugin; // Assign plugin
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("userdata") && sender instanceof Player) {
            // get current user full data
            // /userdata <username>
            Player player = (Player) sender;
            String username = args[0];
            if (player.isOp()) {
                if (args.length < 1) {
                    player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("userDataUsage"));
                    return true;
                }
                UserList userList = new UserList(plugin);
                try {
                    userList.readUsers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                User user = userList.getUser(username);
                if (user != null) {
                    player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("userData"));
                    player.sendMessage("§7====================================");
                    player.sendMessage("§bUUID: §6" + user.getId());
                    player.sendMessage("§bUsername: §6" + user.getUsername());
                    player.sendMessage("§bActive: §6" + user.isActive());
                    player.sendMessage("§bSecurity: §6" + (user.isSecurity() ? "on" : "off"));
                    player.sendMessage("§bCreatedAt: §6" + user.getCreatedAt());
                    player.sendMessage("§bLastJoin: §6" + user.getLastJoin());
                    return true;
                }
            } else {
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("noPermission"));
                return true;
            }
        }

        return false;
    }
}
