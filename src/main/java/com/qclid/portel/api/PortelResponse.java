package com.qclid.portel.api;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** A response returned from a {@link PortelHandler}. */
public final class PortelResponse {

    private static final Gson GSON = new Gson();

    private final int status;
    private final String contentType;
    private final byte[] body;
    private final Map<String, String> headers = new LinkedHashMap<>();

    private PortelResponse(int status, String contentType, byte[] body) {
        this.status = status;
        this.contentType = contentType;
        this.body = body;
    }

    /** 200 response with {@code value} serialized as JSON. */
    public static PortelResponse json(Object value) {
        return json(200, value);
    }

    public static PortelResponse json(int status, Object value) {
        return new PortelResponse(
            status,
            "application/json; charset=utf-8",
            GSON.toJson(value).getBytes(StandardCharsets.UTF_8)
        );
    }

    public static PortelResponse text(String text) {
        return new PortelResponse(
            200,
            "text/plain; charset=utf-8",
            text.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static PortelResponse html(String html) {
        return new PortelResponse(
            200,
            "text/html; charset=utf-8",
            html.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static PortelResponse bytes(int status, String contentType, byte[] body) {
        return new PortelResponse(status, contentType, body);
    }

    /** Empty response with the given status, e.g. 204. */
    public static PortelResponse status(int status) {
        return new PortelResponse(status, "text/plain; charset=utf-8", new byte[0]);
    }

    /** JSON error body: {@code {"error": "<message>"}}. */
    public static PortelResponse error(int status, String message) {
        return json(status, Map.of("error", message));
    }

    /** Adds a response header (e.g. CORS). Returns this for chaining. */
    public PortelResponse header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public int status() {
        return status;
    }

    public String contentType() {
        return contentType;
    }

    public byte[] body() {
        return body;
    }

    public Map<String, String> headers() {
        return headers;
    }
}
