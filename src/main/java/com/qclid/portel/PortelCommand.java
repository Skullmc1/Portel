package com.qclid.portel;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PortelCommand implements CommandExecutor {

    private final Portel plugin;

    public PortelCommand(Portel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("restart")) {
            if (sender.hasPermission("portel.restart")) {
                plugin.restartWebServer();
                sender.sendMessage("Portel web server restarted.");
            } else {
                sender.sendMessage("You do not have permission to do that.");
            }
            return true;
        }
        return false;
    }
}
