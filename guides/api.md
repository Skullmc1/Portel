# Portel API

Portel lets other plugins expose their own HTTP endpoints through the web server it already runs. Register a route, return JSON (or anything else), done. No extra ports, no separate web server.

Portel's IP whitelist/blacklist and rate limiting apply to API routes too. Routes are removed automatically when the plugin that registered them is disabled.

## Setup

Add Portel as a (soft)dependency in your `plugin.yml`:

```yaml
softdepend: [Portel]
```

For compile-time access, depend on the Portel jar (`compileOnly`), then fetch the API from Bukkit's service manager:

```java
PortelAPI api = Bukkit.getServicesManager().load(PortelAPI.class);
if (api == null) return; // Portel is not installed
```

## Registering routes

```java
api.register(this, "GET", "/api/shop/items", request ->
    PortelResponse.json(Map.of("items", shop.listItems())));

api.register(this, "POST", "/api/shop/buy", request -> {
    String body = request.bodyAsString();
    // ...
    return PortelResponse.json(201, Map.of("ok", true));
});

// Prefix route: matches /api/player/anything/here
api.register(this, "GET", "/api/player/*", request -> {
    String name = request.path().substring("/api/player/".length());
    return PortelResponse.json(Map.of("name", name));
});
```

| Argument | Meaning |
| --- | --- |
| `owner` | Your plugin instance. |
| `method` | `GET`, `POST`, `PUT`, `DELETE`, ... or `*` for any method. |
| `path` | Exact path (`/api/shop`) or a prefix ending in `/*`. Exact routes win over prefixes; longer prefixes win over shorter ones. |
| `handler` | A `PortelHandler`: `PortelResponse handle(PortelRequest request)`. |

Registering the same method and path twice throws `IllegalArgumentException`. Requests to a known path with the wrong method get `405`. If your handler throws, the client gets a `500` and the error is logged.

## Requests

`PortelRequest` gives you `method()`, `path()`, `query()` / `query(name)`, `header(name)`, `body()` / `bodyAsString()` (max 1 MB, larger gets `413`) and `remoteAddress()`.

## Responses

`PortelResponse` has helpers: `json(value)`, `json(status, value)`, `text(string)`, `html(string)`, `bytes(status, contentType, data)`, `status(code)` and `error(status, message)`. Add headers with `.header(name, value)`, for example CORS:

```java
return PortelResponse.json(data).header("Access-Control-Allow-Origin", "*");
```

## Threading (important)

Handlers run on Portel's HTTP worker threads, **not** the Minecraft main thread. Never call the Bukkit world/entity API from a handler. The usual patterns:

-   Keep a snapshot of the data you need, refreshed from a main-thread scheduler task, and serve that from the handler (this is what Portel's dashboard does).
-   Or schedule work on the main thread and wait for it with a `CompletableFuture`.

## Using the API from your website

Your pages are served by Portel from the same origin, so plain `fetch` works:

```js
const items = await (await fetch("/api/shop/items")).json();
```

## Built-in endpoints

Portel's own [live dashboard](dashboard.md) is built on this API: `/api/status`, `/api/players`, `/api/events` and `/api/history`. They're a good reference implementation (`DashboardService.java`).
