package de.neincraft.neincraftplugin.modules.storage;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.util.lang.Lang;
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

public class StorageCommand implements CommandExecutor, SimpleTabCompleter {


    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
        if(commandSender.hasPermission("neincraft.storage.others") && args.length == 1){
            completions.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(Lang.WRONG_EXECUTOR.getComponent(null));
            return true;
        }
        Player player = (Player) sender;
        Optional<StorageModule> optionalStorageModule = AbstractModule.getInstance(StorageModule.class);
        if(optionalStorageModule.isEmpty()){
            player.sendMessage(Lang.FATAL_ERROR.getComponent(player));
            return true;
        }
        StorageModule sm = optionalStorageModule.get();
        boolean adminMode = player.hasPermission("neincraft.storage.others");
        if(args.length == 0){
            openStorage(sm, player.getUniqueId(), player);
        }else if(adminMode && args.length == 1){
            Optional<UUID> targetUUID = NeincraftUtils.nameToUuid(args[0]);
            if(targetUUID.isEmpty()){
                player.sendMessage(Lang.PLAYER_NOT_FOUND.getComponent(player));
                return true;
            }
            openStorage(sm, targetUUID.get(), player);
        }else{
            player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                    "label", label,
                    "args", adminMode ? "[player]" : ""
            ).toComponent());
        }
        return true;
    }

    private void openStorage(StorageModule sm, UUID target, Player viewer){
        boolean available = sm.requestStorage(target, (storage -> {
            if(storage == null){
                viewer.sendMessage(Lang.FATAL_ERROR.getComponent(viewer));
                return;
            }
            storage.openInterface(viewer);
        }));
        if(!available)
            viewer.sendMessage(Lang.STORAGE_LOADING.getComponent(viewer));
    }
}
