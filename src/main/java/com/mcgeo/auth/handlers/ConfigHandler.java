package com.mcgeo.auth.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcgeo.auth.Plugin;
import com.mcgeo.auth.utils.SettingsUtil;

public class ConfigHandler implements CommandExecutor {
    private Player player;

    public ConfigHandler(Plugin plugin) {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("config") && sender instanceof Player) {
            // check if player is op
            if (((Player) sender).isOp()) {
                // log all the settings
                Player player = (Player) sender;
                player.sendMessage(SettingsUtil.PREFIX + "Prefix: " + SettingsUtil.PREFIX);
                player.sendMessage(SettingsUtil.PREFIX + "Max Failed Attempts: " + SettingsUtil.MAX_ATTEMPTS);
                player.sendMessage(SettingsUtil.PREFIX + "Min Password Length: " + SettingsUtil.MIN_PASSWORD_LENGTH);
                player.sendMessage(SettingsUtil.PREFIX + "Kick Time: " + SettingsUtil.KickTime);
                return true;
            } else {
                player.sendMessage(SettingsUtil.PREFIX + SettingsUtil.TRANSLATED_STRINGS.get("noPermission"));
                return true;
            }
        }
        return false;
    }
}
