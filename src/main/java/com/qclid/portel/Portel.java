package com.qclid.portel;

import java.io.IOException;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class Portel extends JavaPlugin {

    private WebServerManager webServerManager;
    private ConsoleLogger consoleLogger;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        new SaveDefaultAssets(this).save();

        consoleLogger = new ConsoleLogger(this);
        IPLogger ipLogger = new IPLogger(this, consoleLogger);
        RateLimiter rateLimiter = new RateLimiter(this);
        ErrorPageHandler errorPageHandler = new ErrorPageHandler(this);

        webServerManager = new WebServerManager(
            this,
            consoleLogger,
            ipLogger,
            rateLimiter,
            errorPageHandler
        );

        try {
            webServerManager.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getCommand("portel").setExecutor(
            new PortelCommand(this, webServerManager)
        );
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        webServerManager.stop();
    }

    public void reload() {
        reloadConfig();
        consoleLogger.info("Configuration reloaded.");
        webServerManager.restart();
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException(
                "Tried to access Adventure when the plugin was disabled!"
            );
        }
        return this.adventure;
    }
}
