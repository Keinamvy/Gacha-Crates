package com.gmail.cparse2021.gachacrates;

import com.gmail.cparse2021.gachacrates.cache.CrateCache;
import com.gmail.cparse2021.gachacrates.cache.GachaConfig;
import com.gmail.cparse2021.gachacrates.cache.PlayerCache;
import com.gmail.cparse2021.gachacrates.cache.SessionManager;
import com.gmail.cparse2021.gachacrates.commands.*;
import com.gmail.cparse2021.gachacrates.file.CustomFile;
import com.gmail.cparse2021.gachacrates.file.FileManager;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.listeners.CrateListener;
import com.gmail.cparse2021.gachacrates.listeners.MenuListener;
import com.gmail.cparse2021.gachacrates.listeners.PlayerListener;
import com.gmail.cparse2021.gachacrates.menu.MenuManager;
import com.gmail.cparse2021.gachacrates.menu.menus.CrateMenu;
import com.gmail.cparse2021.gachacrates.menu.menus.CrateOpenMenu;
import com.gmail.cparse2021.gachacrates.menu.menus.PullMenu;
import com.gmail.cparse2021.gachacrates.menu.menus.RewardsMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class GachaCrates extends JavaPlugin {
    private final FileManager fileManager = new FileManager(this);
    private final SessionManager sessionManager = new SessionManager();
    private final CustomFile cratesFile = this.fileManager.getFile("crates");
    private final CustomFile dataFile = this.fileManager.getFile("data");
    private final CustomFile langFile = this.fileManager.getFile("lang");
    private final CustomFile menusFile = this.fileManager.getFile("menus");
    private CrateCache crateCache;
    private PlayerCache playerCache;
    private GachaConfig gachaConfig;
    private MenuManager menuManager;

    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        try {
            this.registerConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.registerCommands();
        this.registerListeners();
        try {
            this.registerMenus();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, this::saveData, 0L, 12000L);
    }

    public void onDisable() {
        this.saveData();
    }

    public CrateCache getCrateCache() {
        return this.crateCache;
    }

    public GachaConfig getGachaConfig() {
        return gachaConfig;
    }

    public MenuManager getMenuManager() {
        return this.menuManager;
    }

    public PlayerCache getPlayerCache() {
        return this.playerCache;
    }

    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    private void registerCommands() {
        PluginCommand crateCommand = this.getCommand("crate");
        CrateCommandExecutor commandExecutor = new CrateCommandExecutor(this);
        commandExecutor.addCommands(
                new CmdSet(this),
                new CmdRemove(this),
                new CmdGive(this),
                new CmdTake(this),
                new CmdGiveAll(this),
                new CmdList(this),
                new CmdCheck(this),
                new CmdReload(this)
        );
        if (crateCommand != null) {
            crateCommand.setExecutor(commandExecutor);
        }
    }

    private void registerConfig() throws IOException {
        this.saveDefaultConfig();
        this.cratesFile.saveDefaultConfig();
        this.dataFile.saveDefaultConfig();
        this.langFile.saveDefaultConfig();
        this.menusFile.saveDefaultConfig();
        gachaConfig = new GachaConfig();
        gachaConfig.load(getConfig());
        this.crateCache = new CrateCache();
        this.crateCache.loadFrom(this.cratesFile.getConfig());
        this.playerCache = new PlayerCache(this);
        this.playerCache.setFile(this.dataFile.getConfig());
        Lang.setFileConfiguration(this.langFile.getConfig());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CrateListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
    }

    private void registerMenus() throws IOException {
        CrateMenu crateMenu = new CrateMenu(this);
        PullMenu pullMenu = new PullMenu(this);
        RewardsMenu rewardsMenu = new RewardsMenu(this);
        CrateOpenMenu crateOpenMenu = new CrateOpenMenu(this);
        menuManager = new MenuManager();
        crateMenu.load(this.menusFile.getConfig().getConfigurationSection("Crate-Menu"));
        pullMenu.load(this.menusFile.getConfig().getConfigurationSection("Pull-Menu"));
        rewardsMenu.load(this.menusFile.getConfig().getConfigurationSection("Rewards-Menu"));
        crateOpenMenu.load(this.menusFile.getConfig().getConfigurationSection("Crate-Open-Menu"));
        this.menuManager.addMenu(crateMenu, pullMenu, rewardsMenu, crateOpenMenu);
    }

    public void reload() throws IOException {
        this.reloadConfig();
        this.fileManager.reloadAllFiles();

        CrateMenu crateMenu = new CrateMenu(this);
        PullMenu pullMenu = new PullMenu(this);
        RewardsMenu rewardsMenu = new RewardsMenu(this);
        CrateOpenMenu crateOpenMenu = new CrateOpenMenu(this);

        menuManager = new MenuManager();
        crateMenu.load(this.menusFile.getConfig().getConfigurationSection("Crate-Menu"));
        pullMenu.load(this.menusFile.getConfig().getConfigurationSection("Pull-Menu"));
        rewardsMenu.load(this.menusFile.getConfig().getConfigurationSection("Rewards-Menu"));
        crateOpenMenu.load(this.menusFile.getConfig().getConfigurationSection("Crate-Open-Menu"));
        menuManager.addMenu(crateMenu, pullMenu, rewardsMenu, crateOpenMenu);

        gachaConfig = new GachaConfig();
        gachaConfig.load(getConfig());

        this.crateCache = new CrateCache();
        this.crateCache.loadFrom(this.cratesFile.getConfig());
        this.playerCache = new PlayerCache(this);
        this.playerCache.setFile(this.dataFile.getConfig());
        Lang.setFileConfiguration(this.langFile.getConfig());
        this.saveData();
    }

    public void saveData() {
        this.playerCache.saveTo(this.dataFile);
        this.crateCache.saveTo(this.cratesFile);
        Bukkit.getLogger().info("[GachaCrates] Saved Data");
    }
}
