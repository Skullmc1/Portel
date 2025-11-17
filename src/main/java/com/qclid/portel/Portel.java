package com.qclid.portel;

import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

public final class Portel extends JavaPlugin {

    private WebServerManager webServerManager;
    private ConsoleLogger consoleLogger;

    @Override
    public void onEnable() {
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
        webServerManager.stop();
    }

    public void reload() {
        reloadConfig();
        consoleLogger.info("Configuration reloaded.");
        webServerManager.restart();
    }
}
