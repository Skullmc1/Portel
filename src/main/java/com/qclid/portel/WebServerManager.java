package com.qclid.portel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class WebServerManager {

    private final JavaPlugin plugin;
    private final ConsoleLogger logger;
    private final IPLogger ipLogger;
    private final RateLimiter rateLimiter;
    private final ErrorPageHandler errorPageHandler;
    private HttpServer server;

    public WebServerManager(
        JavaPlugin plugin,
        ConsoleLogger logger,
        IPLogger ipLogger,
        RateLimiter rateLimiter,
        ErrorPageHandler errorPageHandler
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.ipLogger = ipLogger;
        this.rateLimiter = rateLimiter;
        this.errorPageHandler = errorPageHandler;
    }

    public void start() throws IOException {
        server = HttpServer.create(
            new InetSocketAddress(plugin.getConfig().getInt("port")),
            0
        );
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
        logger.info(
            "Web server started on port " + plugin.getConfig().getInt("port")
        );
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            logger.info("Web server stopped.");
        }
    }

    public void restart() {
        stop();
        try {
            start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String ip = t.getRemoteAddress().getAddress().getHostAddress();
            boolean isWhitelistOn = plugin
                .getConfig()
                .getBoolean("is_whitelist_on");
            List<String> ipList = plugin.getConfig().getStringList("ip_list");

            if (isWhitelistOn) {
                if (!ipList.contains(ip)) {
                    File errorFile = new File(
                        plugin.getDataFolder(),
                        "web/error-pages/403.html"
                    );
                    t.getResponseHeaders().set("Content-Type", "text/html");
                    t.sendResponseHeaders(200, errorFile.length());
                    OutputStream os = t.getResponseBody();
                    Files.copy(errorFile.toPath(), os);
                    os.close();
                    return;
                }
            } else {
                if (ipList.contains(ip)) {
                    errorPageHandler.serve403(t);
                    return;
                }
            }

            ipLogger.log(ip);

            String requestedFile = t.getRequestURI().getPath().equals("/")
                ? plugin.getConfig().getString("index-file")
                : t.getRequestURI().getPath().substring(1);

            if (rateLimiter.isRateLimited(t, requestedFile)) {
                return;
            }

            File file = new File(
                plugin.getDataFolder(),
                "web/" + requestedFile
            );

            logger.info("Attempting to serve file: " + file.getAbsolutePath());
            logger.info(
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
                errorPageHandler.serve404(t);
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
