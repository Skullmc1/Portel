package com.qclid.portel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class Portel extends JavaPlugin {

    private HttpServer server;
    private final Map<String, Long> lastRequest = new ConcurrentHashMap<>();

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
            startWebServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getCommand("portel").setExecutor(new PortelCommand(this));
    }

    @Override
    public void onDisable() {
        server.stop(0);
    }

    public void restartWebServer() {
        server.stop(0);
        try {
            startWebServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startWebServer() throws IOException {
        server = HttpServer.create(
            new InetSocketAddress(getConfig().getInt("port")),
            0
        );
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
        getLogger().info(
            "Web server started on port " + getConfig().getInt("port")
        );
    }

    class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            if (getConfig().getBoolean("rate-limiting.enabled")) {
                String ip = t.getRemoteAddress().getAddress().getHostAddress();

                long time = System.currentTimeMillis();

                if (
                    lastRequest.containsKey(ip) &&
                    time - lastRequest.get(ip) <
                    getConfig().getInt("rate-limiting.delay")
                ) {
                    String response = "429 (Too Many Requests)";

                    t.sendResponseHeaders(429, response.length());

                    OutputStream os = t.getResponseBody();

                    os.write(response.getBytes());

                    os.close();

                    return;
                }

                lastRequest.put(ip, time);
            }

            String requestedFile = t.getRequestURI().getPath().equals("/")
                ? getConfig().getString("index-file")
                : t.getRequestURI().getPath().substring(1);

            File file = new File(getDataFolder(), "web/" + requestedFile);

            getLogger().info(
                "Attempting to serve file: " + file.getAbsolutePath()
            );

            getLogger().info(
                "User " +
                    t.getRemoteAddress().getAddress().getHostAddress() +
                    " requested " +
                    requestedFile
            );

            if (file.exists()) {
                String mimeType = getMimeType(file.getName());
                t.getResponseHeaders().set("Content-Type", mimeType);
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

        private String getMimeType(String fileName) {
            if (fileName.endsWith(".css")) {
                return "text/css";
            } else if (fileName.endsWith(".js")) {
                return "application/javascript";
            } else if (fileName.endsWith(".png")) {
                return "image/png";
            } else if (fileName.endsWith(".ico")) {
                return "image/x-icon";
            } else if (fileName.endsWith(".ttf")) {
                return "font/ttf";
            } else if (fileName.endsWith(".otf")) {
                return "font/otf";
            }
            return "text/html";
        }
    }
}
