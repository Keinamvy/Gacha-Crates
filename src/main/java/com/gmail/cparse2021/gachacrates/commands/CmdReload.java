package com.gmail.cparse2021.gachacrates.commands;


import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import org.bukkit.command.CommandSender;

public class CmdReload extends CrateCommand {
    private final GachaCrates plugin;

    public CmdReload(GachaCrates plugin) {
        super("reload", 0, 1);
        setPermission("gachacrates.reload");

        this.plugin = plugin;
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        GachaCrates.getInstance().reloadConfig();
        Lang.CRATE_RELOAD.send(sender);
    }

}
