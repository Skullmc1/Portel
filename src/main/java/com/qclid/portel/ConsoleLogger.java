package com.qclid.portel;

import org.bukkit.plugin.java.JavaPlugin;

public class ConsoleLogger {

    private final JavaPlugin plugin;

    public ConsoleLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void info(String message) {
        if (plugin.getConfig().getBoolean("logging.console")) {
            plugin.getLogger().info(message);
        }
    }

    public void warning(String message) {
        if (plugin.getConfig().getBoolean("logging.console")) {
            plugin.getLogger().warning(message);
        }
    }
}
