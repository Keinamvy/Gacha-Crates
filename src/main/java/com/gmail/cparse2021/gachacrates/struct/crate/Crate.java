package com.gmail.cparse2021.gachacrates.struct.crate;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.menu.Menu;
import com.gmail.cparse2021.gachacrates.menu.menus.CrateOpenMenu;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.reward.Reward;
import com.gmail.cparse2021.gachacrates.struct.reward.RewardTier;
import com.gmail.cparse2021.gachacrates.util.MathUtil;
import com.gmail.cparse2021.gachacrates.util.ParticleUtil;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public class Crate {
    private final LinkedHashMap<RewardTier, Double> rewardProbabilityMap = new LinkedHashMap<>();
    private final String name;
    private UUID uuid;
    private AnimationType animationType;
    private final Set<Location> crateLocations = new HashSet<>();
    private final HashMap<Location, Boolean> inUse = new HashMap<>();

    public Crate(String name) {
        this.name = name;
    }

    public void addLocation(Location location) {
        this.crateLocations.add(location);
    }

    public RewardTier generateRewardTier() {
        double randDouble = Math.random();

        for (Entry<RewardTier, Double> rewardProbability : this.rewardProbabilityMap.entrySet()) {
            if (randDouble <= rewardProbability.getValue()) {
                return rewardProbability.getKey();
            }
        }

        return this.rewardProbabilityMap.entrySet().iterator().next().getKey();
    }

    public RewardTier generateRewardTier(GachaPlayer gachaPlayer) {
        double randDouble = Math.random();
        double count = 0.0;

        for (Entry<RewardTier, Double> rewardProbability : this.rewardProbabilityMap.entrySet()) {
            RewardTier rewardTier = rewardProbability.getKey();
            count += rewardProbability.getValue();
            if (randDouble <= count || rewardTier.isPityEnabled() && gachaPlayer.getPity(this, rewardTier) >= rewardTier.getPityLimit() - 1) {
                return rewardTier;
            }
        }

        return this.rewardProbabilityMap.entrySet().iterator().next().getKey();
    }

    public LinkedHashSet<Reward> getAllRewards() {
        LinkedHashSet<Reward> rewards = new LinkedHashSet<>();
        this.getRewardTiers().forEach(r -> rewards.addAll(r.getRewards()));
        return rewards;
    }

    public AnimationType getAnimationType() {
        return this.animationType;
    }

    public double getChance(RewardTier rewardTier) {
        return this.rewardProbabilityMap.get(rewardTier);
    }

    public Set<Location> getCrateLocations() {
        return this.crateLocations;
    }

    public String getName() {
        return this.name;
    }

    public Optional<RewardTier> getRewardTier(String name) {
        return this.getRewardTiers().stream().filter(r -> r.getName().equalsIgnoreCase(name)).findFirst();
    }

    public Set<RewardTier> getRewardTiers() {
        return this.rewardProbabilityMap.keySet();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public boolean isCrateLocation(Location location) {
        for (Location crateLocation : this.crateLocations) {
            if (crateLocation.getBlockX() == location.getBlockX()
                    && crateLocation.getBlockY() == location.getBlockY()
                    && crateLocation.getBlockZ() == location.getBlockZ()
                    && crateLocation.getWorld() == location.getWorld()) {
                return true;
            }
        }

        return false;
    }

    public boolean isCrateLocationInUse(Location location) {
        return this.inUse.getOrDefault(location, false);
    }

    public void loadFrom(ConfigurationSection config) {
        ConfigurationSection rewardTiers = config.getConfigurationSection("Reward-Tiers");
        this.uuid = UUID.fromString(config.getString("UUID", UUID.randomUUID().toString()));

        try {
            this.animationType = AnimationType.valueOf(config.getString("Animation-Type", "INTERFACE").toUpperCase());
        } catch (IllegalArgumentException var11) {
            this.animationType = AnimationType.INTERFACE;
            Bukkit.getLogger().log(Level.WARNING, "[GachaCrates] Invalid animation type specified for crate `" + this.name + "`");
        }

        if (rewardTiers != null) {
            for (String rewardTierName : rewardTiers.getKeys(false)) {
                ConfigurationSection rewardTierSection = rewardTiers.getConfigurationSection(rewardTierName);
                RewardTier rewardTier = new RewardTier(rewardTierName);
                double chance = rewardTiers.getDouble(rewardTierName + ".Chance", 50.0) / 100.0;

                assert rewardTierSection != null;

                rewardTier.loadFrom(rewardTierSection);
                this.rewardProbabilityMap.put(rewardTier, Double.valueOf(chance));
            }

            this.sortProbabilityMap();
        } else {
            Bukkit.getLogger().log(Level.WARNING, "[GachaCrates] No reward tiers specified for crate `" + this.name + "`");
        }

        for (String locationString : config.getStringList("Locations")) {
            String[] locationArgs = locationString.split(" ");
            World world = Bukkit.getWorld(locationArgs[0]);
            if (world == null) {
                Bukkit.getLogger().log(Level.SEVERE, "[GachaCrates] Invalid world name specified in crate locations for `" + this.name + "`: " + locationArgs[0]);
            } else {
                int y;
                int z;
                int x;
                try {
                    x = Integer.parseInt(locationArgs[1]);
                    y = Integer.parseInt(locationArgs[2]);
                    z = Integer.parseInt(locationArgs[3]);
                } catch (IllegalArgumentException var12) {
                    Bukkit.getLogger().log(Level.SEVERE, "[GachaCrates] Invalid x, y, or z specified in crate locations for `" + this.name + "`: " + locationString);
                    continue;
                }

                this.crateLocations.add(new Location(world, x, y, z));
            }
        }
    }

    public void open(final GachaCrates plugin, final GachaPlayer gachaPlayer, final CrateSession crateSession, final int pullCount, Menu menu) {
        final Crate crate = crateSession.getCrate();
        switch (this.animationType) {
            case NONE:
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
                break;
            case INTERFACE:
                if (menu instanceof CrateOpenMenu crateOpenMenu) {
                    crateOpenMenu.open(gachaPlayer, crateSession, pullCount);
                } else {
                    Lang.ERR_UNKNOWN.send(gachaPlayer.getPlayer());
                }
                break;
            case PHYSICAL:
                final Location crateLocation = crateSession.getCrateLocation();
                final HashMap<Integer, RewardTier> rewardTiers = new HashMap<>();
                final HashMap<Integer, Reward> rewards = new HashMap<>();
                final HashMap<Integer, Location> endLocationMap = new HashMap<>();
                final Location particleStartLoc = crateLocation.clone().add(0.5, 0.8, 0.5);
                crateSession.setOpenPhase(CrateOpenPhase.OPENING);
                this.setLocationInUse(crateLocation, true);
                (new BukkitRunnable() {
                    int counter = 1;

                    public void run() {
                        if (this.counter > pullCount) {
                            (new BukkitRunnable() {
                                int newCounter = 1;

                                public void run() {
                                    RewardTier rewardTier = rewardTiers.get(this.newCounter);
                                    Reward reward = rewards.get(this.newCounter);
                                    Location endLoc = endLocationMap.get(this.newCounter);
                                    if (rewardTier != null && reward != null && endLoc != null && endLoc.getWorld() != null) {
                                        DustOptions dustOptions = new DustOptions(rewardTier.getColor(), 1.0F);
                                        gachaPlayer.getPlayer().playSound(particleStartLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.9F, 2.0F);
                                        reward.execute(gachaPlayer.getPlayer());
                                        ParticleUtil.spawnStraightLine(endLoc, particleStartLoc, Particle.REDSTONE, dustOptions, 1);
                                        this.newCounter++;
                                    } else {
                                        Crate.this.setLocationInUse(crateLocation, false);
                                        crateSession.setOpenPhase(CrateOpenPhase.COMPLETE);
                                        plugin.getSessionManager().clearSession(gachaPlayer.getUuid());
                                        this.cancel();
                                    }
                                }
                            }).runTaskTimer(plugin, 60L, 7L);
                            this.cancel();
                        } else {
                            RewardTier rewardTier = crate.generateRewardTier(gachaPlayer);
                            Reward reward = rewardTier.generateReward(gachaPlayer, crate, rewardTier);
                            DustOptions dustOptions = new DustOptions(rewardTier.getColor(), 1.0F);
                            double xOffset = new Random().nextDouble(0.4 + (double) this.counter * 0.15) * (double) (new Random().nextBoolean() ? -1 : 1);
                            double zOffset = new Random().nextDouble(0.4 + (double) this.counter * 0.15) * (double) (new Random().nextBoolean() ? -1 : 1);
                            Location endLocation = particleStartLoc.clone().add(xOffset, 1.5, zOffset);
                            if (rewardTier.isPityEnabled()) {
                                gachaPlayer.resetPity(crate, rewardTier);
                            }

                            if (rewardTier.isInsuranceEnabled()) {
                                gachaPlayer.setGuaranteeState(crate, rewardTier, reward.isFeatured());
                            }

                            gachaPlayer.increasePity(crate, rewardTier, 1);
                            rewards.put(this.counter, reward);
                            rewardTiers.put(this.counter, rewardTier);
                            endLocationMap.put(this.counter, endLocation);
                            ParticleUtil.spawnCurvedLine(plugin, particleStartLoc, endLocation, Particle.REDSTONE, dustOptions, 1);
                            gachaPlayer.getPlayer().playSound(particleStartLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                            this.counter++;
                        }
                    }
                }).runTaskTimer(plugin, 0L, 7L);
                (new BukkitRunnable() {
                    int counter = 1;
                    final Location cloudLoc = particleStartLoc.clone().add(0.0, 1.5, 0.0);
                    final List<Location> particleLocations = MathUtil.circle(Crate.super.cloudLoc, 0.5, false);
                    final DustOptions dustOptions = new DustOptions(Color.SILVER, 1.0F);

                    public void run() {
                        if (crateSession.getOpenPhase() == CrateOpenPhase.COMPLETE) {
                            this.cancel();
                        } else {
                            gachaPlayer.getPlayer().playSound(this.cloudLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7F, 0.5F);

                            for (Location particleLoc : this.particleLocations) {
                                if (particleLoc.getWorld() != null) {
                                    particleLoc.getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 1, this.dustOptions);
                                }
                            }

                            if (this.counter < pullCount) {
                                this.particleLocations.addAll(MathUtil.circle(this.cloudLoc, 0.5 + (double) this.counter * 0.15, true));
                                this.counter++;
                            }
                        }
                    }
                }).runTaskTimer(plugin, 20L, 7L);
        }
    }

    public void removeLocation(Location location) {
        this.crateLocations
                .removeIf(
                        crateLocation -> crateLocation.getBlockX() == location.getBlockX()
                                && crateLocation.getBlockY() == location.getBlockY()
                                && crateLocation.getBlockZ() == location.getBlockZ()
                                && crateLocation.getWorld() == location.getWorld()
                );
    }

    public void setLocationInUse(Location location, boolean inUse) {
        this.inUse.put(location, inUse);
    }

    private void sortProbabilityMap() {
        LinkedList<Entry<RewardTier, Double>> probabilityMapList = new LinkedList<>(this.rewardProbabilityMap.entrySet());
        this.rewardProbabilityMap.clear();
        probabilityMapList.sort(Entry.comparingByValue());
        probabilityMapList.forEach(e -> this.rewardProbabilityMap.put(e.getKey(), e.getValue()));
    }
}
