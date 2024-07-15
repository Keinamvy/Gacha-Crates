package com.gmail.cparse2021.gachacrates.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static ItemStack decodeItem(String str) {
        String[] args = str.split(" ");
        Material material = Material.ACACIA_BOAT;
        String name = null;
        List<String> lore = new ArrayList<>();
        HashMap<Enchantment, Integer> enchantments = new HashMap<>();
        int amount = 1;
        int modelData = 0;

        for (String arg : args) {
            // Attribute
            if (arg.contains(":")) {
                String[] attribute = arg.split(":");

                if (attribute[0].equalsIgnoreCase("name")) {
                    name = attribute[1].replace("_", " ");
                    continue;
                }
                if (attribute[0].equalsIgnoreCase("custommodeldata")) {
                    modelData = Integer.parseInt(attribute[1]);
                    continue;
                }
                if (attribute[0].equalsIgnoreCase("lore")) {
                    Arrays.stream(attribute[1].split("\\|")).forEach((s) -> lore.add(s.replace("_", " ")));
                    continue;
                }
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(attribute[0].toLowerCase()));
                int level;

                try {
                    level = Integer.parseInt(attribute[1]);
                } catch (IllegalArgumentException e) {
                    level = 1;
                }

                if (enchantment != null) {
                    enchantments.put(enchantment, level);
                }

                continue;
            }

            try {
                material = Material.valueOf(arg.toUpperCase());
            } catch (IllegalArgumentException e) {
                try {
                    amount = Integer.parseInt(arg);
                } catch (IllegalArgumentException ignored) {

                }
            }
        }

        return new ItemBuilder(material)
                .setAmount(amount)
                .setDisplayName(name)
                .setLore(lore)
                .setCustomModelData(modelData)
                .addEnchantments(enchantments)
                .build();
    }

    /**
     * Format a string by translating unicode characters and colors
     * Also supports hex color codes in the format of &#AA00AA
     *
     * @param str String to format
     * @return Formatted string
     * @author DMan16 on SpigotMC
     */
    public static String formatString(String str) {
        Pattern unicode = Pattern.compile("\\\\u\\+[a-fA-F0-9]{4}");

        if (str == null) {
            return null;
        }
        Matcher match = unicode.matcher(str);

        while (match.find()) {
            String code = str.substring(match.start(), match.end());
            str = str.replace(code, Character.toString((char) Integer.parseInt(code.replace("\\u+", ""), 16)));
            match = unicode.matcher(str);
        }
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        match = pattern.matcher(str);

        while (match.find()) {
            String color = str.substring(match.start(), match.end());
            str = str.replace(color, ChatColor.of(color.replace("&", "")) + "");
            match = pattern.matcher(str);
        }

        return ChatColor.translateAlternateColorCodes('&', str);
    }
}