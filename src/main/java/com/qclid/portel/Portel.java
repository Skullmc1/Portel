package com.qclid.portel;

import java.io.IOException;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class Portel extends JavaPlugin {

    private WebServerManager webServerManager;
    private WebSocketManager wsManager;
    private FileWatcher fileWatcher;
    private ConsoleLogger consoleLogger;
    private BukkitAudiences adventure;
    private ApiRegistry apiRegistry;
    private DashboardService dashboard;

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

        apiRegistry = new ApiRegistry(consoleLogger);
        getServer().getServicesManager().register(
            com.qclid.portel.api.PortelAPI.class,
            apiRegistry,
            this,
            ServicePriority.Normal
        );

        webServerManager = new WebServerManager(
            this,
            consoleLogger,
            ipLogger,
            rateLimiter,
            errorPageHandler,
            placeholderHook,
            apiRegistry
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
            consoleLogger.warning(
                "Could not start the web server on port " + getConfig().getInt("port") +
                " (" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()) + "). " +
                "Is another program using that port? Change 'port' in config.yml."
            );
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        int wsPort = getConfig().getInt("websocket-port", getConfig().getInt("port") + 1);
        wsManager = new WebSocketManager(this, wsPort);
        wsManager.start();
        
        if (getConfig().getBoolean("dashboard.enabled", true)) {
            dashboard = new DashboardService(this, apiRegistry);
            dashboard.start();
            consoleLogger.info("Live dashboard enabled at /dashboard.html");
        }

        // Remove API routes of plugins that get disabled
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPluginDisable(PluginDisableEvent event) {
                if (event.getPlugin() != Portel.this) {
                    apiRegistry.unregisterAll(event.getPlugin());
                }
            }
        }, this);

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
        if (dashboard != null) {
            dashboard.stop();
        }
        getServer().getServicesManager().unregisterAll(this);
        if (webServerManager != null) {
            webServerManager.stop();
        }
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