package com.gmail.cparse2021.gachacrates.commands;

import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CrateCommand {
    private final String label;
    private final int maxArgs;
    private final int minArgs;
    private final List<String> aliases = new ArrayList<>();
    private String permission = null;
    private boolean playerOnly = false;

    public CrateCommand(String label, int minArgs, int maxArgs) {
        this.label = label;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    public void addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public String getLabel() {
        return this.label;
    }

    public int getMaxArgs() {
        return this.maxArgs;
    }

    public int getMinArgs() {
        return this.minArgs;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean hasAlias(String str) {
        for (String alias : this.aliases) {
            if (alias.equalsIgnoreCase(str)) {
                return true;
            }
        }

        return this.label.equalsIgnoreCase(str);
    }

    public boolean isPlayerOnly() {
        return this.playerOnly;
    }

    public void setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    public abstract void run(CommandSender var1, String[] var2) throws IOException;
}
