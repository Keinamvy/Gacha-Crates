package com.gmail.cparse2021.gachacrates.listeners;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {
    private final GachaCrates plugin;

    public MenuListener(GachaCrates plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Menu menu = this.plugin.getMenuManager().getActiveMenu(player.getUniqueId());
        if (menu != null) {
            e.setCancelled(true);
            menu.processClick(e);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Menu menu = this.plugin.getMenuManager().getActiveMenu(player.getUniqueId());
        if (menu != null) {
            menu.processClose(e);
        }
    }
}
