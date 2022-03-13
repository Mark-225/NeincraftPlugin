package de.neincraft.neincraftplugin.modules.playerstats;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.util.Lang;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LabelCommand implements CommandExecutor, SimpleTabCompleter {


    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
        if(args.length == 1){
            completions.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length != 3){
            sender.sendMessage(Lang.WRONG_SYNTAX.getMinedown(sender instanceof Player player ? player : null).replace(
                    "label", label,
                    "args", "[player] [german] [english]"
            ).toComponent());
            return true;
        }
        Optional<UUID> target = NeincraftUtils.nameToUuid(args[0]);
        if(target.isEmpty()){
            sender.sendMessage(Lang.PLAYER_NOT_FOUND.getComponent(sender instanceof Player player ? player : null));
            return true;
        }
        Optional<PlayerStats> ps = AbstractModule.getInstance(PlayerStats.class);
        if(ps.isEmpty()){
            sender.sendMessage(Lang.FATAL_ERROR.getComponent(sender instanceof Player player ? player : null));
            return true;
        }
        PlayerData pd = ps.get().getOrCreate(target.get());
        pd.setGermanLabel(args[1]);
        pd.setEnglishLabel(args[2]);
        ps.get().savePlayerData(pd);
        sender.sendMessage(Lang.LABEL_UPDATED.getMinedown(sender instanceof Player player ? player : null).replace(
                "german", args[1],
                "english", args[2]
        ).toComponent());
        return true;
    }
}
