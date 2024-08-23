package com.gmail.cparse2021.gachacrates.menu.menus;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.menu.Menu;
import com.gmail.cparse2021.gachacrates.menu.MenuManager;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.crate.CrateSession;
import com.gmail.cparse2021.gachacrates.util.ItemBuilder;
import com.gmail.cparse2021.gachacrates.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class PullMenu extends Menu {
    private final GachaCrates plugin;
    private final HashMap<UUID, Integer> pullCountMap = new HashMap<>();
    private final HashMap<UUID, ItemStack> offhandSnapshotMap = new HashMap<>();
    private String title = "Pull Menu";
    private ItemStack backgroundItem = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setDisplayName("&7").build();
    private ItemStack pullCountItem = new ItemBuilder(Material.NETHER_STAR).setDisplayName("&ePull %pull-count%x").build();
    private ItemStack decreasePullCountItem = new ItemBuilder(Material.IRON_INGOT).setDisplayName("&eDecrease Pull Count").build();
    private ItemStack increasePullCountItem = new ItemBuilder(Material.GOLD_INGOT).setDisplayName("&aIncrease Pull Count").build();
    private ItemStack maxPullCountSelectorItem = new ItemBuilder(Material.DIAMOND).setDisplayName("&eMax Pull Count").build();
    private ItemStack pullItem = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setDisplayName("&aPull").build();
    private ItemStack backItem = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("&cBack").build();
    private ItemStack maxPullCountItem = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("&cMaximum Pull Count Reached").build();
    private ItemStack minPullCountItem = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("&cMinimum Pull Count Reached").build();

    public PullMenu(GachaCrates plugin) {
        super("pull");
        this.plugin = plugin;
    }

    @Override
    public void load(ConfigurationSection configurationSection) throws IOException {
        if (configurationSection != null) {
            this.title = Utils.formatString(configurationSection.getString("Title"));
            this.backgroundItem = Utils.decodeItem(configurationSection.getString("Background-Item", "WHITE_STAINED_GLASS_PANE name:&7"));
            this.pullCountItem = Utils.decodeItem(
                    configurationSection.getString(
                            "Pull-Count-Item", "NETHER_STAR name:&6&lPull_%pull-count%x lore:&fPulling_&e%pull-count%_&ftimes|&f|&7Increase_or_decrease_amount_below"
                    )
            );
            this.decreasePullCountItem = Utils.decodeItem(
                    configurationSection.getString("Decrease-Pull-Count-Item", "IRON_INGOT name:&cDecrease_Pull_Count lore:&7Click_to_&clower&7_your_pull_count_by_1")
            );
            this.increasePullCountItem = Utils.decodeItem(
                    configurationSection.getString("Increase-Pull-Count-Item", "GOLD_INGOT name:&aIncrease_Pull_Count lore:&7Click_to_&craise&7_your_pull_count_by_1")
            );
            this.maxPullCountSelectorItem = Utils.decodeItem(
                    configurationSection.getString("Max-Pull-Count-Selector-Item", "DIAMOND name:&eMax_Pull_Count lore:&7Click_to_set_your_pull_count_to_the_max")
            );
            this.pullItem = Utils.decodeItem(configurationSection.getString("Pull-Item", "LIME_STAINED_GLASS_PANE name:&aPull lore:&7Click_to_pull"));
            this.backItem = Utils.decodeItem(
                    configurationSection.getString("Back-Item", "RED_STAINED_GLASS_PANE name:&cPrevious_Menu lore:&7Click_to_return_to_the_previous_menu")
            );
            this.maxPullCountItem = Utils.decodeItem(
                    configurationSection.getString(
                            "Max-Pull-Count-Item", "RED_STAINED_GLASS_PANE name:&cMaximum_Pull_Count_Reached lore:&7Can_no_longer_increase_pull_count"
                    )
            );
            this.minPullCountItem = Utils.decodeItem(
                    configurationSection.getString(
                            "Min-Pull-Count-Item", "RED_STAINED_GLASS_PANE name:&cMinimum_Pull_Count_Reached lore:&7Can_no_longer_decrease_pull_count"
                    )
            );
        }
    }

    @Override
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, this.title);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, this.backgroundItem);
        }

        HashMap<String, String> variableMap = new HashMap<>();
        variableMap.put("%pull-count%", "1");
        ItemBuilder pullCountItemBuilder = new ItemBuilder(this.pullCountItem.clone()).setVariables(variableMap);
        inventory.setItem(4, pullCountItemBuilder.build());
        inventory.setItem(12, this.decreasePullCountItem);
        inventory.setItem(13, this.increasePullCountItem);
        inventory.setItem(14, this.maxPullCountSelectorItem);
        inventory.setItem(18, this.backItem);
        inventory.setItem(26, this.pullItem);
        this.offhandSnapshotMap.put(player.getUniqueId(), player.getInventory().getItemInOffHand());
        player.openInventory(inventory);
        this.plugin.getMenuManager().setActiveMenu(player.getUniqueId(), this);
    }

    @Override
    public void processClick(final InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        GachaPlayer gachaPlayer = this.plugin.getPlayerCache().getPlayer(player.getUniqueId());
        CrateSession crateSession = this.plugin.getSessionManager().getCrateSession(player.getUniqueId());
        MenuManager menuManager = this.plugin.getMenuManager();
        int pullCount = this.pullCountMap.getOrDefault(player.getUniqueId(), 1);
        if (crateSession == null) {
            player.closeInventory();
            Lang.ERR_UNKNOWN.send(player);
        } else if (menuManager.isOnCooldown(player.getUniqueId())) {
            menuManager.addCooldown(player.getUniqueId());
            if (e.getSlot() == 12 && e.getCurrentItem() != null && e.getCurrentItem().isSimilar(this.decreasePullCountItem)) {
                if (pullCount == 1) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                    e.getInventory().setItem(e.getSlot(), this.minPullCountItem);
                    (new BukkitRunnable() {
                        public void run() {
                            e.getInventory().setItem(e.getSlot(), PullMenu.this.decreasePullCountItem);
                        }
                    }).runTaskLater(this.plugin, 20L);
                } else {
                    this.pullCountMap.put(player.getUniqueId(), --pullCount);
                    this.updatePullCountItem(player, pullCount);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
                }
            } else if (e.getSlot() == 13 && e.getCurrentItem() != null && e.getCurrentItem().isSimilar(this.increasePullCountItem)) {
                if (pullCount < gachaPlayer.getAvailablePulls(crateSession.getCrate()) && pullCount < plugin.getGachaConfig().getMaxPull()) {
                    this.pullCountMap.put(player.getUniqueId(), ++pullCount);
                    this.updatePullCountItem(player, pullCount);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                    e.getInventory().setItem(e.getSlot(), this.maxPullCountItem);
                    (new BukkitRunnable() {
                        public void run() {
                            e.getInventory().setItem(e.getSlot(), PullMenu.this.increasePullCountItem);
                        }
                    }).runTaskLater(this.plugin, 20L);
                }
            } else {
                if (e.getSlot() == 14 && e.getCurrentItem() != null && e.getCurrentItem().isSimilar(this.maxPullCountSelectorItem)) {
                    pullCount = Math.min(plugin.getGachaConfig().getMaxPull(), gachaPlayer.getAvailablePulls(crateSession.getCrate()));
                    this.pullCountMap.put(player.getUniqueId(), pullCount);
                    this.updatePullCountItem(player, pullCount);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                }

                if (e.getSlot() == 18) {
                    Optional<Menu> crateMenu = this.plugin.getMenuManager().getMenu("crate");
                    if (crateMenu.isEmpty()) {
                        player.closeInventory();
                        Lang.ERR_UNKNOWN.send(player);
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
                        crateMenu.get().open(player);
                    }
                } else {
                    if (e.getSlot() == 26) {
                        Optional<Menu> crateOpenMenu = this.plugin.getMenuManager().getMenu("crate-open");
                        if (crateOpenMenu.isEmpty()) {
                            player.closeInventory();
                            Lang.ERR_UNKNOWN.send(player);
                            return;
                        }

                        if (gachaPlayer.getAvailablePulls(crateSession.getCrate()) < pullCount) {
                            player.closeInventory();
                            Lang.ERR_NOT_ENOUGH_PULLS.send(player);
                            return;
                        }

                        player.closeInventory();
                        gachaPlayer.setAvailablePulls(crateSession.getCrate(), gachaPlayer.getAvailablePulls(crateSession.getCrate()) - pullCount);
                        this.plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
                        crateSession.getCrate().open(this.plugin, gachaPlayer, crateSession, pullCount, crateOpenMenu.get());
                    }
                }
            }
        }
    }

    @Override
    public void processClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (this.offhandSnapshotMap.containsKey(player.getUniqueId())) {
            player.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());
        }

        this.pullCountMap.remove(player.getUniqueId());
        this.plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
    }

    private void updatePullCountItem(Player player, int pullCount) {
        HashMap<String, String> variableMap = new HashMap<>();
        variableMap.put("%pull-count%", Integer.toString(pullCount));
        ItemBuilder pullCountItemBuilder = new ItemBuilder(this.pullCountItem.clone()).setVariables(variableMap);
        player.getOpenInventory().getTopInventory().setItem(4, pullCountItemBuilder.build());
    }
}
