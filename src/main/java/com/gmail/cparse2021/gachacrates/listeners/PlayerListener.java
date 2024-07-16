package com.gmail.cparse2021.gachacrates.listeners;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private final GachaCrates plugin;

    public PlayerListener(GachaCrates plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        this.plugin.getPlayerCache().getPlayer(e.getPlayer().getUniqueId());
    }
}
