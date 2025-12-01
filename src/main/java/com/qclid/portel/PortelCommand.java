package com.qclid.portel;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

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
            case "whitelist":
                handleWhitelist(sender, args);
                break;
            case "blacklist":
                handleBlacklist(sender, args);
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

    private void handleWhitelist(CommandSender sender, String[] args) {
        if (!sender.hasPermission("portel.admin")) {
            chatStyler.sendMessage(sender, "You don't have permission to do that.");
            return;
        }

        if (args.length < 2) {
            chatStyler.sendMessage(sender, "Usage: /portel whitelist <add/remove/list/on/off> [ip]");
            return;
        }

        String action = args[1].toLowerCase();
        List<String> ipList = plugin.getConfig().getStringList("ip_list");

        switch (action) {
            case "add":
                if (args.length < 3) {
                    chatStyler.sendMessage(sender, "Usage: /portel whitelist add <ip>");
                    return;
                }
                String ipToAdd = args[2];
                if (!ipList.contains(ipToAdd)) {
                    ipList.add(ipToAdd);
                    plugin.getConfig().set("ip_list", ipList);
                    plugin.saveConfig();
                    chatStyler.sendMessage(sender, "Added " + ipToAdd + " to the IP list.");
                } else {
                    chatStyler.sendMessage(sender, ipToAdd + " is already in the IP list.");
                }
                break;
            case "remove":
                if (args.length < 3) {
                    chatStyler.sendMessage(sender, "Usage: /portel whitelist remove <ip>");
                    return;
                }
                String ipToRemove = args[2];
                if (ipList.contains(ipToRemove)) {
                    ipList.remove(ipToRemove);
                    plugin.getConfig().set("ip_list", ipList);
                    plugin.saveConfig();
                    chatStyler.sendMessage(sender, "Removed " + ipToRemove + " from the IP list.");
                } else {
                    chatStyler.sendMessage(sender, ipToRemove + " is not in the IP list.");
                }
                break;
            case "list":
                chatStyler.sendMessage(sender, "IP List: " + String.join(", ", ipList));
                break;
            case "on":
                plugin.getConfig().set("is_whitelist_on", true);
                plugin.saveConfig();
                chatStyler.sendMessage(sender, "Whitelist mode enabled. Only IPs in the list can access the site.");
                break;
            case "off":
                plugin.getConfig().set("is_whitelist_on", false);
                plugin.saveConfig();
                chatStyler.sendMessage(sender, "Whitelist mode disabled. IPs in the list are now BLOCKED (Blacklist mode).");
                break;
            default:
                chatStyler.sendMessage(sender, "Usage: /portel whitelist <add/remove/list/on/off> [ip]");
                break;
        }
    }
    
    private void handleBlacklist(CommandSender sender, String[] args) {
         if (!sender.hasPermission("portel.admin")) {
            chatStyler.sendMessage(sender, "You don't have permission to do that.");
            return;
        }
        // Blacklist is just an alias for managing the same list but likely intending to turn whitelist OFF
        chatStyler.sendMessage(sender, "Note: Portel uses a single IP list. Use '/portel whitelist off' to treat this list as a blacklist.");
        handleWhitelist(sender, args);
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
        chatStyler.sendMessage(
            sender,
            "/portel whitelist <add/remove/list/on/off> - Manage IP access."
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