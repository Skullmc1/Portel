package com.qclid.portel;

import com.qclid.portel.api.PortelAPI;
import com.qclid.portel.api.PortelResponse;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 * Collects live server data on the main thread and serves it as JSON through the
 * Portel API: /api/status, /api/players, /api/events and /api/history.
 */
public class DashboardService implements Listener {

    private static final int HISTORY_SIZE = 180; // 15 minutes at one sample every 5s

    private final Portel plugin;
    private final PortelAPI api;
    private final ConcurrentLinkedDeque<Map<String, Object>> events = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Map<String, Object>> history = new ConcurrentLinkedDeque<>();
    private final AtomicLong eventId = new AtomicLong();

    private volatile Map<String, Object> status = Map.of();
    private volatile List<Map<String, Object>> players = List.of();
    private BukkitTask task;
    private int tick;

    public DashboardService(Portel plugin, PortelAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    private boolean showLocations() {
        return plugin.getConfig().getBoolean("dashboard.show-locations", false);
    }

    private boolean showChat() {
        return plugin.getConfig().getBoolean("dashboard.show-chat", true);
    }

    public void start() {
        api.register(plugin, "GET", "/api/status", r -> PortelResponse.json(status));
        api.register(plugin, "GET", "/api/players", r -> PortelResponse.json(players));
        api.register(plugin, "GET", "/api/history", r -> PortelResponse.json(new ArrayList<>(history)));
        api.register(plugin, "GET", "/api/events", r -> {
            long since = 0;
            try {
                since = Long.parseLong(r.query().getOrDefault("since", "0"));
            } catch (NumberFormatException ignored) {}
            List<Map<String, Object>> out = new ArrayList<>();
            for (Map<String, Object> e : events) {
                if ((long) e.get("id") > since) out.add(e);
            }
            return PortelResponse.json(out);
        });

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::collect, 20L, 20L);
        collect();
    }

    public void stop() {
        if (task != null) task.cancel();
        api.unregisterAll(plugin);
    }

    // ---- data collection (main thread) ----

    private void collect() {
        tick++;
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMb = rt.maxMemory() / (1024 * 1024);
        double[] tps = Bukkit.getTPS();
        double mspt = Bukkit.getAverageTickTime();
        boolean locations = showLocations();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", p.getName());
            m.put("uuid", p.getUniqueId().toString());
            m.put("world", p.getWorld().getName());
            m.put("gamemode", p.getGameMode().name().toLowerCase());
            m.put("ping", p.getPing());
            m.put("health", Math.round(p.getHealth() * 10.0) / 10.0);
            if (locations) {
                Location l = p.getLocation();
                m.put("x", l.getBlockX());
                m.put("y", l.getBlockY());
                m.put("z", l.getBlockZ());
            }
            list.add(m);
        }
        players = list;

        List<Map<String, Object>> worlds = new ArrayList<>();
        if (locations) {
            for (World w : Bukkit.getWorlds()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", w.getName());
                m.put("environment", w.getEnvironment().name().toLowerCase());
                m.put("borderSize", w.getWorldBorder().getSize());
                m.put("borderX", w.getWorldBorder().getCenter().getX());
                m.put("borderZ", w.getWorldBorder().getCenter().getZ());
                m.put("spawnX", w.getSpawnLocation().getBlockX());
                m.put("spawnZ", w.getSpawnLocation().getBlockZ());
                worlds.add(m);
            }
        }

        Map<String, Object> s = new LinkedHashMap<>();
        s.put("name", plugin.getConfig().getString("dashboard.server-name", "Minecraft Server"));
        s.put("motd", PlainTextComponentSerializer.plainText().serialize(Bukkit.motd()));
        s.put("minecraftVersion", Bukkit.getMinecraftVersion());
        s.put("software", Bukkit.getName());
        s.put("online", list.size());
        s.put("max", Bukkit.getMaxPlayers());
        s.put("tps", Math.min(20.0, Math.round(tps[0] * 100.0) / 100.0));
        s.put("mspt", Math.round(mspt * 100.0) / 100.0);
        s.put("memoryUsedMb", usedMb);
        s.put("memoryMaxMb", maxMb);
        s.put("uptimeSeconds", (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000);
        s.put("showLocations", locations);
        s.put("showChat", showChat());
        s.put("skinUrl", plugin.getConfig().getString("dashboard.skin-url", "https://mc-heads.net/avatar/{uuid}/48"));
        s.put("worlds", worlds);
        status = s;

        if (tick % 5 == 0) {
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("t", System.currentTimeMillis());
            h.put("tps", s.get("tps"));
            h.put("mspt", s.get("mspt"));
            h.put("players", list.size());
            h.put("memoryUsedMb", usedMb);
            history.addLast(h);
            while (history.size() > HISTORY_SIZE) history.pollFirst();
        }
    }

    // ---- event feed ----

    private void addEvent(String type, String player, String text) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("id", eventId.incrementAndGet());
        e.put("t", System.currentTimeMillis());
        e.put("type", type);
        e.put("player", player);
        e.put("text", text);
        events.addLast(e);
        int max = plugin.getConfig().getInt("dashboard.events-max", 50);
        while (events.size() > max) events.pollFirst();
    }

    private static String plain(Component c) {
        return c == null ? "" : PlainTextComponentSerializer.plainText().serialize(c);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        addEvent("join", e.getPlayer().getName(), e.getPlayer().getName() + " joined the game");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        addEvent("quit", e.getPlayer().getName(), e.getPlayer().getName() + " left the game");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        String msg = plain(e.deathMessage());
        addEvent("death", e.getEntity().getName(), msg.isEmpty() ? e.getEntity().getName() + " died" : msg);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        if (e.getAdvancement().getKey().getKey().startsWith("recipes/")) return;
        String msg = plain(e.message());
        if (msg.isEmpty()) return;
        addEvent("advancement", e.getPlayer().getName(), msg);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        if (!showChat()) return;
        addEvent("chat", e.getPlayer().getName(), plain(e.message()));
    }
}
