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

        for (String arg : args) {
            if (arg.contains(":")) {
                String[] attribute = arg.split(":");
                if (attribute[0].equalsIgnoreCase("name")) {
                    name = attribute[1].replace("_", " ");
                } else if (attribute[0].equalsIgnoreCase("lore")) {
                    Arrays.stream(attribute[1].split("\\|")).forEach(s -> lore.add(s.replace("_", " ")));
                } else {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(attribute[0].toLowerCase()));

                    int level;
                    try {
                        level = Integer.parseInt(attribute[1]);
                    } catch (IllegalArgumentException var17) {
                        level = 1;
                    }

                    if (enchantment != null) {
                        enchantments.put(enchantment, level);
                    }
                }
            } else {
                try {
                    material = Material.valueOf(arg.toUpperCase());
                } catch (IllegalArgumentException var16) {
                    try {
                        amount = Integer.parseInt(arg);
                    } catch (IllegalArgumentException var15) {
                    }
                }
            }
        }

        return new ItemBuilder(material).setAmount(amount).setDisplayName(name).setLore(lore).addEnchantments(enchantments).build();
    }

    public static String formatString(String str) {
        Pattern unicode = Pattern.compile("\\\\u\\+[a-fA-F0-9]{4}");
        if (str == null) {
            return null;
        } else {
            for (Matcher match = unicode.matcher(str); match.find(); match = unicode.matcher(str)) {
                String code = str.substring(match.start(), match.end());
                str = str.replace(code, Character.toString((char) Integer.parseInt(code.replace("\\u+", ""), 16)));
            }

            Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");

            for (Matcher var5 = pattern.matcher(str); var5.find(); var5 = pattern.matcher(str)) {
                String color = str.substring(var5.start(), var5.end());
                str = str.replace(color, ChatColor.of(color.replace("&", "")) + "");
            }

            return ChatColor.translateAlternateColorCodes('&', str);
        }
    }
}
