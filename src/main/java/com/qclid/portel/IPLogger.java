package com.qclid.portel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.plugin.java.JavaPlugin;

public class IPLogger {

    private final JavaPlugin plugin;
    private final ConsoleLogger logger;

    public IPLogger(JavaPlugin plugin, ConsoleLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void log(String ip) {
        if (plugin.getConfig().getBoolean("logging.ip")) {
            try (
                FileWriter fw = new FileWriter(
                    new File(plugin.getDataFolder(), "ips.yml"),
                    true
                );
                PrintWriter pw = new PrintWriter(fw)
            ) {
                String timestamp = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss"
                ).format(new Date());
                pw.println(timestamp + " " + ip);
            } catch (IOException e) {
                logger.warning("Failed to log IP address: " + e.getMessage());
            }
        }
    }
}
