package com.qclid.portel;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PortelCommand implements CommandExecutor {

    private final Portel plugin;
    private final WebServerManager webServerManager;
    private final ChatStyler chatStyler;

    public PortelCommand(Portel plugin, WebServerManager webServerManager) {
        this.plugin = plugin;
        this.webServerManager = webServerManager;
        this.chatStyler = new ChatStyler(plugin);
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "restart":
                handleRestart(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                chatStyler.sendMessage(
                    sender,
                    "Unknown command. Use /portel help for a list of commands."
                );
                break;
        }

        return true;
    }

    private void handleRestart(CommandSender sender) {
        if (sender.hasPermission("portel.restart")) {
            webServerManager.restart();
            chatStyler.sendMessage(
                sender,
                "Web server restarted successfully."
            );
        } else {
            chatStyler.sendMessage(
                sender,
                "You don't have permission to do that."
            );
        }
    }

    private void handleReload(CommandSender sender) {
        if (sender.hasPermission("portel.reload")) {
            plugin.reload();
            chatStyler.sendMessage(
                sender,
                "Configuration reloaded successfully."
            );
        } else {
            chatStyler.sendMessage(
                sender,
                "You don't have permission to do that."
            );
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        chatStyler.sendMessage(sender, "--------------------------------");
        chatStyler.sendMessage(
            sender,
            "Portel v" + plugin.getDescription().getVersion()
        );
        chatStyler.sendMessage(sender, "");
        chatStyler.sendMessage(
            sender,
            "/portel help - Shows this help message."
        );
        chatStyler.sendMessage(
            sender,
            "/portel restart - Restarts the web server."
        );
        chatStyler.sendMessage(
            sender,
            "/portel reload - Reloads the configuration."
        );
        chatStyler.sendMessage(sender, "");
        chatStyler.sendMessage(
            sender,
            "For more help, visit our GitHub repository:"
        );
        chatStyler.sendMessage(sender, "github.com/Skullmc1/Portel");
        chatStyler.sendMessage(sender, "--------------------------------");
    }
}
