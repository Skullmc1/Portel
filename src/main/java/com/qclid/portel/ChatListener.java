package com.qclid.portel;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final WebSocketManager webSocketManager;

    public ChatListener(WebSocketManager webSocketManager) {
        this.webSocketManager = webSocketManager;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String sender = event.getPlayer().getName();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        webSocketManager.broadcastToWeb(sender, message);
    }
}
