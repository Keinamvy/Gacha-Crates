package com.gmail.cparse2021.gachacrates.listeners;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.menu.Menu;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import com.gmail.cparse2021.gachacrates.struct.crate.CrateOpenPhase;
import com.gmail.cparse2021.gachacrates.struct.crate.CrateSession;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class CrateListener implements Listener {
    private final GachaCrates plugin;
    private final Set<UUID> destroyingCrate = new HashSet<>();
    private final HashMap<UUID, UUID> crateDestructionMap = new HashMap<>();
    private final HashMap<UUID, Integer> taskMap = new HashMap<>();

    public CrateListener(GachaCrates plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Block clickedBlock = e.getClickedBlock();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null) {
            Optional<Crate> crate = this.plugin.getCrateCache().getCrate(clickedBlock.getLocation());
            if (crate.isPresent()) {
                Player player = e.getPlayer();
                CrateSession crateSession = this.plugin.getSessionManager().getCrateSession(player.getUniqueId());
                Optional<Menu> crateMenu = this.plugin.getMenuManager().getMenu("crate");
                if (crateSession == null) {
                    crateSession = new CrateSession(player.getUniqueId(), crate.get(), clickedBlock.getLocation());
                } else {
                    crateSession.setCrate(crate.get());
                    crateSession.setCrateLocation(clickedBlock.getLocation());
                }

                e.setCancelled(true);
                if (crate.get().isCrateLocationInUse(clickedBlock.getLocation())) {
                    Lang.ERR_CRATE_IN_USE.send(player);
                } else if (crateSession.getOpenPhase() == CrateOpenPhase.OPENING) {
                    Lang.ERR_OPENING_CRATE.send(player);
                } else if (crateMenu.isEmpty()) {
                    Lang.ERR_UNKNOWN.send(player);
                } else {
                    this.plugin.getSessionManager().registerSession(crateSession);
                    crateMenu.get().open(player);
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        Optional<Crate> crate = this.plugin.getCrateCache().getCrate(block.getLocation());
        HashMap<String, String> messageReplacements = new HashMap<>();
        if (crate.isPresent()) {
            if (!player.hasPermission("gachacrates.admin.removelocation")) {
                Lang.ERR_MISSING_PERM.send(player);
                e.setCancelled(true);
            } else {
                messageReplacements.put("%crate%", crate.get().getName());
                if (!this.destroyingCrate.contains(player.getUniqueId())) {
                    e.setCancelled(true);
                    this.startDestruction(player, crate.get());
                    Lang.CRATE_CONFIRM_DELETE.send(player, messageReplacements);
                } else {
                    Optional<Crate> selectedCrate = this.plugin.getCrateCache().getCrate(this.crateDestructionMap.get(player.getUniqueId()));
                    if (selectedCrate.isEmpty()) {
                        e.setCancelled(true);
                        this.destroyingCrate.remove(player.getUniqueId());
                        this.crateDestructionMap.remove(player.getUniqueId());
                        Lang.ERR_UNKNOWN.send(player);
                    } else if (!selectedCrate.get().getUuid().equals(crate.get().getUuid())) {
                        e.setCancelled(true);
                        this.startDestruction(player, crate.get());
                        Lang.CRATE_CONFIRM_DELETE.send(player, messageReplacements);
                    } else {
                        crate.get().removeLocation(block.getLocation());
                        Lang.CRATE_LOCATION_REMOVED.send(player, messageReplacements);
                        plugin.saveData();
                    }
                }
            }
        }
    }

    private void startDestruction(Player player, Crate crate) {
        if (this.taskMap.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(this.taskMap.get(player.getUniqueId()));
            this.taskMap.remove(player.getUniqueId());
        }

        this.destroyingCrate.add(player.getUniqueId());
        this.crateDestructionMap.put(player.getUniqueId(), crate.getUuid());
        this.taskMap.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
            this.destroyingCrate.remove(player.getUniqueId());
            this.crateDestructionMap.remove(player.getUniqueId());
        }, 60L).getTaskId());
    }
}
