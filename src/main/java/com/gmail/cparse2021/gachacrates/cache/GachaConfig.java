package com.gmail.cparse2021.gachacrates.cache;

import org.bukkit.configuration.file.FileConfiguration;

public class GachaConfig {
    private int MAX_PULLS;


    public int getMaxPull() {
        return MAX_PULLS;
    }

    public void load(FileConfiguration fileConfiguration) {
        MAX_PULLS = fileConfiguration.getInt("Max-Pulls", 20);
    }
}
