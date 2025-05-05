package com.gmail.cparse2021.gachacrates.struct.reward;

import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import com.gmail.cparse2021.gachacrates.util.ItemBuilder;
import com.gmail.cparse2021.gachacrates.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RewardTier {
    private final String name;
    private HashMap<Reward, Double> rewardProbabilityMap = new HashMap<>();
    private int pityLimit = 0;
    private boolean pityEnabled = false;
    private ItemStack displayItem = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setDisplayName("&7Reward Tier").build();
    private boolean insurance = false;

    public RewardTier(String name) {
        this.name = name;
    }

    public Reward generateReward(GachaPlayer player, Crate crate, RewardTier rewardTier) {
        double randDouble = Math.random();
        double count = 0.0;

        for (Entry<Reward, Double> rewardProbability : this.rewardProbabilityMap.entrySet()) {
            if (player.getGuaranteeState(crate, rewardTier)) {
                if (!rewardProbability.getKey().isFeatured()) {
                    return rewardProbability.getKey();
                }
            } else {
                count += rewardProbability.getValue();
                if (randDouble <= count) {
                    return rewardProbability.getKey();
                }
            }
        }

        return this.rewardProbabilityMap.entrySet().iterator().next().getKey();
    }


    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public String getName() {
        return this.name;
    }

    public int getPityLimit() {
        return this.pityLimit;
    }

    public Set<Reward> getRewards() {
        return this.rewardProbabilityMap.keySet();
    }

    public boolean isPityEnabled() {
        return this.pityEnabled;
    }

    public boolean isInsuranceEnabled() {
        return this.insurance;
    }

    public void loadFrom(ConfigurationSection rewardTierSection) {
        ConfigurationSection rewards = rewardTierSection.getConfigurationSection("Rewards");
        this.pityEnabled = Boolean.parseBoolean(rewardTierSection.getString("Pity", "false"));
        this.pityLimit = rewardTierSection.getInt("Pity-Limit", 0);
        this.displayItem = Utils.decodeItem(rewardTierSection.getString("Display-Item", "WHITE_STAINED_GLASS_PANE name:&7" + this.name));
        this.insurance = rewardTierSection.getBoolean("Insurance", false);
        if (rewards != null) {
            for (String rewardName : rewards.getKeys(false)) {
                ConfigurationSection rewardsSection = rewards.getConfigurationSection(rewardName);
                Reward reward = new Reward(rewardName);
                double chance = rewards.getDouble(rewardName + ".Chance", 10.0) / 100.0;

                assert rewardsSection != null;

                reward.loadFrom(rewardsSection);
                this.rewardProbabilityMap.put(reward, chance);
            }

            this.sortProbabilityMap();
        } else {
            Bukkit.getLogger().log(Level.WARNING, "[GachaCrates] No rewards specified for reward tier `" + this.name + "`");
        }
    }

    private void sortProbabilityMap() {
        List<Entry<Reward, Double>> probabilityMapList = new LinkedList<>(this.rewardProbabilityMap.entrySet());
        probabilityMapList.sort(Entry.comparingByValue());
        this.rewardProbabilityMap = probabilityMapList.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (prev, next) -> next, HashMap::new));
    }
}
