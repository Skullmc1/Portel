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
        plugin.getConsoleLogger().info("Web message raw data: " + message);

        try {
            boolean allowed = plugin.getConfig().getBoolean("websocket.allow-web-to-game-chat", true);
            if (!allowed) {
                plugin.getConsoleLogger().warning("Web-to-game chat is DISABLED in config.yml. Ignoring message from " + conn.getRemoteSocketAddress());
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

            plugin.getConsoleLogger().info("Processing message from [" + sender + "]: " + contentText);

            // 1. Broadcast to all web clients (synchronized view)
            broadcastToWeb(sender, contentText);

            // 2. Deliver to in-game players
            String prefixText = plugin.getConfig().getString("websocket.chat-prefix", "[Web] ");
            String prefixColorName = plugin.getConfig().getString("websocket.prefix-color", "DARK_PURPLE");
            String messageColorName = plugin.getConfig().getString("websocket.message-color", "LIGHT_PURPLE");

            NamedTextColor prefixColor = NamedTextColor.NAMES.value(prefixColorName.toLowerCase());
            if (prefixColor == null) prefixColor = NamedTextColor.DARK_PURPLE;
            
            NamedTextColor messageColor = NamedTextColor.NAMES.value(messageColorName.toLowerCase());
            if (messageColor == null) messageColor = NamedTextColor.LIGHT_PURPLE;

            // Professional format: [Web] Sender: Message
            Component finalMessage = Component.text()
                .append(Component.text(prefixText, prefixColor).decorate(TextDecoration.BOLD))
                .append(Component.text(sender, NamedTextColor.WHITE))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(contentText, messageColor))
                .build();

            // Run on main thread to ensure compatibility
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.adventure().all().sendMessage(finalMessage);
                plugin.getConsoleLogger().info("Message successfully sent to Adventure audiences.");
            });
        } catch (Exception e) {
            plugin.getConsoleLogger().warning("Error processing web message: " + e.getMessage());
            e.printStackTrace();
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
        broadcast(gson.toJson(new ChatMessage(sender, message)));
    }

    private static class ChatMessage {
        private final String sender;
        private final String message;

        public ChatMessage(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }
    }
}