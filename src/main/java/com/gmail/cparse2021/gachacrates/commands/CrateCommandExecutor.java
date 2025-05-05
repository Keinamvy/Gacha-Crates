package com.gmail.cparse2021.gachacrates.commands;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CrateCommandExecutor implements CommandExecutor, TabCompleter {
    private final Set<CrateCommand> commands = new HashSet<>();
    private final GachaCrates plugin;

    public CrateCommandExecutor(GachaCrates plugin) {
        this.plugin = plugin;
    }


    public void addCommands(CrateCommand... crateCommands) {
        this.commands.addAll(Arrays.asList(crateCommands));
    }

    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 0) {
            this.sendUsage(sender);
            return true;
        } else {
            Optional<CrateCommand> optionalCommand = this.commands.stream().filter(c -> c.hasAlias(args[0])).findFirst();
            if (optionalCommand.isEmpty()) {
                this.sendUsage(sender);
                return true;
            } else {
                CrateCommand command = optionalCommand.get();
                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                if (command.isPlayerOnly() && !(sender instanceof Player)) {
                    Lang.ERR_NOT_PLAYER.send(sender);
                    return true;
                } else if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
                    Lang.ERR_MISSING_PERM.send(sender);
                    return true;
                } else if (newArgs.length >= command.getMinArgs() && newArgs.length <= command.getMaxArgs()) {
                    try {
                        command.run(sender, newArgs);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                } else {
                    this.sendUsage(sender);
                    return true;
                }
            }
        }
    }

    private void sendUsage(CommandSender sender) {
        Lang.CRATE_USAGE.send(sender);
    }

    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        List<String> commands = new ArrayList<>();

        for (CrateCommand i : this.commands) {
            commands.add(i.getLabel());
        }

        if (args.length == 1) {
            return commands;
        } else {
            String var8 = args[0];

            return switch (var8) {
                case "set", "giveall" -> this.getCratesName();
                case "give", "take" -> this.getOnlinePlayersName().contains(args[1]) ? this.getCratesName() : null;
                default -> null;
            };
        }
    }

    private List<String> getCratesName() {
        return this.plugin.getCrateCache().getCrates().stream()
                .map(Crate::getName)
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayersName() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
