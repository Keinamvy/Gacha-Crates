package com.gmail.cparse2021.gachacrates.cache;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.file.CustomFile;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import com.gmail.cparse2021.gachacrates.struct.reward.RewardTier;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class PlayerCache {
    private HashMap<UUID, GachaPlayer> playerCache = new HashMap<>();
    private final GachaCrates plugin;
    private FileConfiguration fileConfiguration;

    public PlayerCache(GachaCrates plugin) {
        this.plugin = plugin;
    }

    /**
     * Get a GachaPlayer based on UUID, if none is found, a new one will be created and data will attempt to load
     *
     * @param uuid The GachaPlayer UUID
     * @return GachaPlayer
     */
    public GachaPlayer getPlayer(UUID uuid) {
        if (playerCache.containsKey(uuid)) {
            return playerCache.get(uuid);
        } else {
            GachaPlayer gachaPlayer = new GachaPlayer(uuid);
            ConfigurationSection dataSection = fileConfiguration.getConfigurationSection(uuid.toString());

            if (dataSection != null) {
                for (String crateUuid : dataSection.getKeys(false)) {
                    Optional<Crate> crate = plugin.getCrateCache().getCrate(UUID.fromString(crateUuid));
                    ConfigurationSection pityMapSection = dataSection.getConfigurationSection(crateUuid + ".Pity-Map");
                    ConfigurationSection GuaranteedStateSection = dataSection.getConfigurationSection(crateUuid + ".GuaranteedState");
                    if (crate.isEmpty()) {
                        continue;
                    }

                    if (pityMapSection != null) {
                        for (String rewardTierName : pityMapSection.getKeys(false)) {
                            Optional<RewardTier> rewardTier = crate.get().getRewardTier(rewardTierName);
                            if (rewardTier.isEmpty()) {
                                continue;
                            }
                            gachaPlayer.setPity(crate.get(), rewardTier.get(), pityMapSection.getInt(rewardTierName, 0));
                        }
                    }
                    if (GuaranteedStateSection != null) {
                        for (String guaranteedState : GuaranteedStateSection.getKeys(false)) {
                            Optional<RewardTier> rewardTier = crate.get().getRewardTier(guaranteedState);
                            if (rewardTier.isEmpty()) {
                                continue;
                            }
                            gachaPlayer.setGuaranteedState(crate.get(), rewardTier.get(), GuaranteedStateSection.getBoolean(guaranteedState, false));
                        }
                    }
                    gachaPlayer.setAvailablePulls(crate.get(), dataSection.getInt(crateUuid + ".Pulls", 0));
                }
            }

            playerCache.put(uuid, gachaPlayer);
            return gachaPlayer;
        }
    }

    /**
     * Save cached data to file
     *
     * @param customFile The CustomFile to save to
     */
    public void saveTo(CustomFile customFile) {
        for (GachaPlayer gachaPlayer : playerCache.values()) {
            for (Map.Entry<Crate, HashMap<RewardTier, Integer>> entry : gachaPlayer.getPityMap().entrySet()) {
                Crate crate = entry.getKey();

                for (Map.Entry<RewardTier, Integer> pityMap : entry.getValue().entrySet()) {
                    customFile.getConfig().set(gachaPlayer.getUuid().toString() + "." + crate.getUuid().toString() + ".Pity-Map." + pityMap.getKey().getName(), pityMap.getValue());
                }
            }

            for (Crate crate : gachaPlayer.getPullMap().keySet()) {
                customFile.getConfig().set(gachaPlayer.getUuid().toString() + "." + crate.getUuid().toString() + ".Pulls", gachaPlayer.getAvailablePulls(crate));
            }
            for (Map.Entry<Crate, HashMap<RewardTier, Boolean>> entry : gachaPlayer.getGuaranteedMap().entrySet()) {
                Crate crate = entry.getKey();

                for (Map.Entry<RewardTier, Boolean> guaranteeMap : entry.getValue().entrySet()) {
                    customFile.getConfig().set(gachaPlayer.getUuid().toString() + "." + crate.getUuid().toString() + ".GuaranteedState." + guaranteeMap.getKey().getName(), guaranteeMap.getValue());
                }
            }
        }

        customFile.saveConfig();
    }

    /**
     * Set data to cache
     *
     * @param fileConfiguration The FileConfiguration containing cacheable data
     */
    public void setFile(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }
}