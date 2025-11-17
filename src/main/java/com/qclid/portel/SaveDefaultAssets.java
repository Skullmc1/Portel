package com.qclid.portel;

import org.bukkit.plugin.java.JavaPlugin;

public class SaveDefaultAssets {

    private final JavaPlugin plugin;

    public SaveDefaultAssets(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void save() {
        plugin.saveDefaultConfig();
        plugin.saveResource("web/index.html", true);
        plugin.saveResource("web/script.js", true);
        plugin.saveResource("web/assets/favicon.ico", true);
        plugin.saveResource("web/assets/logo.png", true);
        plugin.saveResource("web/fonts/Unbounded.ttf", true);
        plugin.saveResource("web/fonts/Minecraft.otf", true);
        plugin.saveResource("ips.yml", true);
        plugin.saveResource("web/error-pages/403.html", true);
        plugin.saveResource("web/error-pages/404.html", true);
        plugin.saveResource("web/error-pages/429.html", true);
    }
}
