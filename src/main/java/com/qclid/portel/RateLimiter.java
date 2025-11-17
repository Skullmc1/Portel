package com.qclid.portel;

import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.java.JavaPlugin;

public class RateLimiter {

    private final JavaPlugin plugin;
    private final Map<String, Long> lastRequest = new ConcurrentHashMap<>();

    public RateLimiter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isRateLimited(HttpExchange t, String requestedFile)
        throws IOException {
        if (plugin.getConfig().getBoolean("rate-limiting.enabled")) {
            String ip = t.getRemoteAddress().getAddress().getHostAddress();
            String ipAndFileKey = ip + ":" + requestedFile;
            long time = System.currentTimeMillis();

            if (
                lastRequest.containsKey(ipAndFileKey) &&
                time - lastRequest.get(ipAndFileKey) <
                plugin.getConfig().getInt("rate-limiting.delay")
            ) {
                File errorFile = new File(
                    plugin.getDataFolder(),
                    "web/error-pages/429.html"
                );
                t.getResponseHeaders().set("Content-Type", "text/html");
                t.sendResponseHeaders(429, errorFile.length());
                OutputStream os = t.getResponseBody();
                Files.copy(errorFile.toPath(), os);
                os.close();
                return true;
            }

            lastRequest.put(ipAndFileKey, time);
        }
        return false;
    }
}
