package com.gmail.cparse2021.gachacrates.commands;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;

public class CmdRemove extends CrateCommand {
    private final GachaCrates plugin;

    public CmdRemove(GachaCrates plugin) {
        super("remove", 0, 0);
        this.setPermission("gachacrates.admin");
        this.setPlayerOnly(true);
        this.plugin = plugin;
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        HashMap<String, String> replacements = new HashMap<>();
        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock.isEmpty()) {
            Lang.ERR_NO_BLOCK.send(player);
        } else {
            Optional<Crate> optionalCrate = this.plugin.getCrateCache().getCrate(targetBlock.getLocation());
            if (optionalCrate.isEmpty()) {
                Lang.ERR_NO_CRATE_FOUND.send(player);
            } else {
                replacements.put("%crate%", optionalCrate.get().getName());
                optionalCrate.get().removeLocation(targetBlock.getLocation());
                Lang.CRATE_LOCATION_REMOVED.send(player, replacements);
            }
        }
    }
}
