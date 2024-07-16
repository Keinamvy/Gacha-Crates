package com.gmail.cparse2021.gachacrates.cache;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.file.CustomFile;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import com.gmail.cparse2021.gachacrates.struct.reward.RewardTier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

public class PlayerCache {
    private final HashMap<UUID, GachaPlayer> playerCache = new HashMap<>();
    private final GachaCrates plugin;
    private FileConfiguration fileConfiguration;

    public PlayerCache(GachaCrates plugin) {
        this.plugin = plugin;
    }

    public GachaPlayer getPlayer(UUID uuid) {
        if (this.playerCache.containsKey(uuid)) {
            return this.playerCache.get(uuid);
        } else {
            GachaPlayer gachaPlayer = new GachaPlayer(uuid);
            ConfigurationSection dataSection = this.fileConfiguration.getConfigurationSection(uuid.toString());
            if (dataSection != null) {
                for (String crateUuid : dataSection.getKeys(false)) {
                    Optional<Crate> crate = this.plugin.getCrateCache().getCrate(UUID.fromString(crateUuid));
                    ConfigurationSection pityMapSection = dataSection.getConfigurationSection(crateUuid + ".Pity-Map");
                    if (!crate.isEmpty()) {
                        if (pityMapSection != null) {
                            for (String rewardTierName : pityMapSection.getKeys(false)) {
                                Optional<RewardTier> rewardTier = crate.get().getRewardTier(rewardTierName);
                                if (!rewardTier.isEmpty()) {
                                    gachaPlayer.setPity(crate.get(), rewardTier.get(), pityMapSection.getInt(rewardTierName, 0));
                                }
                            }
                        }

                        gachaPlayer.setAvailablePulls(crate.get(), dataSection.getInt(crateUuid + ".Pulls", 0));
                    }
                }
            }

            this.playerCache.put(uuid, gachaPlayer);
            return gachaPlayer;
        }
    }

    public void saveTo(CustomFile customFile) {
        for (GachaPlayer gachaPlayer : this.playerCache.values()) {
            for (Entry<Crate, HashMap<RewardTier, Integer>> entry : gachaPlayer.getPityMap().entrySet()) {
                Crate crate = entry.getKey();

                for (Entry<RewardTier, Integer> pityMap : entry.getValue().entrySet()) {
                    customFile.getConfig()
                            .set(gachaPlayer.getUuid().toString() + "." + crate.getUuid().toString() + ".Pity-Map." + pityMap.getKey().getName(), pityMap.getValue());
                }
            }

            for (Entry<Crate, HashMap<RewardTier, Boolean>> entry : gachaPlayer.getInsuranceMap().entrySet()) {
                Crate crate = entry.getKey();

                for (Entry<RewardTier, Boolean> insuranceMap : entry.getValue().entrySet()) {
                    customFile.getConfig()
                            .set(
                                    gachaPlayer.getUuid().toString() + "." + crate.getUuid().toString() + ".Insurance." + insuranceMap.getKey().getName(),
                                    insuranceMap.getValue()
                            );
                }
            }

            for (Crate crate : gachaPlayer.getPullMap().keySet()) {
                customFile.getConfig().set(gachaPlayer.getUuid().toString() + "." + crate.getUuid().toString() + ".Pulls", gachaPlayer.getAvailablePulls(crate));
            }
        }

        customFile.saveConfig();
    }

    public void setFile(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }
}
