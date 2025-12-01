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
import java.security.KeyStore;
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import org.bukkit.plugin.java.JavaPlugin;

public class WebServerManager {

    private final JavaPlugin plugin;
    private final ConsoleLogger logger;
    private final IPLogger ipLogger;
    private final RateLimiter rateLimiter;
    private final ErrorPageHandler errorPageHandler;
    private final PlaceholderHook placeholderHook;
    private HttpServer server;

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
                SSLContext sslContext = SSLContext.getInstance("TLS");

                char[] password = plugin.getConfig().getString("ssl.keystore-password").toCharArray();
                KeyStore ks = KeyStore.getInstance("JKS");
                FileInputStream fis = new FileInputStream(new File(plugin.getDataFolder(), plugin.getConfig().getString("ssl.keystore-path")));
                ks.load(fis, password);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, password);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);

                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    public void configure(HttpsParameters params) {
                        try {
                            SSLContext c = getSSLContext();
                            SSLParameters sslparams = c.getDefaultSSLParameters();
                            params.setSSLParameters(sslparams);
                        } catch (Exception e) {
                            // Using plugin logger here as we are inside anonymous class and logger might not be accessible if private
                            // but we can access 'logger' if it's effectively final or we can use the plugin instance
                            // logger is a field, so we can access it
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
        server.setExecutor(null);
        server.start();
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

                if (mimeType.equals("text/html") || mimeType.equals("application/javascript")) {
                    String content = Files.readString(file.toPath());
                    int wsPort = plugin.getConfig().getInt("websocket-port", plugin.getConfig().getInt("port") + 1);
                    content = content.replace("%WEBSOCKET_PORT%", String.valueOf(wsPort));
                    
                    // Process Placeholders
                    content = placeholderHook.parse(content);

                    byte[] bytes = content.getBytes("UTF-8");
                    t.sendResponseHeaders(200, bytes.length);
                    OutputStream os = t.getResponseBody();
                    os.write(bytes);
                    os.close();
                } else {
                    t.sendResponseHeaders(200, file.length());
                    OutputStream os = t.getResponseBody();
                    Files.copy(file.toPath(), os);
                    os.close();
                }
            } else {
                errorPageHandler.serve404(t);
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
