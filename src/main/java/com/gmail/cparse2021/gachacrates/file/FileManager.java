package com.gmail.cparse2021.gachacrates.file;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class FileManager {
    private final HashMap<String, CustomFile> fileHashMap = new HashMap<>();
    private final JavaPlugin plugin;

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CustomFile getFile(String name) {
        CustomFile customFile = null;

        for (String key : this.fileHashMap.keySet()) {
            if (key.equalsIgnoreCase(name)) {
                customFile = this.fileHashMap.get(name);
            }
        }

        if (customFile == null) {
            customFile = new CustomFile(this.plugin, name);
            this.fileHashMap.put(name, customFile);
        }

        return customFile;
    }

    public void reloadAllFiles() {
        this.fileHashMap.values().forEach(CustomFile::reloadConfig);
    }

    public void saveAllFiles() {
        this.fileHashMap.values().forEach(CustomFile::saveConfig);
    }
}
