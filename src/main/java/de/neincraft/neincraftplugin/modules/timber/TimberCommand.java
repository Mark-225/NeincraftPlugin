package de.neincraft.neincraftplugin.modules.timber;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.util.Lang;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TimberCommand implements CommandExecutor, SimpleTabCompleter {

    public final NamespacedKey timberStateKey = new NamespacedKey(NeincraftPlugin.getInstance(), "timber-state");

    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(Lang.WRONG_EXECUTOR.getComponent(null));
            return true;
        }
        Player player = (Player) sender;
        if(args.length != 0){
            player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                    "label", label,
                    "args", ""
            ).toComponent());
            return true;
        }
        Byte oldState = null;
        byte newState = 1;
        if((oldState = player.getPersistentDataContainer().get(timberStateKey, PersistentDataType.BYTE)) != null && oldState.equals((byte) 1)){
            newState = 0;
        }
        player.getPersistentDataContainer().set(timberStateKey, PersistentDataType.BYTE, newState);
        if(newState == 1){
            player.sendMessage(Lang.TIMBER_ACTIVE.getComponent(player));
        }else{
            player.sendMessage(Lang.TIMBER_INACTIVE.getComponent(player));
        }
        return true;
    }
}
