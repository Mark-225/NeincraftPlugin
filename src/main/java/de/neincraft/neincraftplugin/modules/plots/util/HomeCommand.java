package de.neincraft.neincraftplugin.modules.plots.util;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.modules.plots.Plot;
import de.neincraft.neincraftplugin.modules.plots.PlotModule;
import de.neincraft.neincraftplugin.util.Lang;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class HomeCommand implements CommandExecutor, SimpleTabCompleter {

    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
        if(!label.equalsIgnoreCase("home")) return;
        if(commandSender instanceof Player player && args.length == 1){
            if(args[0].contains(":")){
                String playerName = args[0].substring(0, args[0].indexOf(":"));
                UUID ownerUUID;
                if(playerName.equals("")){
                    ownerUUID = null;
                }else {
                    Optional<UUID> oUuid = NeincraftUtils.nameToUuid(playerName);
                    if(oUuid.isEmpty()) return;
                    ownerUUID = oUuid.get();
                }
                completions.addAll(possiblePlots(player).stream().filter(plot -> Objects.equals(plot.getPlotData().getOwner(), ownerUUID)).map(plot -> (plot.isServerPlot() ? "" : NeincraftUtils.uuidToName(plot.getPlotData().getOwner())) + ":" + plot.getPlotData().getName()).toList());
            }else{
                List<Plot> possiblePlots = possiblePlots(player);
                completions.addAll(possiblePlots.stream().filter(plot -> player.getUniqueId().equals(plot.getPlotData().getOwner())).map(plot -> plot.getPlotData().getName()).toList());
                completions.addAll(possiblePlots.stream().map(plot -> plot.getPlotData().getOwner()).distinct().filter(uuid -> !player.getUniqueId().equals(uuid)).map(uuid -> uuid == null ? ":" : NeincraftUtils.uuidToName(uuid) + ":").toList());
            }
        }
    }

    private List<Plot> possiblePlots(Player p){
        Optional<PlotModule> oPlotModule = AbstractModule.getInstance(PlotModule.class);
        if(oPlotModule.isEmpty()) return Collections.emptyList();
        PlotModule pm = oPlotModule.get();
        if(p.hasPermission("neincraft.plots.bypass." + PlotPermission.TELEPORT_HOME.getBypassPermission())) return pm.getLoadedPlots();
        return Stream.concat(pm.getPlotsOfOwner(p.getUniqueId()).stream(), pm.getPlotsWithPermissionOnMain(p.getUniqueId(), PlotPermission.TELEPORT_HOME).stream()).distinct().toList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(label.equalsIgnoreCase("spawn")){
            Bukkit.dispatchCommand(sender, "home :spawn");
            return true;
        }
        if(!(sender instanceof Player)){
            sender.sendMessage(Lang.WRONG_EXECUTOR.getComponent(null));
            return true;
        }
        Player player = (Player) sender;
        if(args.length != 1){
            player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                    "label", label,
                    "args", "[plot / owner:plot]"
            ).toComponent());
        }
        Optional<PlotModule> oPlotModule = AbstractModule.getInstance(PlotModule.class);
        if(oPlotModule.isEmpty()){
            player.sendMessage(Lang.FATAL_ERROR.getComponent(player));
            return true;
        }
        PlotModule pm = oPlotModule.get();
        Optional<Plot> optionalPlot = args[0].contains(":") ? pm.findByName(args[0]) : pm.findByName(args[0], player.getUniqueId());
        if(optionalPlot.isEmpty()){
            player.sendMessage(Lang.PLOT_NOT_FOUND.getComponent(player));
            return true;
        }
        Plot plot = optionalPlot.get();
        if(!player.hasPermission("neincraft.plots.bypass." + PlotPermission.TELEPORT_HOME.getBypassPermission()) && !plot.resolvePermission(plot.getSubdivision("main"), plot.getPlayerGroup(player.getUniqueId()), PlotPermission.TELEPORT_HOME)){
            player.sendMessage(Lang.PLOT_CANT_TELEPORT.getComponent(player));
            return true;
        }
        NeincraftUtils.teleportToLocation(player, plot.getHome());
        return true;
    }
}
