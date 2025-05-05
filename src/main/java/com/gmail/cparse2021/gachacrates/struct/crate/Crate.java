package com.gmail.cparse2021.gachacrates.struct.crate;

import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.menu.Menu;
import com.gmail.cparse2021.gachacrates.menu.menus.CrateOpenMenu;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.reward.Reward;
import com.gmail.cparse2021.gachacrates.struct.reward.RewardTier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;

public class Crate {
    private final LinkedHashMap<RewardTier, Double> rewardProbabilityMap = new LinkedHashMap<>();
    private final String name;
    private final Set<Location> crateLocations = new HashSet<>();
    private UUID uuid;
    private AnimationType animationType;

    public Crate(String name) {
        this.name = name;
    }

    public void addLocation(Location location) {
        crateLocations.add(location);
    }

    /**
     * Generate a random reward tier based on set probability
     *
     * @return Generated RewardTier
     */
    public RewardTier generateRewardTier() {
        double randDouble = Math.random();

        for (Map.Entry<RewardTier, Double> rewardProbability : rewardProbabilityMap.entrySet()) {
            if (randDouble <= rewardProbability.getValue()) {
                return rewardProbability.getKey();
            }
        }

        return rewardProbabilityMap.entrySet().iterator().next().getKey();
    }

    /**
     * Generate a random reward tier based on set probability and pity for a player
     *
     * @return Generated RewardTier
     */
    public RewardTier generateRewardTier(GachaPlayer gachaPlayer) {
        double randDouble = Math.random();
        double count = 0.0;

        for (Map.Entry<RewardTier, Double> rewardProbability : rewardProbabilityMap.entrySet()) {
            RewardTier rewardTier = rewardProbability.getKey();
            count += rewardProbability.getValue();
            if (rewardTier.isInsuranceEnabled()) {
                if (randDouble * (1 - (gachaPlayer.getPity(this, rewardTier) * 1.41) / 100) <= count ||
                        (rewardTier.isPityEnabled() && gachaPlayer.getPity(this, rewardTier) >= rewardTier.getPityLimit() - 1)) {
                    return rewardTier;
                }
            } else if (randDouble <= count ||
                    (rewardTier.isPityEnabled() && gachaPlayer.getPity(this, rewardTier) >= rewardTier.getPityLimit() - 1)) {
                return rewardTier;
            }
        }

        return rewardProbabilityMap.entrySet().iterator().next().getKey();
    }

    public LinkedHashSet<Reward> getAllRewards() {
        LinkedHashSet<Reward> rewards = new LinkedHashSet<>();

        getRewardTiers().forEach((r) -> rewards.addAll(r.getRewards()));
        return rewards;
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public double getChance(RewardTier rewardTier) {
        return rewardProbabilityMap.get(rewardTier);
    }

    public Set<Location> getCrateLocations() {
        return crateLocations;
    }

    public String getName() {
        return name;
    }

    public Optional<RewardTier> getRewardTier(String name) {
        return getRewardTiers().stream().filter((r) -> r.getName().equalsIgnoreCase(name)).findFirst();
    }

    public Set<RewardTier> getRewardTiers() {
        return rewardProbabilityMap.keySet();
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isCrateLocation(Location location) {
        for (Location crateLocation : crateLocations) {
            if (crateLocation.getBlockX() == location.getBlockX()
                    && crateLocation.getBlockY() == location.getBlockY()
                    && crateLocation.getBlockZ() == location.getBlockZ()
                    && crateLocation.getWorld() == location.getWorld()) {
                return true;
            }
        }

        return false;
    }


    public void loadFrom(ConfigurationSection config) {
        ConfigurationSection rewardTiers = config.getConfigurationSection("Reward-Tiers");

        this.uuid = UUID.fromString(config.getString("UUID", UUID.randomUUID().toString()));

        try {
            this.animationType = AnimationType.valueOf(config.getString("Animation-Type", "INTERFACE").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.animationType = AnimationType.INTERFACE;
            Bukkit.getLogger().log(Level.WARNING, "[GachaCrates] Invalid animation type specified for crate `" + name + "`");
        }

        // Load reward tiers
        if (rewardTiers != null) {
            for (String rewardTierName : rewardTiers.getKeys(false)) {
                ConfigurationSection rewardTierSection = rewardTiers.getConfigurationSection(rewardTierName);
                RewardTier rewardTier = new RewardTier(rewardTierName);
                double chance = rewardTiers.getDouble(rewardTierName + ".Chance", 50) / 100;

                assert rewardTierSection != null;
                rewardTier.loadFrom(rewardTierSection);
                rewardProbabilityMap.put(rewardTier, chance);
            }

            sortProbabilityMap();
        } else {
            Bukkit.getLogger().log(Level.WARNING, "[GachaCrates] No reward tiers specified for crate `" + name + "`");
        }

        // Load crate locations
        for (String locationString : config.getStringList("Locations")) {
            String[] locationArgs = locationString.split(" ");
            World world = Bukkit.getWorld(locationArgs[0]);
            int x;
            int y;
            int z;

            if (world == null) {
                Bukkit.getLogger().log(Level.SEVERE, "[GachaCrates] Invalid world name specified in crate locations for `" + name + "`: " + locationArgs[0]);
                continue;
            }

            try {
                x = Integer.parseInt(locationArgs[1]);
                y = Integer.parseInt(locationArgs[2]);
                z = Integer.parseInt(locationArgs[3]);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GachaCrates] Invalid x, y, or z specified in crate locations for `" + name + "`: " + locationString);
                continue;
            }

            crateLocations.add(new Location(world, x, y, z));
        }
    }

    public void open(GachaPlayer gachaPlayer, CrateSession crateSession, int pullCount, Menu menu) {
        Crate crate = crateSession.getCrate();

        switch (animationType) {
            case NONE -> {
                for (int i = 0; i < pullCount; i++) {
                    RewardTier rewardTier = crate.generateRewardTier(gachaPlayer);
                    Reward reward = rewardTier.generateReward(gachaPlayer, crate, rewardTier);
                    if (rewardTier.isInsuranceEnabled()) {
                        gachaPlayer.setGuaranteeState(crate, rewardTier, reward.isFeatured());
                    }


                    if (rewardTier.isPityEnabled()) {
                        gachaPlayer.resetPity(crate, rewardTier);
                    }

                    reward.execute(gachaPlayer.getPlayer());
                    gachaPlayer.increasePity(crate, rewardTier, 1);
                }
            }
            case INTERFACE -> {
                if (!(menu instanceof CrateOpenMenu crateOpenMenu)) {
                    Lang.ERR_UNKNOWN.send(gachaPlayer.getPlayer());
                    break;
                }

                crateOpenMenu.open(gachaPlayer, crateSession, pullCount);
            }
        }
    }

    public void removeLocation(Location location) {
        crateLocations.removeIf(crateLocation -> crateLocation.getBlockX() == location.getBlockX()
                && crateLocation.getBlockY() == location.getBlockY()
                && crateLocation.getBlockZ() == location.getBlockZ()
                && crateLocation.getWorld() == location.getWorld());
    }

    /**
     * Sort the probability map
     */
    private void sortProbabilityMap() {
        LinkedList<Map.Entry<RewardTier, Double>> probabilityMapList = new LinkedList<>(rewardProbabilityMap.entrySet());

        rewardProbabilityMap.clear();
        probabilityMapList.sort(Map.Entry.comparingByValue());
        probabilityMapList.forEach((e) -> rewardProbabilityMap.put(e.getKey(), e.getValue()));
    }
}