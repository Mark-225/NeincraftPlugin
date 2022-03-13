package de.neincraft.neincraftplugin.modules.teleportCommands;

import de.neincraft.neincraftplugin.modules.teleportCommands.requests.TPAHereRequest;
import de.neincraft.neincraftplugin.modules.teleportCommands.requests.TPARequest;
import de.neincraft.neincraftplugin.modules.teleportCommands.requests.TPRequest;
import de.neincraft.neincraftplugin.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TPACommand extends TPCommand{

    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
        if(args.length != 1)
            return;
        if(label.equalsIgnoreCase("tpaccept") && commandSender instanceof Player player)
            completions.addAll(teleportModule.getOpenRequests(player).stream().map(r -> r.getSender().getName()).toList());
        else
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(Lang.WRONG_EXECUTOR.getComponent(null));
            return true;
        }
        Player player = (Player) sender;
        if(args.length != 1){
            player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                    "label", label,
                    "args", "[player]"
            ).toComponent());
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null){
            player.sendMessage(Lang.PLAYER_NOT_FOUND.getComponent(player));
            return true;
        }
        if(label.equalsIgnoreCase("tpaccept")){
            if(!teleportModule.acceptRequest(target, player))
                player.sendMessage(Lang.NO_OPEN_REQUESTS.getComponent(player));
            return true;
        }
        boolean isTpaHere = label.equalsIgnoreCase("tpahere");
        if(!teleportModule.createRequest(isTpaHere ? new TPAHereRequest(player, target) : new TPARequest(player, target))){
            player.sendMessage(Lang.TP_REQUEST_COOLDOWN.getComponent(player));
            return true;
        }
        player.sendMessage(Lang.TP_REQUEST_SENT.getComponent(player));
        target.sendMessage((isTpaHere ? Lang.TPAHERE_REQUEST_RECEIVED : Lang.TPA_REQUEST_RECEIVED).getMinedown(target).replace("player", player.getName()).toComponent());
        return true;
    }
}
