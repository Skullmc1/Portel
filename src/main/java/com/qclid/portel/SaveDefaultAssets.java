package com.qclid.portel;

import org.bukkit.plugin.java.JavaPlugin;

public class SaveDefaultAssets {

    private final JavaPlugin plugin;

    public SaveDefaultAssets(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void save() {
        plugin.saveDefaultConfig();
        
        String[] resources = {
            "web/index.html", "web/script.js", "web/assets/favicon.ico",
            "web/assets/logo.png", "web/fonts/Unbounded.ttf", "web/fonts/Minecraft.otf",
            "ips.log", "web/error-pages/403.html", "web/error-pages/404.html",
            "web/error-pages/429.html"
        };

        for (String resource : resources) {
            java.io.File file = new java.io.File(plugin.getDataFolder(), resource);
            if (!file.exists()) {
                plugin.saveResource(resource, false);
            }
        }
    }
}
