package com.gmail.cparse2021.gachacrates.menu.menus;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.menu.Menu;
import com.gmail.cparse2021.gachacrates.menu.MenuManager;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import com.gmail.cparse2021.gachacrates.struct.crate.CrateSession;
import com.gmail.cparse2021.gachacrates.struct.reward.Reward;
import com.gmail.cparse2021.gachacrates.struct.reward.RewardTier;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

public class RewardsMenu extends Menu {
    private final GachaCrates plugin;
    private final HashMap<UUID, ItemStack> offhandSnapshotMap = new HashMap<>();
    private final HashMap<UUID, Integer> pageMap = new HashMap<>();
    private String title = "Rewards Menu";
    private ItemStack backgroundItem = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setDisplayName("&7").build();
    private ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("&7").build();
    private ItemStack nextPageItem = new ItemBuilder(Material.FEATHER).setDisplayName("&aNext Page").build();
    private ItemStack previousPageItem = new ItemBuilder(Material.ARROW).setDisplayName("&cPrevious Page").build();
    private ItemStack backItem = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("&cBack").build();
    private ItemStack pityItem = new ItemBuilder(Material.NETHER_STAR).setDisplayName("&ePity Tracker").build();
    private ItemStack rateItem = new ItemBuilder(Material.DARK_OAK_SIGN).setDisplayName("&eReward Tier Rates").build();

    public RewardsMenu(GachaCrates plugin) {
        super("rewards");
        this.plugin = plugin;
    }

    @Override
    public void load(ConfigurationSection configurationSection) throws IOException {
        if (configurationSection != null) {
            this.title = Utils.formatString(configurationSection.getString("Title"));
            this.backgroundItem = Utils.decodeItem(configurationSection.getString("Background-Item", "WHITE_STAINED_GLASS_PANE name:&7"));
            this.borderItem = Utils.decodeItem(configurationSection.getString("Border-Item", "GRAY_STAINED_GLASS_PANE name:&7"));
            this.nextPageItem = Utils.decodeItem(configurationSection.getString("Next-Page-Item", "FEATHER name:&aNext Page"));
            this.previousPageItem = Utils.decodeItem(configurationSection.getString("Previous-Page-Item", "ARROW name:&cPrevious Page"));
            this.backItem = Utils.decodeItem(
                    configurationSection.getString("Back-Item", "RED_STAINED_GLASS_PANE name:&cPrevious_Menu lore:&7Click_to_return_to_the_previous_menu")
            );
            this.pityItem = Utils.decodeItem(
                    configurationSection.getString("Pity-Item", "NETHER_STAR name:&2Pull_Tracker lore:&ePity_List|%pity-list%|&bGuaranteed_State_List|%insurance-list%")
            );
            this.rateItem = Utils.decodeItem(configurationSection.getString("Rate-Item", "DARK_OAK_SIGN name:&e&lReward_Tier_Rates lore:%rate-list%"));
        }
    }

    @Override
    public void open(Player player) {
        this.open(player, 1);
        this.plugin.getMenuManager().setActiveMenu(player.getUniqueId(), this);
    }

    public void open(Player player, int page) {
        CrateSession crateSession = this.plugin.getSessionManager().getCrateSession(player.getUniqueId());
        GachaPlayer gachaPlayer = this.plugin.getPlayerCache().getPlayer(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(null, 54, this.title);
        int counter = 0;
        if (crateSession == null) {
            player.closeInventory();
            Lang.ERR_UNKNOWN.send(player);
        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, this.backgroundItem);
            }

            for (int i = 36; i < 45; i++) {
                inventory.setItem(i, this.borderItem);
            }

            if (page > 1) {
                inventory.setItem(36, this.previousPageItem);
            }

            for (Reward reward : crateSession.getCrate().getAllRewards()) {
                if (++counter >= (page - 1) * 36) {
                    if (counter > 36) {
                        inventory.setItem(44, this.nextPageItem);
                        break;
                    }

                    inventory.setItem(counter - 1, reward.getDisplayItem());
                }
            }

            ItemStack newPityItem = this.pityItem.clone();
            ItemStack newRateItem = this.rateItem.clone();
            ItemMeta itemMeta = newPityItem.getItemMeta();
            if (itemMeta != null && itemMeta.getLore() != null) {
                List<String> lore = new ArrayList<>();
                itemMeta.getLore().forEach(l -> {
                    if (l.contains("%pity-list%")) {
                        lore.addAll(this.getPityList(gachaPlayer, crateSession.getCrate()));
                    } else if (l.contains("%insurance-list%")) {
                        lore.addAll(this.getInsuranceList(gachaPlayer, crateSession.getCrate()));
                    } else {
                        lore.add(l);
                    }
                });
                itemMeta.setLore(lore);
                newPityItem.setItemMeta(itemMeta);
            }

            itemMeta = newRateItem.getItemMeta();
            if (itemMeta != null && itemMeta.getLore() != null) {
                List<String> lore = new ArrayList<>();
                itemMeta.getLore().forEach(l -> {
                    if (l.contains("%rate-list%")) {
                        lore.addAll(this.getRateList(crateSession.getCrate()));
                    } else {
                        lore.add(l);
                    }
                });
                itemMeta.setLore(lore);
                newRateItem.setItemMeta(itemMeta);
            }

            inventory.setItem(45, this.backItem);
            inventory.setItem(49, newPityItem);
            inventory.setItem(53, newRateItem);
            this.offhandSnapshotMap.put(player.getUniqueId(), player.getInventory().getItemInOffHand());
            player.openInventory(inventory);
            this.pageMap.put(player.getUniqueId(), page);
        }
    }

    @Override
    public void processClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        MenuManager menuManager = this.plugin.getMenuManager();
        if (menuManager.isOnCooldown(player.getUniqueId())) {
            menuManager.addCooldown(player.getUniqueId());
            if (e.getSlot() == 45) {
                Optional<Menu> crateMenu = this.plugin.getMenuManager().getMenu("crate");
                if (crateMenu.isEmpty()) {
                    player.closeInventory();
                    Lang.ERR_UNKNOWN.send(player);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    crateMenu.get().open(player);
                }
            } else if (e.getSlot() == 52 && Objects.requireNonNull(e.getCurrentItem()).isSimilar(this.previousPageItem)) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
                this.open(player, this.pageMap.getOrDefault(player.getUniqueId(), 2) - 1);
            } else {
                if (e.getSlot() == 53 && Objects.requireNonNull(e.getCurrentItem()).isSimilar(this.nextPageItem)) {
                    this.open(player, this.pageMap.getOrDefault(player.getUniqueId(), 1) + 1);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
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

        this.plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
    }

    private List<String> getPityList(GachaPlayer gachaPlayer, Crate crate) {
        HashMap<RewardTier, Integer> pityMap = gachaPlayer.getPityMap(crate);
        List<String> pityList = new ArrayList<>();

        for (RewardTier rewardTier : crate.getRewardTiers()) {
            if (rewardTier.isPityEnabled()) {
                pityList.add(
                        Lang.PITY_TRACKER_FORMAT
                                .toString(false)
                                .replace("%reward-tier%", rewardTier.getName())
                                .replace("%pity-count%", Integer.toString(pityMap.getOrDefault(rewardTier, 0)))
                                .replace("%pity-limit%", Integer.toString(rewardTier.getPityLimit()))
                );
            }
        }

        return pityList;
    }

    private List<String> getInsuranceList(GachaPlayer gachaPlayer, Crate crate) {
        HashMap<RewardTier, Boolean> guaranteedMap = gachaPlayer.getInsuranceMap(crate);
        List<String> guaranteedList = new ArrayList<>();

        for (RewardTier rewardTier : crate.getRewardTiers()) {
            if (rewardTier.isInsuranceEnabled()) {
                guaranteedList.add(
                        Lang.INSURANCE_TRACKER_FORMAT
                                .toString(false)
                                .replace("%reward-tier%", rewardTier.getName())
                                .replace("%insuranceState%", Boolean.toString(guaranteedMap.getOrDefault(rewardTier, false)))
                );
            }
        }

        return guaranteedList;
    }

    private List<String> getRateList(Crate crate) {
        List<String> rateList = new ArrayList<>();

        for (RewardTier rewardTier : crate.getRewardTiers()) {
            if (rewardTier.isPityEnabled()) {
                rateList.add(
                        Lang.TIER_RATE_FORMAT
                                .toString(false)
                                .replace("%reward-tier%", rewardTier.getName())
                                .replace("%rate%", String.valueOf(crate.getChance(rewardTier) * 100.0))
                );
            }
        }

        return rateList;
    }
}
