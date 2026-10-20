package com.qclid.portel;

import com.qclid.portel.api.PortelAPI;
import com.qclid.portel.api.PortelHandler;
import com.qclid.portel.api.PortelRequest;
import com.qclid.portel.api.PortelResponse;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.plugin.Plugin;

/** Holds routes registered through {@link PortelAPI} and dispatches HTTP requests to them. */
public class ApiRegistry implements PortelAPI {

    private static final int MAX_BODY_BYTES = 1024 * 1024;

    private record Route(String method, String path, boolean prefix, Plugin owner, PortelHandler handler) {
        boolean matchesPath(String requestPath) {
            return prefix ? requestPath.startsWith(path) : requestPath.equals(path);
        }
    }

    private final ConsoleLogger logger;
    private final List<Route> routes = new CopyOnWriteArrayList<>();

    public ApiRegistry(ConsoleLogger logger) {
        this.logger = logger;
    }

    @Override
    public void register(Plugin owner, String method, String path, PortelHandler handler) {
        if (owner == null || method == null || path == null || handler == null) {
            throw new IllegalArgumentException("owner, method, path and handler are required");
        }
        if (!path.startsWith("/") || path.equals("/") || path.contains("..")) {
            throw new IllegalArgumentException("Invalid route path: " + path);
        }
        String m = method.toUpperCase(Locale.ROOT);
        boolean prefix = path.endsWith("/*");
        String normalized = prefix ? path.substring(0, path.length() - 1) : path;

        synchronized (routes) {
            for (Route r : routes) {
                if (r.prefix() == prefix && r.path().equals(normalized) && r.method().equals(m)) {
                    throw new IllegalArgumentException(
                        "Route already registered by " + r.owner().getName() + ": " + m + " " + path
                    );
                }
            }
            routes.add(new Route(m, normalized, prefix, owner, handler));
        }
        logger.info("API route registered: " + m + " " + path + " (" + owner.getName() + ")");
    }

    @Override
    public boolean unregister(Plugin owner, String method, String path) {
        String m = method.toUpperCase(Locale.ROOT);
        boolean prefix = path.endsWith("/*");
        String normalized = prefix ? path.substring(0, path.length() - 1) : path;
        return routes.removeIf(
            r -> r.owner() == owner && r.method().equals(m) && r.prefix() == prefix && r.path().equals(normalized)
        );
    }

    @Override
    public void unregisterAll(Plugin owner) {
        if (routes.removeIf(r -> r.owner() == owner)) {
            logger.info("API routes removed for " + owner.getName());
        }
    }

    @Override
    public List<String> routes() {
        List<String> out = new ArrayList<>();
        for (Route r : routes) {
            out.add(r.method() + " " + r.path() + (r.prefix() ? "*" : "") + " (" + r.owner().getName() + ")");
        }
        return out;
    }

    private Route find(String method, String path) {
        Route best = null;
        for (Route r : routes) {
            if (!r.matchesPath(path) || !(r.method().equals("*") || r.method().equals(method))) continue;
            // Prefer exact routes, then the longest prefix.
            if (
                best == null ||
                (!r.prefix() && best.prefix()) ||
                (r.prefix() == best.prefix() && r.path().length() > best.path().length())
            ) {
                best = r;
            }
        }
        return best;
    }

    private boolean pathExists(String path) {
        for (Route r : routes) {
            if (r.matchesPath(path)) return true;
        }
        return false;
    }

    /**
     * Tries to serve the exchange from a registered route.
     *
     * @return true if the request was handled (a response has been sent)
     */
    public boolean dispatch(HttpExchange t, String ip) throws IOException {
        if (routes.isEmpty()) return false;
        String path = t.getRequestURI().getPath();
        String method = t.getRequestMethod().toUpperCase(Locale.ROOT);

        Route route = find(method, path);
        if (route == null) {
            if (pathExists(path)) {
                send(t, PortelResponse.error(405, "Method not allowed"));
                return true;
            }
            return false;
        }

        PortelResponse response;
        try {
            byte[] body = readBody(t.getRequestBody());
            PortelRequest request = new PortelRequest(
                method,
                path,
                parseQuery(t.getRequestURI().getRawQuery()),
                new HashMap<>(t.getRequestHeaders()),
                body,
                ip
            );
            response = route.handler().handle(request);
            if (response == null) response = PortelResponse.status(204);
        } catch (BodyTooLarge e) {
            response = PortelResponse.error(413, "Request body too large");
        } catch (Exception e) {
            logger.warning("API handler " + route.owner().getName() + " " + method + " " + path + " failed: " + e);
            response = PortelResponse.error(500, "Internal server error");
        }
        send(t, response);
        return true;
    }

    private static void send(HttpExchange t, PortelResponse response) throws IOException {
        t.getResponseHeaders().set("Content-Type", response.contentType());
        t.getResponseHeaders().set("Cache-Control", "no-store");
        response.headers().forEach((k, v) -> t.getResponseHeaders().set(k, v));
        byte[] body = response.body();
        boolean noBody = body.length == 0 || t.getRequestMethod().equalsIgnoreCase("HEAD");
        t.sendResponseHeaders(response.status(), noBody ? -1 : body.length);
        if (!noBody) {
            try (OutputStream os = t.getResponseBody()) {
                os.write(body);
            }
        }
        t.close();
    }

    private static class BodyTooLarge extends IOException {}

    private static byte[] readBody(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
            if (out.size() > MAX_BODY_BYTES) throw new BodyTooLarge();
        }
        return out.toByteArray();
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String pair : raw.split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            String key = URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = eq < 0 ? "" : URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            map.putIfAbsent(key, value);
        }
        return map;
    }
}
