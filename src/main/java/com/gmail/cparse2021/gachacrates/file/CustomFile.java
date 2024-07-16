package com.gmail.cparse2021.gachacrates.file;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class CustomFile {
    private final JavaPlugin plugin;
    private final String fileName;
    private File file = null;
    private FileConfiguration configFile = null;

    public CustomFile(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public FileConfiguration getConfig() {
        if (this.configFile == null) {
            this.reloadConfig();
        }

        return this.configFile;
    }

    public void reloadConfig() {
        if (this.configFile == null) {
            this.file = new File(this.plugin.getDataFolder(), this.fileName + ".yml");
        }

        this.configFile = YamlConfiguration.loadConfiguration(this.file);
        InputStream inputStream = this.plugin.getResource(this.fileName + ".yml");
        if (inputStream != null) {
            Reader defConfigStream = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.configFile.setDefaults(defConfig);
        }
    }

    public void saveConfig() {
        if (this.configFile != null && this.file != null) {
            try {
                this.getConfig().save(this.file);
            } catch (IOException var2) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile);
            }
        }
    }

    public void saveDefaultConfig() {
        if (this.file == null) {
            this.file = new File(this.plugin.getDataFolder(), this.fileName + ".yml");
        }

        if (!this.file.exists()) {
            this.plugin.saveResource(this.fileName + ".yml", false);
        }
    }
}
