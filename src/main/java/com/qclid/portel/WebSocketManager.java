package com.qclid.portel;

import com.google.gson.Gson;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

public class WebSocketManager extends WebSocketServer {

    private final Portel plugin;
    private final Gson gson = new Gson();

    public WebSocketManager(Portel plugin, int port) {
        super(new InetSocketAddress(port));
        this.plugin = plugin;
        configureSSL();
    }

    private void configureSSL() {
        if (plugin.getConfig().getBoolean("ssl.enabled")) {
            try {
                SSLContext sslContext = SSLUtils.createSSLContext(plugin.getDataFolder(), plugin.getConfig());
                this.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
                plugin.getConsoleLogger().info("WebSocket configured for WSS (Secure WebSocket).");
            } catch (Exception e) {
                 plugin.getConsoleLogger().warning("Failed to configure SSL for WebSocket: " + e.getMessage());
            }
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        plugin.getConsoleLogger().info("New WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        plugin.getConsoleLogger().info("Closed WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            boolean allowed = plugin.getConfig().getBoolean("websocket.allow-web-to-game-chat", true);
            if (!allowed) {
                return;
            }

            // Parse "User: Message" format
            String sender = "WebUser";
            String contentText = message;
            if (message.contains(": ")) {
                String[] parts = message.split(": ", 2);
                sender = parts[0];
                contentText = parts[1];
            }

            // 1. Deliver to in-game players (Using SmallFont, NO BOLD)
            String prefixText = plugin.getConfig().getString("websocket.chat-prefix", "[Web] ");
            String prefixColorName = plugin.getConfig().getString("websocket.prefix-color", "DARK_PURPLE");
            String messageColorName = plugin.getConfig().getString("websocket.message-color", "LIGHT_PURPLE");

            NamedTextColor prefixColor = NamedTextColor.NAMES.value(prefixColorName.toLowerCase());
            if (prefixColor == null) prefixColor = NamedTextColor.DARK_PURPLE;
            
            NamedTextColor messageColor = NamedTextColor.NAMES.value(messageColorName.toLowerCase());
            if (messageColor == null) messageColor = NamedTextColor.LIGHT_PURPLE;

            // Everything in SmallFont as requested
            String formattedPrefix = SmallFont.toSmallFont(prefixText);
            String formattedSender = SmallFont.toSmallFont(sender + ": ");
            String formattedContent = SmallFont.toSmallFont(contentText);

            Component finalMessage = Component.text()
                .append(Component.text(formattedPrefix, prefixColor))
                .append(Component.text(formattedSender, NamedTextColor.WHITE))
                .append(Component.text(formattedContent, messageColor))
                .build();

            // Run on main thread to ensure compatibility
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.adventure().all().sendMessage(finalMessage);
            });

            // 2. Broadcast to web clients with source "web"
            broadcastToWeb(sender, contentText, "web");
        } catch (Exception e) {
            plugin.getConsoleLogger().warning("Error processing web message: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        plugin.getConsoleLogger().warning("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        plugin.getConsoleLogger().info("WebSocket server started on port " + getPort());
    }

    public void broadcastToWeb(String sender, String message) {
        broadcastToWeb(sender, message, "game");
    }

    public void broadcastToWeb(String sender, String message, String source) {
        broadcast(gson.toJson(new ChatMessage(sender, message, source)));
    }

    private static class ChatMessage {
        private final String sender;
        private final String message;
        private final String source;

        public ChatMessage(String sender, String message, String source) {
            this.sender = sender;
            this.message = message;
            this.source = source;
        }
    }
}