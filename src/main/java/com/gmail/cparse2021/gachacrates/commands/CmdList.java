package com.gmail.cparse2021.gachacrates.commands;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.StringJoiner;

public class CmdList extends CrateCommand {
    private final GachaCrates plugin;

    public CmdList(GachaCrates plugin) {
        super("list", 0, 0);
        this.setPermission("gachacrates.admin");
        this.plugin = plugin;
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        StringJoiner listJoiner = new StringJoiner(", ");
        HashMap<String, String> replacements = new HashMap<>();
        this.plugin.getCrateCache().getCrates().forEach(c -> listJoiner.add(c.getName()));
        replacements.put("%list%", listJoiner.toString());
        Lang.CRATE_LIST.send(sender, replacements);
    }
}
