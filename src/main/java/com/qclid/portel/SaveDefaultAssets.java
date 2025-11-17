package com.qclid.portel;

import org.bukkit.plugin.java.JavaPlugin;

public class SaveDefaultAssets {

    private final JavaPlugin plugin;

    public SaveDefaultAssets(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void save() {
        plugin.saveDefaultConfig();
        plugin.saveResource("web/index.html", false);
        plugin.saveResource("web/script.js", false);
        plugin.saveResource("web/assets/favicon.ico", false);
        plugin.saveResource("web/assets/logo.png", false);
        plugin.saveResource("web/fonts/Unbounded.ttf", false);
        plugin.saveResource("web/fonts/Minecraft.otf", false);
        plugin.saveResource("ips.yml", false);
        plugin.saveResource("web/error-pages/403.html", false);
        plugin.saveResource("web/error-pages/404.html", false);
        plugin.saveResource("web/error-pages/429.html", false);
    }
}
