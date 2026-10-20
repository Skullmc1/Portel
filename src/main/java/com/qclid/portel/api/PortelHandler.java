package com.qclid.portel.api;

/**
 * Handles a request to a route registered through {@link PortelAPI}.
 * <p>
 * Handlers run on one of Portel's HTTP worker threads, <b>not</b> the Minecraft
 * main thread. Do not touch the Bukkit world/entity API from here; read from
 * thread-safe state instead or hop to the main thread yourself.
 */
@FunctionalInterface
public interface PortelHandler {
    PortelResponse handle(PortelRequest request) throws Exception;
}
