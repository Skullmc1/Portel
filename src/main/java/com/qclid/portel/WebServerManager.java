package com.qclid.portel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.bukkit.plugin.java.JavaPlugin;

public class WebServerManager {

    private final JavaPlugin plugin;
    private final ConsoleLogger logger;
    private final IPLogger ipLogger;
    private final RateLimiter rateLimiter;
    private final ErrorPageHandler errorPageHandler;
    private final PlaceholderHook placeholderHook;
    private HttpServer server;

    private final Map<String, byte[]> fileCache = new ConcurrentHashMap<>();

    public WebServerManager(
        JavaPlugin plugin,
        ConsoleLogger logger,
        IPLogger ipLogger,
        RateLimiter rateLimiter,
        ErrorPageHandler errorPageHandler,
        PlaceholderHook placeholderHook
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.ipLogger = ipLogger;
        this.rateLimiter = rateLimiter;
        this.errorPageHandler = errorPageHandler;
        this.placeholderHook = placeholderHook;
    }

    public void start() throws IOException {
        int port = plugin.getConfig().getInt("port");
        boolean sslEnabled = plugin.getConfig().getBoolean("ssl.enabled");

        if (sslEnabled) {
            try {
                HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
                SSLContext sslContext = SSLUtils.createSSLContext(plugin.getDataFolder(), plugin.getConfig());

                httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    public void configure(HttpsParameters params) {
                        try {
                            SSLContext c = getSSLContext();
                            SSLParameters sslparams = c.getDefaultSSLParameters();
                            params.setSSLParameters(sslparams);
                        } catch (Exception e) {
                            logger.warning("SSL configuration error: " + e.getMessage());
                        }
                    }
                });

                server = httpsServer;
                logger.info("Secure Web server (HTTPS) started on port " + port);

            } catch (Exception e) {
                logger.warning("Failed to start HTTPS server: " + e.getMessage());
                throw new IOException("Failed to start HTTPS server", e);
            }
        } else {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            logger.info("Web server started on port " + port);
        }

        server.createContext("/", new MyHandler());
        // Use a thread pool to handle multiple requests simultaneously
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            fileCache.clear();
            logger.info("Web server stopped.");
        }
    }

    public void clearCache(String fileName) {
        if (fileCache.remove(fileName) != null) {
            logger.info("Cache cleared for: " + fileName);
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
                    serveErrorPage(t, "web/error-pages/403.html", 403);
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

            // Path Traversal Check
            Path webRoot = plugin.getDataFolder().toPath().resolve("web").toAbsolutePath().normalize();
            Path requestedPath = webRoot.resolve(requestedFile).toAbsolutePath().normalize();

            if (!requestedPath.startsWith(webRoot)) {
                logger.warning("Blocked potential path traversal attempt from " + ip + ": " + requestedFile);
                errorPageHandler.serve403(t);
                return;
            }

            if (rateLimiter.isRateLimited(t, requestedFile)) {
                return;
            }

            File file = requestedPath.toFile();

            if (file.exists() && !file.isDirectory()) {
                String mimeType = getMimeType(file.getName());
                t.getResponseHeaders().set("Content-Type", mimeType);

                if (mimeType.equals("text/html") || mimeType.equals("application/javascript")) {
                    // Don't cache HTML/JS as they may contain placeholders like %WEBSOCKET_PORT%
                    String content = Files.readString(file.toPath());
                    int wsPort = plugin.getConfig().getInt("websocket-port", plugin.getConfig().getInt("port") + 1);
                    
                    content = content.replace("%WEBSOCKET_PORT%", String.valueOf(wsPort));
                    
                    // Process Placeholders
                    content = placeholderHook.parse(content);
                    
                    byte[] bytes = content.getBytes("UTF-8");
                    t.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = t.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    // Cache other files (images, fonts, etc.)
                    byte[] bytes = fileCache.computeIfAbsent(requestedFile, k -> {
                        try {
                            return Files.readAllBytes(file.toPath());
                        } catch (IOException e) {
                            return null;
                        }
                    });

                    if (bytes != null) {
                        t.sendResponseHeaders(200, bytes.length);
                        try (OutputStream os = t.getResponseBody()) {
                            os.write(bytes);
                        }
                    } else {
                        errorPageHandler.serve404(t);
                    }
                }
            } else {
                errorPageHandler.serve404(t);
            }
        }

        private void serveErrorPage(HttpExchange t, String path, int code) throws IOException {
            File errorFile = new File(plugin.getDataFolder(), path);
            t.getResponseHeaders().set("Content-Type", "text/html");
            t.sendResponseHeaders(code, errorFile.length());
            try (OutputStream os = t.getResponseBody()) {
                Files.copy(errorFile.toPath(), os);
            }
        }

        private String getMimeType(String fileName) {
            String name = fileName.toLowerCase();
            if (name.endsWith(".css")) return "text/css";
            if (name.endsWith(".js")) return "application/javascript";
            if (name.endsWith(".json")) return "application/json";
            if (name.endsWith(".xml")) return "application/xml";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
            if (name.endsWith(".gif")) return "image/gif";
            if (name.endsWith(".webp")) return "image/webp";
            if (name.endsWith(".svg")) return "image/svg+xml";
            if (name.endsWith(".ico")) return "image/x-icon";
            if (name.endsWith(".ttf")) return "font/ttf";
            if (name.endsWith(".otf")) return "font/otf";
            if (name.endsWith(".woff")) return "font/woff";
            if (name.endsWith(".woff2")) return "font/woff2";
            if (name.endsWith(".txt")) return "text/plain";
            if (name.endsWith(".mp3")) return "audio/mpeg";
            if (name.endsWith(".mp4")) return "video/mp4";
            return "text/html";
        }
    }
}
