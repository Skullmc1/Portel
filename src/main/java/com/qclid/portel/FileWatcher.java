package com.qclid.portel;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;

public class FileWatcher implements Runnable {
    private final JavaPlugin plugin;
    private final WebServerManager webServerManager;
    private final Path rootPath;
    private final WatchService watchService;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private boolean running = true;

    public FileWatcher(JavaPlugin plugin, WebServerManager webServerManager, Path rootPath) throws IOException {
        this.plugin = plugin;
        this.webServerManager = webServerManager;
        this.rootPath = rootPath;
        this.watchService = FileSystems.getDefault().newWatchService();
        registerAll(rootPath);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, 
                                   StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        keys.put(key, dir);
    }

    @Override
    public void run() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) continue;

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path name = ev.context();
                Path child = dir.resolve(name);

                // If a new directory is created, register it and its subdirectories
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // Ignore
                    }
                }

                // Get relative path for cache clearing
                try {
                    String relativePath = rootPath.relativize(child).toString().replace("\\", "/");
                    webServerManager.clearCache(relativePath);
                } catch (IllegalArgumentException e) {
                    // Path might not be relative to root if something weird happens
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) break;
            }
        }
    }

    public void stop() {
        running = false;
        try {
            watchService.close();
        } catch (IOException e) {
            // Already closed or other issue
        }
    }
}
