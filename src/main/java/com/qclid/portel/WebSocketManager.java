package com.qclid.portel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class WebSocketManager extends WebSocketServer {

    private final Portel plugin;

    public WebSocketManager(Portel plugin, int port) {
        super(new InetSocketAddress(port));
        this.plugin = plugin;
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
        // Format: Purple prefix, star separator, message
        Component prefix = Component.text("[Portel]", NamedTextColor.DARK_PURPLE);
        Component separator = Component.text(" * ", NamedTextColor.DARK_PURPLE);
        Component content = Component.text(message, NamedTextColor.LIGHT_PURPLE);

        Component finalMessage = prefix.append(separator).append(content);

        // Run on main thread to be safe with Bukkit API if needed, though adventure is async-safe usually.
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
        // Simple JSON construction
        String json = String.format("{\"sender\": \"%s\", \"message\": \"%s\"}", escape(sender), escape(message));
        broadcast(json);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
