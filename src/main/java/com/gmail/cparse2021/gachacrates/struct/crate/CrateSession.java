package com.gmail.cparse2021.gachacrates.struct.crate;

import com.gmail.cparse2021.gachacrates.struct.reward.Reward;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class CrateSession {
    private final UUID playerUuid;
    private Crate crate;
    private Location crateLocation;
    private HashMap<Integer, Reward> rewards = new HashMap<>();
    private CrateOpenPhase openPhase = CrateOpenPhase.INACTIVE;

    public CrateSession(UUID playerUuid, Crate crate, Location crateLocation) {
        this.playerUuid = playerUuid;
        this.crate = crate;
        this.crateLocation = crateLocation;
    }

    public void clearRewards() {
        rewards.clear();
    }

    public Crate getCrate() {
        return crate;
    }

    public void setCrate(Crate crate) {
        this.crate = crate;
    }

    public Location getCrateLocation() {
        return crateLocation;
    }

    public void setCrateLocation(Location crateLocation) {
        this.crateLocation = crateLocation;
    }

    public CrateOpenPhase getOpenPhase() {
        return openPhase;
    }

    public void setOpenPhase(CrateOpenPhase openPhase) {
        this.openPhase = openPhase;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @Nullable
    public Reward getReward(int slotNumber) {
        return rewards.get(slotNumber);
    }

    public Collection<Reward> getRewards() {
        return rewards.values();
    }

    public void setRewards(HashMap<Integer, Reward> rewards) {
        this.rewards = rewards;
    }
}