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