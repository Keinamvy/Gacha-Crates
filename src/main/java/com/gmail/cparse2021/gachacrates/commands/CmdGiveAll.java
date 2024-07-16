package com.gmail.cparse2021.gachacrates.commands;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Optional;

public class CmdGiveAll extends CrateCommand {
    public final GachaCrates plugin;

    public CmdGiveAll(GachaCrates plugin) {
        super("giveall", 1, 2);
        this.setPermission("gachacrates.admin");
        this.plugin = plugin;
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Optional<Crate> optionalCrate = this.plugin.getCrateCache().getCrate(args[0]);
        HashMap<String, String> replacements = new HashMap<>();
        if (optionalCrate.isEmpty()) {
            replacements.put("%crate%", args[0]);
            Lang.ERR_UNKNOWN_CRATE.send(sender, replacements);
        } else {
            Crate crate = optionalCrate.get();
            int amount = 1;
            if (args.length > 1) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (IllegalArgumentException var8) {
                    replacements.put("%arg%", args[1]);
                    Lang.ERR_INVALID_AMOUNT.send(sender, replacements);
                    return;
                }
            }

            if (amount < 1) {
                replacements.put("%arg%", args[1]);
                Lang.ERR_INVALID_AMOUNT.send(sender, replacements);
            } else {
                int finalAmount = amount;
                replacements.put("%crate%", crate.getName());
                replacements.put("%amount%", Integer.toString(amount));
                Bukkit.getOnlinePlayers().forEach(p -> {
                    GachaPlayer gachaPlayer = this.plugin.getPlayerCache().getPlayer(p.getUniqueId());
                    gachaPlayer.setAvailablePulls(crate, gachaPlayer.getAvailablePulls(crate) + finalAmount);
                    Lang.CRATE_RECEIVED.send(p, replacements);
                });
                Lang.CRATE_GIVEN_TO_ALL.send(sender, replacements);
            }
        }
    }
}
