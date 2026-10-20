package com.qclid.portel.api;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** An incoming HTTP request routed to a {@link PortelHandler}. */
public final class PortelRequest {

    private final String method;
    private final String path;
    private final Map<String, String> query;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final String remoteAddress;

    public PortelRequest(
        String method,
        String path,
        Map<String, String> query,
        Map<String, List<String>> headers,
        byte[] body,
        String remoteAddress
    ) {
        this.method = method;
        this.path = path;
        this.query = query;
        this.headers = headers;
        this.body = body;
        this.remoteAddress = remoteAddress;
    }

    /** Upper-case HTTP method, e.g. {@code GET}. */
    public String method() {
        return method;
    }

    /** Request path without the query string, e.g. {@code /api/shop}. */
    public String path() {
        return path;
    }

    /** Decoded query parameters (first value wins). */
    public Map<String, String> query() {
        return query;
    }

    /** A single query parameter, or {@code null}. */
    public String query(String name) {
        return query.get(name);
    }

    /** First value of a header (case-insensitive), or {@code null}. */
    public String header(String name) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name) && !e.getValue().isEmpty()) {
                return e.getValue().get(0);
            }
        }
        return null;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    /** Raw request body (empty for requests without one). */
    public byte[] body() {
        return body;
    }

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    /** IP address of the client. */
    public String remoteAddress() {
        return remoteAddress;
    }
}
