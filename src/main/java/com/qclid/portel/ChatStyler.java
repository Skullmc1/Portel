package com.qclid.portel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class ChatStyler {

    private final Portel plugin;
    private static final String PREFIX =
        "<gradient:#8A2BE2:#4B0082>Portel</gradient> ";

    public ChatStyler(Portel plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(CommandSender sender, String message) {
        String smallFontMessage = SmallFont.toSmallFont(message);
        Component component = MiniMessage.miniMessage().deserialize(
            PREFIX + smallFontMessage
        );
        plugin.adventure().sender(sender).sendMessage(component);
    }
}
