package com.qclid.portel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class WebSocketManager extends WebSocketServer {

    private final Portel plugin;

    public WebSocketManager(Portel plugin, int port) {
        super(new InetSocketAddress(port));
        this.plugin = plugin;
        configureSSL();
    }

    private void configureSSL() {
        if (plugin.getConfig().getBoolean("ssl.enabled")) {
            try {
                char[] password = plugin.getConfig().getString("ssl.keystore-password").toCharArray();
                KeyStore ks = KeyStore.getInstance("JKS");
                FileInputStream fis = new FileInputStream(new File(plugin.getDataFolder(), plugin.getConfig().getString("ssl.keystore-path")));
                ks.load(fis, password);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, password);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                this.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
                plugin.getConsoleLogger().info("WebSocket configured for WSS (Secure WebSocket).");
            } catch (Exception e) {
                 plugin.getConsoleLogger().warning("Failed to configure SSL for WebSocket: " + e.getMessage());
                 e.printStackTrace();
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