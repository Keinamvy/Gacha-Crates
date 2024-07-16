package com.gmail.cparse2021.gachacrates.struct;

import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import com.gmail.cparse2021.gachacrates.struct.reward.RewardTier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public class GachaPlayer {
    private final HashMap<Crate, HashMap<RewardTier, Integer>> pityMap = new HashMap<>();
    private final HashMap<Crate, HashMap<RewardTier, Boolean>> insuranceMap = new HashMap<>();
    private final HashMap<Crate, Integer> pullMap = new HashMap<>();
    private final UUID uuid;

    public GachaPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public int getAvailablePulls(Crate crate) {
        return this.pullMap.getOrDefault(crate, 0);
    }

    public int getPity(Crate crate, RewardTier rewardTier) {
        return this.getPityMap(crate).getOrDefault(rewardTier, 0);
    }

    public boolean getGuaranteeState(Crate crate, RewardTier rewardTier) {
        return this.getInsuranceMap(crate).getOrDefault(rewardTier, false);
    }

    public HashMap<Crate, HashMap<RewardTier, Integer>> getPityMap() {
        return this.pityMap;
    }

    public HashMap<Crate, HashMap<RewardTier, Boolean>> getInsuranceMap() {
        return this.insuranceMap;
    }

    @Nonnull
    public HashMap<RewardTier, Integer> getPityMap(Crate crate) {
        if (!this.pityMap.containsKey(crate)) {
            this.pityMap.put(crate, new HashMap<>());
        }

        return this.pityMap.get(crate);
    }

    @Nonnull
    public HashMap<RewardTier, Boolean> getInsuranceMap(Crate crate) {
        if (!this.insuranceMap.containsKey(crate)) {
            this.insuranceMap.put(crate, new HashMap<>());
        }

        return this.insuranceMap.get(crate);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public HashMap<Crate, Integer> getPullMap() {
        return this.pullMap;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void increasePity(Crate crate, int amt) {
        for (RewardTier rewardTier : crate.getRewardTiers()) {
            if (rewardTier.isPityEnabled()) {
                this.setPity(crate, rewardTier, Math.min(this.getPity(crate, rewardTier) + amt, rewardTier.getPityLimit() - 1));
            }
        }
    }

    public void increasePity(Crate crate, RewardTier exception, int amt) {
        for (RewardTier rewardTier : crate.getRewardTiers()) {
            if (rewardTier.isPityEnabled() && rewardTier != exception) {
                this.setPity(crate, rewardTier, Math.min(this.getPity(crate, rewardTier) + amt, rewardTier.getPityLimit() - 1));
            }
        }
    }

    public void resetPity(Crate crate, RewardTier rewardTier) {
        this.getPityMap(crate).put(rewardTier, 0);
    }

    public void setAvailablePulls(Crate crate, int count) {
        this.pullMap.put(crate, count);
    }

    public void setPity(Crate crate, RewardTier rewardTier, int pityLevel) {
        this.getPityMap(crate).put(rewardTier, pityLevel);
    }

    public void setGuaranteeState(Crate crate, RewardTier rewardTier, boolean state) {
        this.getInsuranceMap(crate).put(rewardTier, state);
    }
}
