# Live Dashboard

Portel ships with a live server dashboard at `/dashboard.html`: players online with skin heads, TPS and MSPT, memory, uptime, a 15-minute TPS/players chart, an activity feed (joins, leaves, deaths, advancements, chat) and an optional live player map. It refreshes every 2 seconds.

It is served from your `plugins/Portel/web` folder like any other page, so you can restyle it freely. Portel only writes `dashboard.html` if the file is missing on startup.

## Configuration

```yaml
dashboard:
  enabled: true
  server-name: "Minecraft Server"
  # Exposes player coordinates to everyone who can reach the site.
  show-locations: false
  show-chat: true
  events-max: 50
  skin-url: "https://mc-heads.net/avatar/{uuid}/48"
```

-   **`show-locations`** turns on the live map (per-world tabs, world border, spawn, player heads with pan/zoom). It publishes every player's coordinates, which makes griefing and base-raiding easier. It is **off by default**. Enable it for creative/minigame servers, or put the site behind the IP whitelist.
-   **`show-chat`** controls whether chat appears in the activity feed. Web chat over WebSocket already exposes chat, so this defaults to on.
-   **`skin-url`** is where player heads come from. The visitor's browser loads these images directly from that host (default: mc-heads.net). Point it at your own skin service if you prefer.
-   Changing `enabled` needs a server restart. The other options are read live.

## Data endpoints

The dashboard reads plain JSON, which you can use in your own pages:

| Endpoint | Returns |
| --- | --- |
| `GET /api/status` | name, MOTD, version, online/max, TPS, MSPT, memory, uptime, worlds |
| `GET /api/players` | name, uuid, world, gamemode, ping, health (+ x/y/z if `show-locations`) |
| `GET /api/events?since=<id>` | activity feed entries newer than `id` |
| `GET /api/history` | TPS/MSPT/players/memory samples, one every 5 s for 15 min |

These are registered through the [Portel API](api.md).
