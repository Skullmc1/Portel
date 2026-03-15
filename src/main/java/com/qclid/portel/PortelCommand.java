package com.qclid.portel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
            case "version":
                handleVersion(sender);
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

    private void handleVersion(CommandSender sender) {
        chatStyler.sendMessage(
            sender,
            "Running Portel v" + plugin.getDescription().getVersion()
        );
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
        String version = plugin.getDescription().getVersion();
        
        chatStyler.sendRawMessage(sender, "<gray>--------------------------------");
        
        // Title line
        chatStyler.sendMessage(sender, Component.text()
            .append(Component.text("Portel", TextColor.color(0x8A2BE2)).decorate(TextDecoration.BOLD))
            .append(Component.text(" v" + version, NamedTextColor.WHITE))
            .hoverEvent(HoverEvent.showText(Component.text("Click to check version", NamedTextColor.GRAY)))
            .clickEvent(ClickEvent.runCommand("/portel version"))
            .asComponent());

        chatStyler.sendMessage(sender, Component.empty());

        // Commands
        addCommandHelp(sender, "/portel help", "Shows this help message.", ClickEvent.suggestCommand("/portel help"));
        addCommandHelp(sender, "/portel restart", "Restarts the web server.", ClickEvent.runCommand("/portel restart"));
        addCommandHelp(sender, "/portel reload", "Reloads the configuration.", ClickEvent.runCommand("/portel reload"));
        addCommandHelp(sender, "/portel whitelist", "Manage IP access.", ClickEvent.suggestCommand("/portel whitelist "));

        chatStyler.sendMessage(sender, Component.empty());
        chatStyler.sendMessage(sender, Component.text("For more help, visit our GitHub repository:", NamedTextColor.WHITE));
        
        // GitHub Link
        chatStyler.sendMessage(sender, Component.text("github.com/Skullmc1/Portel", TextColor.color(0x8A2BE2))
            .hoverEvent(HoverEvent.showText(Component.text("Click to open GitHub", NamedTextColor.GRAY)))
            .clickEvent(ClickEvent.openUrl("https://github.com/Skullmc1/Portel")));

        chatStyler.sendRawMessage(sender, "<gray>--------------------------------");
    }

    private void addCommandHelp(CommandSender sender, String cmd, String desc, ClickEvent event) {
        chatStyler.sendMessage(sender, Component.text()
            .append(Component.text(cmd, NamedTextColor.WHITE))
            .append(Component.text(" - " + desc, NamedTextColor.GRAY))
            .hoverEvent(HoverEvent.showText(Component.text("Click to interact", NamedTextColor.GRAY)))
            .clickEvent(event)
            .asComponent());
    }
}