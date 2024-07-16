package com.gmail.cparse2021.gachacrates.cache;

import com.gmail.cparse2021.gachacrates.file.CustomFile;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CrateCache {
    private final List<Crate> crates = new ArrayList<>();

    public Optional<Crate> getCrate(Location location) {
        return this.crates.stream().filter(c -> c.isCrateLocation(location)).findFirst();
    }

    public Optional<Crate> getCrate(String name) {
        return this.crates.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst();
    }

    public Optional<Crate> getCrate(UUID uuid) {
        return this.crates.stream().filter(c -> c.getUuid().equals(uuid)).findFirst();
    }

    public List<Crate> getCrates() {
        return this.crates;
    }

    public void loadFrom(FileConfiguration config) {
        ConfigurationSection cratesSection = config.getConfigurationSection("Crates");
        if (cratesSection != null) {
            for (String crateName : cratesSection.getKeys(false)) {
                Crate crate = new Crate(crateName);
                ConfigurationSection crateSection = cratesSection.getConfigurationSection(crateName);

                assert crateSection != null;

                crate.loadFrom(crateSection);
                this.crates.add(crate);
            }
        }
    }

    public void saveTo(CustomFile customFile) {
        for (Crate crate : this.crates) {
            List<String> locations = new ArrayList<>();

            for (Location location : crate.getCrateLocations()) {
                if (location.getWorld() != null) {
                    locations.add(location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
                }
            }

            customFile.getConfig().set("Crates." + crate.getName() + ".Locations", locations);
            customFile.getConfig().set("Crates." + crate.getName() + ".UUID", crate.getUuid().toString());
        }

        customFile.saveConfig();
    }
}
