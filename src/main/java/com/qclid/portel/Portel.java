package com.qclid.portel;

import java.io.IOException;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Portel extends JavaPlugin {

    private WebServerManager webServerManager;
    private WebSocketManager wsManager;
    private FileWatcher fileWatcher;
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
        PlaceholderHook placeholderHook = new PlaceholderHook();

        if (placeholderHook.isEnabled()) {
            consoleLogger.info("PlaceholderAPI found and hooked!");
        } else {
            consoleLogger.warning("PlaceholderAPI not found. Placeholders will not be parsed.");
        }

        webServerManager = new WebServerManager(
            this,
            consoleLogger,
            ipLogger,
            rateLimiter,
            errorPageHandler,
            placeholderHook
        );

        // Cleanup rate limiter every minute to prevent memory leak
        getServer().getScheduler().runTaskTimerAsynchronously(this, rateLimiter::cleanup, 1200L, 1200L);

        try {
            webServerManager.start();
            
            // Start FileWatcher for hot-reloading if enabled
            if (getConfig().getBoolean("hot-reloading", true)) {
                java.nio.file.Path webPath = getDataFolder().toPath().resolve("web");
                if (java.nio.file.Files.exists(webPath)) {
                    fileWatcher = new FileWatcher(this, webServerManager, webPath);
                    getServer().getScheduler().runTaskAsynchronously(this, fileWatcher);
                    consoleLogger.info("Hot-reloading enabled for web directory.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int wsPort = getConfig().getInt("websocket-port", getConfig().getInt("port") + 1);
        wsManager = new WebSocketManager(this, wsPort);
        wsManager.start();
        
        getServer().getPluginManager().registerEvents(new ChatListener(wsManager), this);
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onServerLoad(ServerLoadEvent event) {
                String protocol = getConfig().getBoolean("ssl.enabled") ? "https" : "http";
                getLogger().info("Website started at: " + protocol + "://localhost:" + getConfig().getInt("port"));
            }
        }, this);

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
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
        webServerManager.stop();
        if (wsManager != null) {
            try {
                wsManager.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        reloadConfig();
        consoleLogger.info("Configuration reloaded.");
        webServerManager.restart();

        if (fileWatcher != null) {
            fileWatcher.stop();
            fileWatcher = null;
        }

        if (getConfig().getBoolean("hot-reloading", true)) {
            java.nio.file.Path webPath = getDataFolder().toPath().resolve("web");
            if (java.nio.file.Files.exists(webPath)) {
                try {
                    fileWatcher = new FileWatcher(this, webServerManager, webPath);
                    getServer().getScheduler().runTaskAsynchronously(this, fileWatcher);
                    consoleLogger.info("Hot-reloading re-enabled.");
                } catch (IOException e) {
                    consoleLogger.warning("Failed to restart FileWatcher: " + e.getMessage());
                }
            }
        }
        
        if (wsManager != null) {
             try {
                wsManager.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int wsPort = getConfig().getInt("websocket-port", getConfig().getInt("port") + 1);
            wsManager = new WebSocketManager(this, wsPort);
            wsManager.start();
        }
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException(
                "Tried to access Adventure when the plugin was disabled!"
            );
        }
        return this.adventure;
    }

    public ConsoleLogger getConsoleLogger() {
        return consoleLogger;
    }
}