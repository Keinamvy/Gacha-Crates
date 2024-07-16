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
import java.util.HashMap;
import java.util.UUID;

public class CrateOpenMenu extends Menu {
    private final GachaCrates plugin;
    private final HashMap<UUID, ItemStack> offhandSnapshotMap = new HashMap<>();
    private String title = "&6&lPull Rewards";

    public CrateOpenMenu(GachaCrates plugin) {
        super("crate-open");
        this.plugin = plugin;
    }

    @Override
    public void load(@Nullable ConfigurationSection configurationSection) {
        if (configurationSection != null) {
            this.title = Utils.formatString(configurationSection.getString("Title", "&6&lPull Rewards"));
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
        }

        (new BukkitRunnable() {
            int counter = 0;

            public void run() {
                RewardTier rewardTier = rewardTiers.get(this.counter);
                if (rewardTier == null) {
                    crateSession.setOpenPhase(CrateOpenPhase.COMPLETE);
                    this.cancel();
                } else {
                    inventory.setItem(this.counter++, rewardTier.getDisplayItem());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
                }
            }
        }).runTaskTimer(this.plugin, 0L, 7L);
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
        } else if (!menuManager.isOnCooldown(player.getUniqueId())) {
            menuManager.addCooldown(player.getUniqueId());
            if (crateSession.getOpenPhase() == CrateOpenPhase.COMPLETE && e.getCurrentItem() != null) {
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
        CrateSession crateSession = this.plugin.getSessionManager().getCrateSession(player.getUniqueId());
        if (this.offhandSnapshotMap.containsKey(player.getUniqueId())) {
            player.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());
        }

        if (crateSession == null) {
            this.plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
        } else if (crateSession.getOpenPhase() == CrateOpenPhase.OPENING) {
            player.openInventory(e.getInventory());
        } else {
            for (Reward reward : crateSession.getRewards()) {
                reward.execute(player);
            }

            this.plugin.getSessionManager().clearSession(player.getUniqueId());
            this.plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
        }
    }
}
