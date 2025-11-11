package com.qclid.portel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public final class Portel extends JavaPlugin {

    private HttpServer server;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("web/index.html", false);
        saveResource("web/styles.css", false);
        saveResource("web/script.js", false);
        saveResource("web/assets/favicon.ico", false);
        saveResource("web/assets/logo.png", false);
        saveResource("web/fonts/Unbounded.ttf", false);
        saveResource("web/fonts/Minecraft.otf", false);

        try {
            server = HttpServer.create(new InetSocketAddress(getConfig().getInt("port")), 0);
            server.createContext("/", new MyHandler());
            server.setExecutor(null);
            server.start();
            getLogger().info("Web server started on port " + getConfig().getInt("port"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        server.stop(0);
    }

    class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String requestedFile = t.getRequestURI().getPath().equals("/") ? getConfig().getString("index-file") : t.getRequestURI().getPath().substring(1);
            File file = new File(getDataFolder(), "web/" + requestedFile);
            getLogger().info("User " + t.getRemoteAddress().getAddress().getHostAddress() + " requested " + requestedFile);
            if (file.exists()) {
                t.sendResponseHeaders(200, file.length());
                OutputStream os = t.getResponseBody();
                Files.copy(file.toPath(), os);
                os.close();
            } else {
                String response = "404 (Not Found)";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}
