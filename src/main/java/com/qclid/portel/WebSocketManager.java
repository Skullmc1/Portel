package com.qclid.portel;

import com.google.gson.Gson;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        if (!plugin.getConfig().getBoolean("websocket.allow-web-to-game-chat", false)) {
            plugin.getConsoleLogger().warning("Blocked unauthorized web-to-game message from " + conn.getRemoteSocketAddress());
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

        // 1. Broadcast to all web clients (including the sender)
        broadcastToWeb(sender, contentText);

        // 2. Deliver to in-game players
        String prefixText = plugin.getConfig().getString("websocket.chat-prefix", "[Portel] * ");
        String prefixColorName = plugin.getConfig().getString("websocket.prefix-color", "DARK_PURPLE");
        String messageColorName = plugin.getConfig().getString("websocket.message-color", "LIGHT_PURPLE");

        NamedTextColor prefixColor = NamedTextColor.NAMES.value(prefixColorName.toLowerCase());
        if (prefixColor == null) prefixColor = NamedTextColor.DARK_PURPLE;
        
        NamedTextColor messageColor = NamedTextColor.NAMES.value(messageColorName.toLowerCase());
        if (messageColor == null) messageColor = NamedTextColor.LIGHT_PURPLE;

        Component prefix = Component.text(prefixText, prefixColor);
        Component userComp = Component.text(sender + ": ", prefixColor);
        Component content = Component.text(contentText, messageColor);

        Component finalMessage = prefix.append(userComp).append(content);

        // Run on main thread to be safe with Bukkit API
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.adventure().all().sendMessage(finalMessage);
        });
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