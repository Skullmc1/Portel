package com.qclid.portel;

import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import org.bukkit.plugin.java.JavaPlugin;

public class ErrorPageHandler {

    private final JavaPlugin plugin;

    public ErrorPageHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void serve403(HttpExchange t) throws IOException {
        File errorFile = new File(
            plugin.getDataFolder(),
            "web/error-pages/403.html"
        );
        t.getResponseHeaders().set("Content-Type", "text/html");
        t.sendResponseHeaders(403, errorFile.length());
        OutputStream os = t.getResponseBody();
        Files.copy(errorFile.toPath(), os);
        os.close();
    }

    public void serve404(HttpExchange t) throws IOException {
        File errorFile = new File(
            plugin.getDataFolder(),
            "web/error-pages/404.html"
        );
        t.getResponseHeaders().set("Content-Type", "text/html");
        t.sendResponseHeaders(404, errorFile.length());
        OutputStream os = t.getResponseBody();
        Files.copy(errorFile.toPath(), os);
        os.close();
    }
}
