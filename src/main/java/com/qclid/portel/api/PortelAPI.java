package com.qclid.portel.api;

import java.util.List;
import org.bukkit.plugin.Plugin;

/**
 * Lets other plugins expose HTTP endpoints through Portel's web server.
 *
 * <pre>{@code
 * PortelAPI api = Bukkit.getServicesManager().load(PortelAPI.class);
 * if (api != null) {
 *     api.register(this, "GET", "/api/shop/items",
 *         req -> PortelResponse.json(Map.of("items", shop.listItems())));
 * }
 * }</pre>
 *
 * Add {@code softdepend: [Portel]} to your plugin.yml. Routes are removed
 * automatically when the owning plugin is disabled. Portel's IP whitelist /
 * blacklist and rate limiting apply to API routes as well.
 */
public interface PortelAPI {
    /**
     * Registers a route.
     *
     * @param owner   the plugin that owns the route
     * @param method  {@code GET}, {@code POST}, {@code PUT}, {@code DELETE} etc., or {@code *} for any method
     * @param path    exact path such as {@code /api/shop}, or a prefix ending in {@code /*}
     *                such as {@code /api/shop/*}
     * @param handler the handler (runs off the main thread)
     * @throws IllegalArgumentException if the path is invalid or already registered for this method
     */
    void register(Plugin owner, String method, String path, PortelHandler handler);

    /** Removes one route. Returns true if it existed and belonged to {@code owner}. */
    boolean unregister(Plugin owner, String method, String path);

    /** Removes every route owned by {@code owner}. */
    void unregisterAll(Plugin owner);

    /** Currently registered routes as {@code "METHOD /path (owner)"} strings. */
    List<String> routes();
}
