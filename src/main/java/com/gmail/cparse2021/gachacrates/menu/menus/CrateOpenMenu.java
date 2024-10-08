package com.gmail.cparse2021.gachacrates.menu.menus;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.menu.Menu;
import com.gmail.cparse2021.gachacrates.menu.MenuManager;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import com.gmail.cparse2021.gachacrates.struct.crate.CrateOpenPhase;
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
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class CrateOpenMenu extends Menu {
    private final GachaCrates plugin;
    private final HashMap<UUID, ItemStack> offhandSnapshotMap = new HashMap<>();
    private String title = "&6&lPull Rewards";
    private ItemStack countdownItem3 = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).setDisplayName("&7").build();
    private ItemStack countdownItem2 = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).setDisplayName("&7").build();
    private ItemStack countdownItem1 = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("&7").build();

    public CrateOpenMenu(GachaCrates plugin) {
        super("crate-open");
        this.plugin = plugin;
    }

    @Override
    public void load(@Nullable ConfigurationSection configurationSection) throws IOException {
        if (configurationSection != null) {
            this.title = Utils.formatString(configurationSection.getString("Title", "&6&lPull Rewards"));
            this.countdownItem1 = Utils.decodeItem(configurationSection.getString("Countdown-Item-1", "GRAY_STAINED_GLASS_PANE name:&7Revealing_in_1s"));
            this.countdownItem2 = Utils.decodeItem(configurationSection.getString("Countdown-Item-2", "YELLOW_STAINED_GLASS_PANE name:&7Revealing_in_2s"));
            this.countdownItem3 = Utils.decodeItem(configurationSection.getString("Countdown-Item-3", "ORANGE_STAINED_GLASS_PANE name:&7Revealing_in_3s"));
        }
    }

    @Override
    public void open(Player player) {
    }


    public void open(GachaPlayer gachaPlayer, final CrateSession crateSession, int pullCount) {
        int rows = Math.min(pullCount % 9 > 0 ? pullCount / 9 + 1 : pullCount / 9, 6);
        final Inventory inventory = Bukkit.createInventory(null, rows * 9, this.title);
        final HashMap<Integer, RewardTier> rewardTiers = new HashMap<>();
        HashMap<Integer, Reward> rewards = new HashMap<>();
        Crate crate = crateSession.getCrate();
        final Player player = gachaPlayer.getPlayer();

        for (int i = 0; i < pullCount; i++) {
            RewardTier rewardTier = crate.generateRewardTier(gachaPlayer);
            Reward reward = rewardTier.generateReward(gachaPlayer, crate, rewardTier);
            if (rewardTier.isInsuranceEnabled()) {
                gachaPlayer.setGuaranteeState(crate, rewardTier, reward.isFeatured());
            }

            if (rewardTier.isPityEnabled()) {
                gachaPlayer.resetPity(crate, rewardTier);
            }

            rewards.put(i, reward);
            rewardTiers.put(i, rewardTier);
            gachaPlayer.increasePity(crate, rewardTier, 1);
            inventory.setItem(i, this.countdownItem3);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            for (int ix = 0; ix < pullCount; ix++) {
                inventory.setItem(ix, this.countdownItem2);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        }, 20L);
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            for (int ix = 0; ix < pullCount; ix++) {
                inventory.setItem(ix, this.countdownItem1);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        }, 40L);

        (new BukkitRunnable() {
            int counter = 0;

            public void run() {
                RewardTier rewardTier = rewardTiers.get(this.counter);
                if (rewardTier == null) {
                    crateSession.setOpenPhase(CrateOpenPhase.COMPLETE);
                    this.cancel();
                } else if (crateSession.getOpenPhase() == CrateOpenPhase.COMPLETE) {
                    this.cancel();
                } else {
                    inventory.setItem(this.counter++, rewardTier.getDisplayItem());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
                }
            }
        }).runTaskTimer(this.plugin, 60L, 3L);
        crateSession.setRewards(rewards);
        crateSession.setOpenPhase(CrateOpenPhase.OPENING);
        this.offhandSnapshotMap.put(player.getUniqueId(), player.getInventory().getItemInOffHand());
        player.openInventory(inventory);
        this.plugin.getMenuManager().setActiveMenu(player.getUniqueId(), this);
    }

    @Override
    public void processClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        CrateSession crateSession = this.plugin.getSessionManager().getCrateSession(player.getUniqueId());
        MenuManager menuManager = this.plugin.getMenuManager();
        if (crateSession == null) {
            player.closeInventory();
            Lang.ERR_UNKNOWN.send(player);
            this.plugin.getSessionManager().clearSession(player.getUniqueId());
            this.plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
        } else if (menuManager.isOnCooldown(player.getUniqueId())) {
            menuManager.addCooldown(player.getUniqueId());
            if (e.getCurrentItem() != null) {
                Reward reward = crateSession.getReward(e.getSlot());
                if (reward != null) {
                    player.getOpenInventory().getTopInventory().setItem(e.getSlot(), reward.getDisplayItem());
                }
            }
        }
    }

    @Override
    public void processClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (player == null) {
            return;
        }
        CrateSession crateSession = this.plugin.getSessionManager().getCrateSession(player.getUniqueId());
        if (this.offhandSnapshotMap.containsKey(player.getUniqueId())) {
            player.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());
        }

        if (crateSession == null) {
            this.plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
        } else {
            crateSession.setOpenPhase(CrateOpenPhase.COMPLETE);
            for (Reward reward : crateSession.getRewards()) {
                reward.execute(player);
            }

            this.plugin.getSessionManager().clearSession(player.getUniqueId());
            this.plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
        }
    }
}
