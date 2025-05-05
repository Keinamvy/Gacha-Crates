package com.gmail.cparse2021.gachacrates.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import javax.annotation.Nullable;

public abstract class Menu {
    private final String menuID;

    public Menu(String menuID) {
        this.menuID = menuID;
    }

    public String getMenuID() {
        return this.menuID;
    }

    public abstract void load(@Nullable ConfigurationSection var1);

    public abstract void open(Player var1);

    public abstract void processClick(InventoryClickEvent var1);

    public abstract void processClose(InventoryCloseEvent var1);
}
