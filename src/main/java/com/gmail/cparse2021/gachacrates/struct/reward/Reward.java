package com.gmail.cparse2021.gachacrates.struct.reward;

import com.gmail.cparse2021.gachacrates.util.ItemBuilder;
import com.gmail.cparse2021.gachacrates.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Reward {
    private final String name;
    private final List<ItemStack> items = new ArrayList<>();
    private final List<String> commands = new ArrayList<>();
    private ItemStack displayItem = new ItemBuilder(Material.AMETHYST_SHARD).setDisplayName("&dReward").build();
    private boolean featured = false;

    public Reward(String name) {
        this.name = name;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public String getName() {
        return this.name;
    }

    public boolean isFeatured() {
        return !this.featured;
    }

    public void execute(Player player) {
        for (String cmd : this.commands) {
            cmd = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        for (ItemStack item : this.items) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }
        }
    }

    public void loadFrom(ConfigurationSection rewardSection) {
        rewardSection.getStringList("Items").forEach(i -> this.items.add(Utils.decodeItem(i)));
        this.commands.addAll(rewardSection.getStringList("Commands"));
        this.displayItem = Utils.decodeItem(rewardSection.getString("Display-Item", "AMETHYST_SHARD 1 name:&d" + this.name));
        this.featured = rewardSection.getBoolean("Featured", false);
    }
}
