package com.gmail.cparse2021.gachacrates.commands;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;

public class CmdSet extends CrateCommand {
    private final GachaCrates plugin;

    public CmdSet(GachaCrates plugin) {
        super("set", 1, 1);
        this.setPermission("gachacrates.admin");
        this.setPlayerOnly(true);
        this.plugin = plugin;
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Optional<Crate> crate = this.plugin.getCrateCache().getCrate(args[0]);
        HashMap<String, String> replacements = new HashMap<>();
        Player player = (Player) sender;
        if (crate.isEmpty()) {
            replacements.put("%crate%", args[0]);
            Lang.ERR_UNKNOWN_CRATE.send(player, replacements);
        } else {
            replacements.put("%crate%", crate.get().getName());
            Block targetBlock = player.getTargetBlock(null, 5);
            if (targetBlock.isEmpty()) {
                Lang.ERR_NO_BLOCK.send(player);
            } else if (this.plugin.getCrateCache().getCrate(targetBlock.getLocation()).isPresent()) {
                Lang.ERR_ALREADY_CRATE.send(player);
            } else {
                crate.get().addLocation(targetBlock.getLocation());
                Lang.CRATE_LOCATION_ADDED.send(player, replacements);
                plugin.saveData();
            }
        }
    }
}
