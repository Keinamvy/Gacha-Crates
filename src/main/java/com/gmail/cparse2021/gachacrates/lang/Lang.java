package com.gmail.cparse2021.gachacrates.lang;

import com.gmail.cparse2021.gachacrates.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public enum Lang {
    ERR_ALREADY_CRATE("err-already-crate", "A crate already exists here", LangType.NORMAL),
    ERR_INVALID_AMOUNT("err-invalid-amount", "%arg% must be a number above 0", LangType.NORMAL),
    ERR_NO_BLOCK("err-no-block", "Unable to fin a target block", LangType.NORMAL),
    ERR_NO_CRATE_FOUND("err-no-crate-found", "No crate was found here", LangType.NORMAL),
    ERR_NOT_ENOUGH_PULLS("err-not-enough-pulls", "You don't have enough pulls to open this", LangType.NORMAL),
    ERR_NOT_PLAYER("err-not-player", "You must be a player to use this command", LangType.NORMAL),
    ERR_OPENING_CRATE("err-opening-crate", "You are already opening a crate", LangType.NORMAL),
    ERR_PLAYER_OFFLINE("err-player-offline", "%player% is not online", LangType.NORMAL),
    ERR_MISSING_PERM("err-missing-perm", "You don't have permission to use this command", LangType.NORMAL),
    ERR_UNKNOWN_CRATE("err-unknown-crate", "No crate named '%crate%' was found", LangType.NORMAL),
    ERR_UNKNOWN("err-unknown", "An unknown error has occurred", LangType.NORMAL),
    CRATE_CONFIRM_DELETE("crate-confirm-delete", "Confirm deletion of crate &a%crate% &fby breaking again within &a3s", LangType.NORMAL),
    CRATE_RELOAD("crate-RELOAD", "Reloaded!", LangType.NORMAL),
    CRATE_GIVEN("crate-given", "Gave &a%player% %amount%x %crate% &fpulls", LangType.NORMAL),
    CRATE_GIVEN_TO_ALL("crate-given-to-all", "Gave all online players &a%amount%x %crate% &fpulls", LangType.NORMAL),
    CRATE_RECEIVED("crate-received", "Received &a%amount%x %crate% &fpulls", LangType.NORMAL),
    CRATE_LIST("crate-list", "%list%", LangType.NORMAL),
    CRATE_LOCATION_ADDED("crate-location-added", "Crate location added for &a%crate%", LangType.NORMAL),
    CRATE_LOCATION_REMOVED("crate-location-removed", "Crate location removed for &a%crate%", LangType.NORMAL),
    CRATE_LOST("crate-lost", "Lost &a%amount%x %crate% &fpulls", LangType.NORMAL),
    CRATE_PULL_LIST("crate-pull-list", LangType.LONG),
    CRATE_TAKEN("crate-taken", "Took &a%amount%x %crate% &fpulls from &a%player%", LangType.NORMAL),
    CRATE_USAGE("crate-usage", LangType.LONG),
    PITY_TRACKER_FORMAT("pity-tracker-format", "&f  %pity-count%&7/&8%pity-limit% &7%reward-tier%", LangType.NORMAL),
    PULL_LIST_FORMAT("pull-list-format", "&a  %crate%&7: &f%pull-count%", LangType.NORMAL),
    TIER_RATE_FORMAT("tier-rate-format", "&a  %reward-tier% &7%rate%%", LangType.NORMAL),
    INSURANCE_TRACKER_FORMAT("insurance-tracker-format", "&a  %reward-tier%: &7%insuranceState%", LangType.NORMAL),
    PREFIX("prefix", "&2Gacha Crates &8» &f", LangType.NORMAL);

    private static FileConfiguration fileConfiguration;
    private final String path;
    private final String def;
    private final LangType langType;

    Lang(String path, LangType langType) {
        this.path = path;
        this.def = "";
        this.langType = langType;
    }

    Lang(String path, String def, LangType langType) {
        this.path = path;
        this.def = def;
        this.langType = langType;
    }

    public static void setFileConfiguration(FileConfiguration fileConfiguration) {
        Lang.fileConfiguration = fileConfiguration;
    }

    public void send(CommandSender sender) {
        switch (this.langType) {
            case NORMAL:
                sender.sendMessage(this.toString());
                break;
            case LONG:
                for (String str : this.toStringList()) {
                    sender.sendMessage(str);
                }
        }
    }

    public void send(CommandSender sender, boolean addPrefix) {
        switch (this.langType) {
            case NORMAL:
                sender.sendMessage(this.toString(addPrefix));
                break;
            case LONG:
                this.send(sender);
        }
    }

    public void send(CommandSender sender, HashMap<String, String> replacements) {
        switch (this.langType) {
            case NORMAL:
                sender.sendMessage(this.toString(replacements));
                break;
            case LONG:
                List<String> messages = new ArrayList<>();
                this.toStringList().forEach(s -> {
                    for (Entry<String, String> replacement : replacements.entrySet()) {
                        s = s.replace(replacement.getKey(), replacement.getValue());
                    }

                    messages.add(Utils.formatString(s));
                });
                messages.forEach(sender::sendMessage);
        }
    }

    public List<String> toStringList() {
        List<String> coloredList = new ArrayList<>();

        for (String message : fileConfiguration.getStringList(this.path)) {
            coloredList.add(Utils.formatString(message));
        }

        return coloredList;
    }

    @Override
    public String toString() {
        return this.toString(true);
    }

    public String toString(boolean addPrefix) {
        String message = "";
        if (this != PREFIX && addPrefix) {
            message = message + PREFIX;
        }

        message = message + fileConfiguration.getString(this.path, this.def);
        return Utils.formatString(message);
    }

    public String toString(HashMap<String, String> replacements) {
        String message = this.toString();

        for (String key : replacements.keySet()) {
            message = message.replace(key, replacements.get(key));
        }

        return message;
    }

    public String toString(HashMap<String, String> replacements, boolean addPrefix) {
        String message = this.toString(addPrefix);

        for (String key : replacements.keySet()) {
            message = message.replace(key, replacements.get(key));
        }

        return message;
    }
}
