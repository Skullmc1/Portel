package com.qclid.portel;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PortelCommand implements CommandExecutor {

    private final Portel plugin;
    private final WebServerManager webServerManager;

    public PortelCommand(Portel plugin, WebServerManager webServerManager) {
        this.plugin = plugin;
        this.webServerManager = webServerManager;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("restart")) {
                if (sender.hasPermission("portel.restart")) {
                    webServerManager.restart();
                    sender.sendMessage("Portel web server restarted.");
                } else {
                    sender.sendMessage(
                        "You do not have permission to do that."
                    );
                }
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("portel.reload")) {
                    plugin.reload();
                    sender.sendMessage("Portel configuration reloaded.");
                } else {
                    sender.sendMessage(
                        "You do not have permission to do that."
                    );
                }
                return true;
            }
        }
        return false;
    }
}
