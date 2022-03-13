package de.neincraft.neincraftplugin.modules.plots;

import de.neincraft.neincraftplugin.modules.plots.util.PlotUtils;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.commands.NeincraftCommand;
import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerStats;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.modules.plots.dto.ChunkData;
import de.neincraft.neincraftplugin.modules.plots.dto.PlotMemberGroup;
import de.neincraft.neincraftplugin.modules.plots.dto.SubdivisionData;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.LocationData;
import de.neincraft.neincraftplugin.modules.plots.util.PlotPermission;
import de.neincraft.neincraftplugin.modules.plots.util.PlotSetting;
import de.neincraft.neincraftplugin.util.Lang;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlotCommand extends NeincraftCommand implements CommandExecutor, SimpleTabCompleter {

    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
        if(!(commandSender instanceof Player)) return;
        Player player = (Player) commandSender;
        Optional<PlotModule> oPm = AbstractModule.getInstance(PlotModule.class);
        if(oPm.isEmpty()) return;
        PlotModule pm = oPm.get();
        boolean adminMode = label.toLowerCase().endsWith("admin");
        if(args.length == 1){
            completions.addAll(List.of("info", "create", "delete", "addChunk", "removeChunk", "createSubdivision", "deleteSubdivision", "assignChunk", "unassign", "listSubdivisions", "setSetting", "showSettings", "createGroup", "deleteGroup", "listGroups", "listMembers", "getGroup", "setGroup", "removePlayer", "setPermission", "listPermissions", "setHome", "listPlots"));
            if(adminMode)
                completions.addAll(List.of("reloadData"));
            return;
        }
        String firstArg = args[0].toLowerCase();
        switch(firstArg){
            case "create", "createsubdivision", "creategroup" -> {
                if(args.length == 2 && args[1].length() == 0){
                    completions.add("[name]");
                }
            }

            case "delete" -> {
                if(args.length == 2){
                    completions.addAll(getPlotNameCompletions(player, args[1], pm, adminMode));
                }else if(args.length == 3 && args[2].length() == 0){
                    completions.add("[confirmationToken]");
                }
            }

            case "addchunk" -> {
                if(args.length == 2 && args[1].length() == 0){
                    completions.add("([plot])");
                }
            }

            case "assignchunk", "deletesubdivision" -> {
                if(args.length == 2 && args[1].length() == 0){
                    completions.add("[subdivision]");
                }else if(args.length == 2){
                    Plot plot = assertOnOwnPlot(player, pm, adminMode, true);
                    if(plot == null) return;
                    completions.addAll(plot.getPlotData().getSubdivisions().stream().map(sd -> sd.getSubdivisionId().getName()).toList());
                }
            }

            case "setsetting", "showSettings" -> {
                if(args.length == 2 && args[1].length() == 0){
                    completions.add("[subdivision]");
                }else if(args.length == 2){
                    Plot plot = assertOnOwnPlot(player, pm, adminMode, true);
                    if(plot == null) return;
                    completions.addAll(plot.getPlotData().getSubdivisions().stream().map(sd -> sd.getSubdivisionId().getName()).toList());
                }else if(args.length == 3 && args[2].length() == 0){
                    completions.add("[setting]");
                }else if(args.length == 3){
                    completions.addAll(Arrays.stream(PlotSetting.values()).map(ps -> ps.name().toLowerCase()).toList());
                }else if(args.length == 4 && firstArg.equals("setsetting")){
                    completions.addAll(List.of("true", "false", "delete"));
                }
            }

            case "deletegroup", "listmembers", "listpermissions" -> {
                if(args.length == 2 && args[1].length() == 0){
                    completions.add("[group]");
                } else if(args.length == 2){
                    Plot plot = assertOnOwnPlot(player, pm, adminMode, true);
                    if(plot == null) return;
                    completions.addAll(plot.getPlotData().getGroups().stream().map(g -> g.getGroupId().getGroupName()).toList());
                }
            }

            case "getgroup" -> {
                if(args.length == 2 && args[1].length() == 0){
                    completions.add("[player]");
                }else if(args.length == 2){
                    completions.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
                }
            }

            case "setgroup" -> {
                if(args.length == 2 && args[1].length() == 0){
                    completions.add("[player]");
                }else if(args.length == 2){
                    completions.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
                }else if(args.length == 3 && args[2].length() == 0){
                    completions.add("[group]");
                }else  if(args.length == 3){
                    Plot plot = assertOnOwnPlot(player, pm, adminMode, true);
                    if(plot == null) break;
                    completions.addAll(plot.getPlotData().getGroups().stream().map(g -> g.getGroupId().getGroupName()).toList());
                }
            }

            case "removeplayer" ->{
                if(args.length == 2){
                    Plot plot = assertOnOwnPlot(player, pm, adminMode, true);
                    if(plot == null) break;
                    completions.addAll(plot.getPlotData().getGroups().stream().flatMap(g -> g.getMembers().stream().map(member -> member.getPlotMemberId().getUuid())).map(NeincraftUtils::uuidToName).toList());
                }
            }

            case "setpermission" -> {
                if(args.length == 2 && args[1].length() == 0){
                    completions.add("[group]");
                }else if (args.length == 2){
                    Plot plot = assertOnOwnPlot(player, pm, adminMode,true);
                    if(plot == null) break;
                    completions.addAll(plot.getPlotData().getGroups().stream().map(g -> g.getGroupId().getGroupName()).toList());
                }else if(args.length == 3){
                    completions.addAll(Arrays.stream(PlotPermission.values()).map(pp -> pp.name().toLowerCase()).toList());
                }else if(args.length == 4){
                    completions.addAll(List.of("true", "false", "delete"));
                }
            }

            case "listplots" -> {
                if(args.length == 2 && adminMode && args[1].length() == 0){
                    completions.add("[player]");
                }else if (args.length == 2 && adminMode){
                    completions.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
                }
            }

            case "reloaddata" -> {
                if(args.length == 2 && adminMode){
                    completions.addAll(getPlotNameCompletions(player, args[1], pm, true));
                }
            }
        }
    }
    
    private List<String> getPlotNameCompletions(Player player, String arg, PlotModule pm, boolean qualified){
        if(!qualified){
            return pm.getPlotsOfOwner(player.getUniqueId()).stream().map(plot -> plot.getPlotData().getName()).toList();
        }else if(arg.length() == 0){
            return List.of("[player]:");
        }else if(!arg.contains(":")){
            return Arrays.stream(Bukkit.getOfflinePlayers()).map(op -> op.getName() +":").toList();
        }
        String targetName = arg.substring(0, arg.indexOf(":"));
        List<Plot> plots = null;
        if(targetName.equals("")){
            plots = pm.getPlotsOfOwner(null);
        }else {
            Optional<UUID> target = NeincraftUtils.nameToUuid(targetName);
            if(target.isPresent()){
                plots = pm.getPlotsOfOwner(target.get());
            }else{
                return Collections.emptyList();
            }
        }
        return plots.stream().map(plot -> targetName + ":" + plot.getPlotData().getName()).toList();
        
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean adminMode = label.toLowerCase().endsWith("admin");
        if(!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Lang.WRONG_EXECUTOR.getComponent(null, null));
            return true;
        }
        Optional<PlayerStats> stats = AbstractModule.getInstance(PlayerStats.class);
        if(stats.isEmpty()){
            player.sendMessage(Lang.FATAL_ERROR.getComponent(PlayerLanguage.AUTO, player));
            getLogger().log(Level.WARNING, "Module PlayerData not present when it should be");
            return true;
        }
        Optional<PlotModule> oPlotModule = AbstractModule.getInstance(PlotModule.class);
        if(oPlotModule.isEmpty()){
            player.sendMessage(Lang.FATAL_ERROR.getComponent(PlayerLanguage.AUTO, player));
            getLogger().log(Level.WARNING, "Module PlotModule not present when it should be");
            return true;
        }

        PlotModule pm = oPlotModule.get();

        PlayerData pd = stats.get().getOrCreate(player.getUniqueId());
        if(args.length <= 0){
            player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                    "label", label,
                    "args", "(info|create)"
            ).toComponent());
            return true;
        }
        String firstArg = args[0].toLowerCase();

        main: switch(firstArg){

            case "info" -> {showInfo(player, pd.getLanguage(), pm);}

            case "create" -> {
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", "create [name]"
                    ).toComponent());
                    break;
                }
                createPlot(adminMode, player, pd, args[1], pm);
            }

            case "delete" -> {
                if(args.length < 2 || args.length > 3){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", "delete [name]"
                    ).toComponent());
                }
                String plotName = args[1];
                Optional<Plot> optionalPlot;
                if(adminMode){
                    optionalPlot = pm.findByName(plotName);
                }else {
                    optionalPlot = pm.findByName(plotName, player.getUniqueId());
                }
                if(optionalPlot.isEmpty()){
                    commandSender.sendMessage(Lang.PLOT_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                String token = plotToToken(optionalPlot.get().getPlotData().getId());
                if(args.length == 2){
                    player.sendMessage(Lang.PLOT_DELETION_CONFIRM.getMinedown(pd.getLanguage(), player).replace(
                            "command", "/" + label.toLowerCase() + " delete " + plotName + " " + token
                    ).toComponent());
                    break;
                }
                String enteredToken = args[2];
                if(!token.equals(enteredToken)){
                    player.sendMessage(Lang.PLOT_CONFIRMATION_FAILED.getComponent(pd.getLanguage(), player));
                    break;
                }
                pm.deletePlot(optionalPlot.get());
                player.sendMessage(Lang.PLOT_DELETED.getComponent(pd.getLanguage(), player));
            }

            case "addchunk" -> {
                if(args.length > 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " ([plot])"
                    ).toComponent());
                    break;
                }

                ChunkKey chunkKey = ChunkKey.fromChunk(player.getChunk());
                if(pm.getPlotAtChunk(chunkKey).isPresent()){
                    player.sendMessage(Lang.ALREADY_OCCUPIED.getComponent(pd.getLanguage(), player));
                    break;
                }

                if(!adminMode && pm.getFreeChunks(pd) < 1){
                    player.sendMessage(Lang.INSUFFICIENT_CHUNKS.getComponent(pd.getLanguage(), player));
                    break;
                }
                List<Plot> neighbouringPlots = getDirectNeighbours(chunkKey).stream().map(pm::getPlotAtChunk).filter(Optional::isPresent).map(Optional::get).distinct().filter(plot -> adminMode || player.getUniqueId().equals(plot.getPlotData().getOwner())).toList();

                if(neighbouringPlots.size() > 1 && args.length == 1){
                    player.sendMessage(Lang.MULTIPLE_PLOTS_SURROUNDING.getComponent(pd.getLanguage(), player));
                    for(Plot p : neighbouringPlots){
                        String plotName = (adminMode ? (p.isServerPlot() ? "" : NeincraftUtils.uuidToName(p.getPlotData().getOwner())) + ":" : "") + p.getPlotData().getName();
                        player.sendMessage(new MineDown("\n - [" +  plotName + "](color=gold run_command=/" + label + " addchunk " + plotName + ")").toComponent());
                    }
                    break;
                }
                Plot plot = null;
                if(neighbouringPlots.size() > 1){
                    String plotName = args[1];
                    Optional<Plot> queriedPlot = adminMode ? pm.findByName(plotName) : pm.findByName(plotName, player.getUniqueId());
                    if(queriedPlot.isEmpty() || neighbouringPlots.stream().noneMatch(p -> p == queriedPlot.get())){
                        player.sendMessage(Lang.PLOT_NOT_FOUND.getComponent(pd.getLanguage(), player));
                        break;
                    }
                    plot = queriedPlot.get();
                }else if(neighbouringPlots.size() == 1){
                    plot = neighbouringPlots.get(0);
                }else{
                    player.sendMessage(Lang.NO_PLOTS_SURROUNDING.getComponent(pd.getLanguage(), player));
                    break;
                }

                if(!adminMode && isConnectingChunk(plot, chunkKey)){
                    player.sendMessage(Lang.PLOT_ENCLOSES_AREA.getComponent(pd.getLanguage(), player));
                    break;
                }

                plot.addChunk(chunkKey);
                plot.persist();
                player.sendMessage(Lang.CHUNK_ASSIGNED.getComponent(pd.getLanguage(), player));
                PlotUtils.visualize(plot, player);
            }

            case "removechunk" ->{
                if(args.length > 1){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                ChunkKey chunkKey = ChunkKey.fromChunk(player.getChunk());
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;

                if(plot.getPlotData().getChunks().size() <= 1){
                    player.sendMessage(Lang.LAST_REMAINING_CHUNK.getComponent(pd.getLanguage(), player));
                    break;
                }

                if(ChunkKey.fromChunk(plot.getHome().getChunk()).equals(chunkKey)){
                    player.sendMessage(Lang.HOME_CHUNK_REMOVED.getComponent(pd.getLanguage(), player));
                    break;
                }

                if(!adminMode && isDirectlyEnclosed(plot, chunkKey)){
                    player.sendMessage(Lang.PLOT_ENCLOSES_AREA.getComponent(pd.getLanguage(), player));
                    break;
                }

                if(!adminMode && isConnectingChunk(plot, chunkKey)){
                    player.sendMessage(Lang.PLOT_SEPARATED.getComponent(pd.getLanguage(), player));
                    break;
                }

                plot.removeChunk(chunkKey);
                plot.persist();
                player.sendMessage(Lang.CHUNK_DELETED.getComponent(pd.getLanguage(), player));
                PlotUtils.visualize(plot, player);
            }

            case "createsubdivision" ->{
                if(args.length != 2) {
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [name]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                if(!PlotModule.PLOT_NAME_PATTERN.matcher(args[1]).matches()){
                    player.sendMessage(Lang.PLOT_NAME_INVALID.getComponent(pd.getLanguage(), player));
                    break;
                }
                if(plot.getSubdivision(args[1]) != null){
                    player.sendMessage(Lang.PLOT_SUBDIVISION_EXISTS.getComponent(pd.getLanguage(), player));
                    break;
                }
                plot.createSubdivision(args[1]);
                plot.persist();
                player.sendMessage(Lang.PLOT_SUBDIVISION_CREATED.getComponent(pd.getLanguage(), player));
            }

            case "assignchunk" ->{
                if(args.length != 2) {
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [name]"
                    ).toComponent());
                    break;
                }
                ChunkKey chunkKey = ChunkKey.fromChunk(player.getChunk());
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                SubdivisionData subdivisionData = plot.getSubdivision(args[1]);
                if(subdivisionData == null){
                    player.sendMessage(Lang.PLOT_SUBDIVISION_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                ChunkData chunkData = plot.getChunkData(chunkKey);
                if(chunkData == null){
                    getLogger().log(Level.WARNING, "Player %s failed to assign chunk %s to a subdivision %s on plot %s: Chunk not found");
                    player.sendMessage(Lang.FATAL_ERROR.getComponent(pd.getLanguage(), player));
                    break;
                }
                chunkData.setSubdivision(subdivisionData);
                plot.persist();
                player.sendMessage(Lang.CHUNK_ASSIGNED.getComponent(pd.getLanguage(), player));

            }

            case "unassign" ->{
                if(args.length != 1) {
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                Bukkit.dispatchCommand(player, label + " assignchunk main");
            }

            case "deletesubdivision" ->{
                if(args.length != 2) {
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [name]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                SubdivisionData subdivisionData = plot.getSubdivision(args[1]);
                if(subdivisionData == null){
                    player.sendMessage(Lang.PLOT_SUBDIVISION_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                if(!plot.deleteSubdivision(subdivisionData)){

                }
                plot.persist();
                player.sendMessage(Lang.PLOT_SUBDIVISION_DELETED.getComponent(pd.getLanguage(), player));
            }

            case "listsubdivisions" ->{
                if(args.length > 1){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                player.sendMessage(Lang.PLOT_SUBDIVISION_LIST.getComponent(pd.getLanguage(), player));
                player.sendMessage(new MineDown("&gold&" + plot.getPlotData().getSubdivisions().stream().map(sd -> sd.getSubdivisionId().getName()).collect(Collectors.joining(", "))).toComponent());
            }

            case "setsetting" ->{
                if(args.length != 4) {
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [subdivision] [setting] [value (true|false|delete)]"
                    ).toComponent());
                    break;
                }
                    Plot plot = assertOnOwnPlot(player, pm, adminMode);
                    if(plot == null) break;
                    SubdivisionData subdivisionData = plot.getSubdivision(args[1]);
                    if(subdivisionData == null){
                        player.sendMessage(Lang.PLOT_SUBDIVISION_NOT_FOUND.getComponent(pd.getLanguage(), player));
                        break;
                    }
                    PlotSetting setting = PlotSetting.findByName(args[2]);
                    if(setting == null){
                        player.sendMessage(Lang.PLOT_SETTING_NOT_FOUND.getComponent(pd.getLanguage(), player));
                        break;
                    }
                    if(!adminMode && !setting.isUserEditable()){
                        player.sendMessage(Lang.PLOT_SETTING_PROTECTED.getComponent(pd.getLanguage(), player));
                        break;
                    }
                    switch (args[3].toLowerCase()){
                        case "true" -> {
                            plot.setSetting(subdivisionData, setting, true);
                        }
                        case "false" -> {
                            plot.setSetting(subdivisionData, setting, false);
                        }
                        case "delete" ->{
                            plot.unsetSetting(subdivisionData, setting);
                        }
                        default ->{
                            player.sendMessage(Lang.PLOT_SETTING_VALUE_INCORRECT.getComponent(pd.getLanguage(), player));
                            break main;
                        }
                    }
                    plot.persist();
                    player.sendMessage(Lang.PLOT_SETTING_UPDATED.getComponent(pd.getLanguage(), player));

            }

            case "showsettings" ->{
                if(args.length > 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " ([subdivision])"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                SubdivisionData subdivisionData = null;
                if(args.length == 1){
                    subdivisionData = plot.getChunkData(ChunkKey.fromChunk(player.getChunk())).getSubdivision();
                }else{
                    subdivisionData = plot.getSubdivision(args[1]);
                }
                if(subdivisionData == null){
                    player.sendMessage(Lang.PLOT_SUBDIVISION_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                player.sendMessage(Lang.PLOT_SETTINGS_LIST.getComponent(pd.getLanguage(), player));
                for(PlotSetting setting : PlotSetting.values()){
                    boolean value;
                    SubdivisionData[] definedIn = new SubdivisionData[1];
                    value = plot.resolveSettingsValue(subdivisionData, setting, s -> definedIn[0] = s);
                    player.sendMessage(Lang.PLOT_SETTINGS_ENTRY.getMinedown(pd.getLanguage(), player).replace(
                            "setting", setting.name().toLowerCase(),
                            "description", setting.getDescription().getRawString(pd.getLanguage(), player),
                            "value", value ? Lang.YES.getRawString(pd.getLanguage(), player) : Lang.NO.getRawString(pd.getLanguage(), player),
                            "valueColor", value ? "green" : "red",
                            "defined", definedIn[0] != null ? definedIn[0].getSubdivisionId().getName() : Lang.DEFAULT.getRawString(pd.getLanguage(), player)
                    ).toComponent());
                }
            }

            case "creategroup" ->{
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [name]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                if(plot.getGroup(args[1]) != null){
                    player.sendMessage(Lang.PLOT_GROUP_EXISTS.getComponent(pd.getLanguage(), player));
                    break;
                }
                if(!PlotModule.PLOT_NAME_PATTERN.matcher(args[1]).matches()){
                    player.sendMessage(Lang.PLOT_NAME_INVALID.getComponent(pd.getLanguage(), player));
                    break;
                }
                plot.createGroup(args[1]);
                plot.persist();
                player.sendMessage(Lang.PLOT_GROUP_CREATED.getComponent(pd.getLanguage(), player));
            }

            case "deletegroup" ->{
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [name]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                PlotMemberGroup group = plot.getGroup(args[1]);
                if(group == null){
                    player.sendMessage(Lang.PLOT_GROUP_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                if(!plot.deleteGroup(group)){
                    player.sendMessage(Lang.PLOT_GROUP_PROTECTED.getComponent(pd.getLanguage(), player));
                    break;
                }
                plot.persist();
                player.sendMessage(Lang.PLOT_GROUP_DELETED.getComponent(pd.getLanguage(), player));
            }

            case "listgroups" ->{
                if(args.length != 1){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                player.sendMessage(Lang.PLOT_GROUP_LIST.getComponent(pd.getLanguage(), player));
                player.sendMessage(
                        new MineDown("&gold&" + plot.getPlotData().getGroups().stream().map(group -> group.getGroupId().getGroupName()).collect(Collectors.joining(", "))).toComponent()
                );
            }

            case "listmembers" ->{
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] +" [group]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                PlotMemberGroup group = plot.getGroup(args[1]);
                if(group == null){
                    player.sendMessage(Lang.PLOT_GROUP_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                boolean everyone = group.getGroupId().getGroupName().equalsIgnoreCase("everyone");
                Component listHeader = everyone ? Lang.PLOT_GROUP_EVERYONE_LIST.getComponent(pd.getLanguage(), player) : Lang.PLOT_GROUP_MEMBERS_LIST.getMinedown(pd.getLanguage(), player).replace("group", group.getGroupId().getGroupName()).toComponent();
                String members = everyone ? plot.getPlotData().getGroups().stream().flatMap(g -> g.getMembers().stream().map(member -> member.getPlotMemberId().getUuid())).distinct().map(NeincraftUtils::uuidToName).collect(Collectors.joining(", ")) : group.getMembers().stream().map(member -> member.getPlotMemberId().getUuid()).distinct().map(NeincraftUtils::uuidToName).collect(Collectors.joining(", "));
                player.sendMessage(listHeader);
                player.sendMessage(new MineDown("&gold&" + members).toComponent());
            }

            case "getgroup" ->{
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] +" [player]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                Optional<UUID> playerUUID = NeincraftUtils.nameToUuid(args[1]);
                if(playerUUID.isEmpty()){
                    player.sendMessage(Lang.PLAYER_NOT_FOUND.getMinedown(pd.getLanguage(), player).replace(
                            "player", args[1]
                    ).toComponent());
                    break;
                }
                PlotMemberGroup group = plot.getPlayerGroup(playerUUID.get());
                player.sendMessage(Lang.PLOT_GROUP_MEMBER_QUERY.getMinedown(pd.getLanguage(), player).replace(
                        "player", args[1],
                        "group", group.getGroupId().getGroupName()
                ).toComponent());
            }

            case "setgroup" ->{
                if(args.length != 3){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [player] [group]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                Optional<UUID> target = NeincraftUtils.nameToUuid(args[1]);
                if(target.isEmpty()){
                    player.sendMessage(Lang.PLAYER_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                PlotMemberGroup group = plot.getGroup(args[2]);
                if(group == null){
                    player.sendMessage(Lang.PLOT_GROUP_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                plot.setPlayerGroup(target.get(), group);
                plot.persist();
                player.sendMessage(Lang.PLOT_GROUP_MEMBER_ADDED.getMinedown(pd.getLanguage(), player).replace(
                        "player", NeincraftUtils.uuidToName(target.get()),
                        "group", group.getGroupId().getGroupName()
                ).toComponent());
            }

            case "removePlayer" ->{
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [player]"
                    ).toComponent());
                    break;
                }
                Bukkit.dispatchCommand(player, label + " setgroup " + args[1] + " everyone");
            }

            case "setpermission" ->{
                if(args.length != 4){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] +" [group] [permission] [value]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                SubdivisionData subdivisionData = plot.getChunkData(ChunkKey.fromChunk(player.getChunk())).getSubdivision();
                PlotMemberGroup group = plot.getGroup(args[1]);
                if(group == null){
                    player.sendMessage(Lang.PLOT_GROUP_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                PlotPermission plotPermission = PlotPermission.findByName(args[2]);
                if(plotPermission == null){
                    player.sendMessage(Lang.PLOT_PERMISSION_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                switch (args[3].toLowerCase()){
                    case "true" -> {
                        plot.setPermission(subdivisionData, group, plotPermission, true);
                    }
                    case "false" -> {
                        plot.setPermission(subdivisionData, group, plotPermission, false);
                    }
                    case "delete" -> {
                        plot.unsetPermission(subdivisionData, group, plotPermission);
                    }
                    default -> {
                        player.sendMessage(Lang.PLOT_PERMISSION_VALUE_INCORRECT.getComponent(pd.getLanguage(), player));
                        break main;
                    }
                }
                plot.persist();
                player.sendMessage(Lang.PLOT_PERMISSION_UPDATED.getComponent(pd.getLanguage(),player));
            }

            case "listpermissions" ->{
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [group]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                SubdivisionData subdivisionData = plot.getChunkData(ChunkKey.fromChunk(player.getChunk())).getSubdivision();
                PlotMemberGroup group = plot.getGroup(args[1]);
                if(group == null){
                    player.sendMessage(Lang.PLOT_GROUP_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                player.sendMessage(Lang.PLOT_PERMISSION_LIST.getMinedown(pd.getLanguage(), player).replace(
                        "group", group.getGroupId().getGroupName(),
                        "subdivision", subdivisionData.getSubdivisionId().getName()
                ).toComponent());
                for(PlotPermission permission : PlotPermission.values()){
                    SubdivisionData[] definedInSubdivision = new SubdivisionData[1];
                    PlotMemberGroup[] definedInGroup = new PlotMemberGroup[1];
                    boolean value = plot.resolvePermission(subdivisionData, group, permission, (s, g) -> {
                        definedInSubdivision[0] = s;
                        definedInGroup[0] =  g;
                    });
                    player.sendMessage(Lang.PLOT_PERMISSION_ENTRY.getMinedown(pd.getLanguage(), player).replace(
                            "permission", permission.name().toLowerCase(),
                            "value", value ? Lang.YES.getRawString(pd.getLanguage(), player) : Lang.NO.getRawString(pd.getLanguage(), player),
                            "valueColor", value ? "green" : "red",
                            "subdivision", definedInSubdivision[0] != null ? definedInSubdivision[0].getSubdivisionId().getName() : Lang.DEFAULT.getRawString(pd.getLanguage(), player),
                            "group", definedInGroup[0] != null ? definedInGroup[0].getGroupId().getGroupName() : Lang.DEFAULT.getRawString(pd.getLanguage(), player)
                    ).toComponent());
                }
            }

            case "sethome" ->{
                if(args.length != 1){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pm, adminMode);
                if(plot == null) break;
                plot.setHome(player.getLocation());
                plot.persist();
                player.sendMessage(Lang.HOME_SET.getComponent(pd.getLanguage(), player));
            }

            case "listplots" ->{
                if((!adminMode && args.length != 1) || (adminMode && args.length != 2)){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + (adminMode ? " [player]" : "")
                    ).toComponent());
                    break;
                }
                UUID targetPlayer = adminMode ? NeincraftUtils.nameToUuid(args[1]).orElse(null) : player.getUniqueId();
                if(targetPlayer == null){
                    player.sendMessage(Lang.PLAYER_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }

                List<Plot> ownPlots = pm.getPlotsOfOwner(targetPlayer);
                List<Plot> memberPlots = pm.getPlotsWithGroupMember(targetPlayer);
                List<Plot> homePlots = pm.getPlotsWithPermissionOnMain(targetPlayer, PlotPermission.TELEPORT_HOME);

                player.sendMessage(Lang.PLOT_LIST.getMinedown(pd.getLanguage(), player).replace(
                        "player", NeincraftUtils.uuidToName(targetPlayer)
                ).toComponent());

                player.sendMessage(Lang.PLOT_LIST_OWNER.getComponent(pd.getLanguage(), player));
                if(ownPlots.isEmpty()){
                    player.sendMessage(Lang.LIST_EMPTY.getComponent(pd.getLanguage(), player));
                }else{
                    player.sendMessage(new MineDown("&gray&" + ownPlots.stream().map(plot -> plot.getPlotData().getName()).sorted().collect(Collectors.joining(", "))).toComponent());
                }

                player.sendMessage(Lang.PLOT_LIST_MEMBER.getComponent(pd.getLanguage(), player));
                if(memberPlots.isEmpty()){
                    player.sendMessage(Lang.LIST_EMPTY.getComponent(pd.getLanguage(), player));
                }else{
                    player.sendMessage(new MineDown("&gray&" + memberPlots.stream().map(plot -> (plot.isServerPlot() ? "" : NeincraftUtils.uuidToName(plot.getPlotData().getOwner())) + ":" + plot.getPlotData().getName()).sorted().collect(Collectors.joining(", "))).toComponent());
                }

                player.sendMessage(Lang.PLOT_LIST_HOME.getComponent(pd.getLanguage(), player));
                if(ownPlots.isEmpty() && homePlots.isEmpty()){
                    player.sendMessage(Lang.LIST_EMPTY.getComponent(pd.getLanguage(), player));
                }else{
                    player.sendMessage(new MineDown("&gray&" +
                            /*
                            Convert own Plots to plot names as sorted stream ->
                            filter plots the target does not own from plots with home permission, convert to sorted stream of "[owner]:[plotName]" ->
                            concat the two streams and join with ", " as delimiter.
                             */
                            Stream.concat(ownPlots.stream().map(plot -> plot.getPlotData().getName()).sorted(),
                                    homePlots.stream().filter(plot -> !targetPlayer.equals(plot.getPlotData().getOwner())).map(plot -> (plot.isServerPlot() ? "" : NeincraftUtils.uuidToName(plot.getPlotData().getOwner())) + ":" + plot.getPlotData().getName()).sorted())
                                    .collect(Collectors.joining(", "))).toComponent());
                }
            }

            case "reloaddata" ->{
                if(!adminMode) break;
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [plot]"
                    ).toComponent());
                    break;
                }
                Optional<Plot> oPlot = pm.findByName(args[1]);
                if(oPlot.isEmpty()){
                    player.sendMessage(Lang.PLOT_NOT_FOUND.getComponent(pd.getLanguage(), player));
                    break;
                }
                oPlot.get().refreshData();
                player.sendMessage(Lang.PLOT_RELOADED.getComponent(pd.getLanguage(), player));
            }

            default -> {
                player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                        "label", label,
                        "args", "[action] [...]"
                ).toComponent());
            }
        }
        return true;
    }

    private String plotToToken(long plotId){
        String token = new Random(plotId).ints(0, 9).limit(4).boxed().map(i -> "" + i).collect(Collectors.joining());
        return token;
    }

    private Plot assertOnOwnPlot(Player player, PlotModule pm, boolean adminMode, boolean silent){
        Optional<Plot> oPlot = pm.getPlotAtChunk(ChunkKey.fromChunk(player.getChunk()));
        if(oPlot.isEmpty()){
            if(!silent) player.sendMessage(Lang.PLOT_NOT_AT_LOCATION.getComponent(player));
            return null;
        }
        Plot plot = oPlot.get();
        if(!adminMode && !player.getUniqueId().equals(plot.getPlotData().getOwner())){
            if(!silent) player.sendMessage(Lang.NO_MANAGEMENT_PERMISSION.getComponent(player));
            return null;
        }
        return plot;
    }

    private Plot assertOnOwnPlot(Player player, PlotModule pm, boolean adminMode){
        return assertOnOwnPlot(player, pm, adminMode, false);
    }

    private void showInfo(Player p, PlayerLanguage lang, PlotModule pm){
        Optional<Plot> oPlot = pm.getPlotAtChunk(ChunkKey.fromChunk(p.getChunk()));
        if(oPlot.isPresent()){
            Plot plot = oPlot.get();
            p.sendMessage(Lang.PLOT_INFO.getMinedown(lang, p).replace(
                    "name", plot.getPlotData().getName(),
                    "chunks", "" + plot.getPlotData().getChunks().size(),
                    "owner", plot.getPlotData().getOwner() != null ? NeincraftUtils.uuidToName(plot.getPlotData().getOwner()) :  "Server",
                    "group", plot.getPlayerGroup(p.getUniqueId()).getGroupId().getGroupName(),
                    "subdivision", plot.getChunkData(ChunkKey.fromChunk(p.getChunk())).getSubdivision().getSubdivisionId().getName())
                    .toComponent());
            PlotUtils.visualize(plot, p);
        }else{
            p.sendMessage(Lang.PLOT_NOT_AT_LOCATION.getComponent(lang, p));
        }
    }

    private void createPlot(boolean adminMode, Player p, PlayerData pd, String name, PlotModule pm){
        PlayerLanguage lang = pd.getLanguage();
        Chunk c = p.getChunk();
        if(!adminMode && !pm.isPublicWorld(c.getWorld())){
            p.sendMessage(Lang.INVALID_WORLD.getComponent(lang, p));
            getLogger().log(Level.FINE, String.format("Player's (%s) plot creation attempt failed because of an invalid world", p.getName()));
            return;
        }
        if(pm.getPlotAtChunk(ChunkKey.fromChunk(c)).isPresent()){
            p.sendMessage(Lang.ALREADY_OCCUPIED.getComponent(lang, p));
            getLogger().log(Level.FINE, String.format("Player's (%s) plot creation attempt failed because the chunk was already occupied", p.getName()));
            return;
        }
        if(!adminMode && pm.getFreePlots(pd) <= 0){
            p.sendMessage(Lang.INSUFFICIENT_PLOTS.getComponent(lang, p));
            getLogger().log(Level.FINE, String.format("Player's (%s) plot creation attempt failed because they had insufficient plots", p.getName()));
            return;
        }
        if(!adminMode && pm.getFreeChunks(pd) <= 0){
            p.sendMessage(Lang.INSUFFICIENT_CHUNKS.getComponent(lang, p));
            getLogger().log(Level.FINE, String.format("Player's (%s) plot creation attempt failed because they had insufficient chunks", p.getName()));
            return;
        }
        if(!adminMode && !PlotModule.PLOT_NAME_PATTERN.matcher(name).matches()){
            p.sendMessage(Lang.PLOT_NAME_INVALID.getMinedown(lang, p).replace(
                    "min", "" + 3,
                    "max", "" + 32,
                    "chars", "a-z, A-Z, 0-9, _, äöüÄÖÜ, ßẞ"
            ).toComponent());
            getLogger().log(Level.FINE, String.format("Player's (%s) plot creation attempt failed because their chosen name was invalid", p.getName()));
            return;
        }

        if(pm.findByName(name, adminMode ? null : p.getUniqueId()).isPresent()){
            p.sendMessage(Lang.PLOT_NAME_OCCUPIED.getComponent(lang, p));
            getLogger().log(Level.FINE, String.format("Player's (%s) plot creation attempt failed because their chosen name was occupied", p.getName()));
            return;
        }
        Plot plot = Plot.createNewPlot(name, ChunkKey.fromChunk(c), adminMode ? null : p.getUniqueId(), LocationData.fromBukkitlocation(p.getLocation()));
        pm.addPlot(plot);
        p.sendMessage(Lang.PLOT_CREATED.getComponent(lang, p));
        PlotUtils.visualize(plot, p);
    }

    private boolean isConnectingChunk(Plot p, ChunkKey newChunk){
        List<ChunkKey> chunks = getNeighboursCounterClockwise(newChunk);

        int transitions = 0;

        for(int i = 0; i < chunks.size(); i++){
            ChunkKey current = chunks.get(i);
            ChunkKey next = chunks.get((i+1)%chunks.size());
            if(p.hasChunk(current) != p.hasChunk(next)) transitions++;
            if(transitions > 2) return true;
        }
        return false;
    }

    private boolean isDirectlyEnclosed(Plot p, ChunkKey newChunk){
        return getDirectNeighbours(newChunk).stream().allMatch(p::hasChunk);
    }

    private List<ChunkKey> getDirectNeighbours(ChunkKey chunk){
        return List.of(
                new ChunkKey(chunk.getX(), chunk.getZ()+1, chunk.getWorld()), //south
                new ChunkKey(chunk.getX()+1, chunk.getZ(), chunk.getWorld()), //east
                new ChunkKey(chunk.getX(), chunk.getZ()-1, chunk.getWorld()), //north
                new ChunkKey(chunk.getX()-1, chunk.getZ(), chunk.getWorld())  //west
        );
    }

    private List<ChunkKey> getNeighboursCounterClockwise(ChunkKey chunk){
        return List.of(
                new ChunkKey(chunk.getX(), chunk.getZ()+1, chunk.getWorld()), //south
                new ChunkKey(chunk.getX()+1, chunk.getZ()+1, chunk.getWorld()), //south-east
                new ChunkKey(chunk.getX()+1, chunk.getZ(), chunk.getWorld()), //east
                new ChunkKey(chunk.getX()+1, chunk.getZ()-1, chunk.getWorld()), //north-east
                new ChunkKey(chunk.getX(), chunk.getZ()-1, chunk.getWorld()), //north
                new ChunkKey(chunk.getX()-1, chunk.getZ()-1, chunk.getWorld()), //north-west
                new ChunkKey(chunk.getX()-1, chunk.getZ(), chunk.getWorld()), //west
                new ChunkKey(chunk.getX()-1, chunk.getZ()+1, chunk.getWorld()) //south-west
        );
    }

}
