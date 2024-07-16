package com.gmail.cparse2021.gachacrates.menu;

import javax.annotation.Nullable;
import java.util.*;

public class MenuManager {
    private final Set<Menu> menus = new HashSet<>();
    private final HashMap<UUID, Menu> activeMenuMap = new HashMap<>();
    private final HashMap<UUID, Long> clickCooldownMap = new HashMap<>();

    public void addCooldown(UUID uuid) {
        this.clickCooldownMap.put(uuid, System.currentTimeMillis());
    }

    public void addMenu(Menu... menus) {
        this.menus.addAll(Arrays.asList(menus));
    }

    public void clearActiveMenu(UUID uuid) {
        this.activeMenuMap.remove(uuid);
    }

    @Nullable
    public Menu getActiveMenu(UUID uuid) {
        return this.activeMenuMap.get(uuid);
    }

    public Optional<Menu> getMenu(String menuId) {
        return this.menus.stream().filter(m -> m.getMenuID().equals(menuId)).findFirst();
    }

    public boolean isOnCooldown(UUID uuid) {
        if (!this.clickCooldownMap.containsKey(uuid)) {
            return true;
        } else {
            long lastClick = this.clickCooldownMap.get(uuid);
            if (System.currentTimeMillis() - lastClick < 100L) {
                return false;
            } else {
                this.clickCooldownMap.remove(uuid);
                return true;
            }
        }
    }

    public void setActiveMenu(UUID uuid, Menu menu) {
        this.activeMenuMap.put(uuid, menu);
    }
}
