package de.neincraft.neincraftplugin.modules.plots;

import de.neincraft.neincraftplugin.NeincraftUtils;
import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.commands.NeincraftCommand;
import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerStats;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.modules.plots.dto.ChunkData;
import de.neincraft.neincraftplugin.modules.plots.dto.SubdivisionData;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.LocationData;
import de.neincraft.neincraftplugin.modules.plots.util.PlotSetting;
import de.neincraft.neincraftplugin.util.Lang;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlotCommand extends NeincraftCommand implements CommandExecutor, SimpleTabCompleter {

    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean adminMode = label.toLowerCase().endsWith("admin");
        if(!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Lang.WRONG_EXECUTOR.getComponent(null, null));
            return true;
        }
        Optional<PlayerStats> stats = Module.getInstance(PlayerStats.class);
        if(stats.isEmpty()){
            player.sendMessage(Lang.FATAL_ERROR.getComponent(PlayerLanguage.AUTO, player));
            getLogger().log(Level.WARNING, "Module PlayerData not present when it should be");
            return true;
        }
        Optional<PlotModule> oPlotModule = Module.getInstance(PlotModule.class);
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
                Plot plot = assertOnOwnPlot(player, pd, pm, adminMode);
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
            }

            case "subdivide" ->{
                if(args.length != 2) {
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [name]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pd, pm, adminMode);
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
                Plot plot = assertOnOwnPlot(player, pd, pm, adminMode);
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
                Bukkit.dispatchCommand(player, "plot assignchunk main");
            }

            case "deletesubdivision" ->{
                if(args.length != 2) {
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " [name]"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pd, pm, adminMode);
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

            case "settings" ->{
                if(args.length != 4){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + "[subdivision] [setting] [value (true|false|delete)]"
                    ).toComponent());
                    Plot plot = assertOnOwnPlot(player, pd, pm, adminMode);
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
            }

            case "showsettings" ->{
                if(args.length > 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(pd.getLanguage(), player).replace(
                            "label", label,
                            "args", args[0] + " ([subdivision])"
                    ).toComponent());
                    break;
                }
                Plot plot = assertOnOwnPlot(player, pd, pm, adminMode);
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
                    String[] definedIn = new String[1];
                    value = plot.resolveSettingsValue(subdivisionData.getName(), setting, s -> definedIn[0] = s);
                    player.sendMessage(Lang.PLOT_SETTINGS_ENTRY.getMinedown(pd.getLanguage(), player).replace(
                            "setting", setting.name().toLowerCase(),
                            "description", setting.getDescription().getRawString(pd.getLanguage(), player),
                            "value", value ? Lang.YES.getRawString(pd.getLanguage(), player) : Lang.NO.getRawString(pd.getLanguage(), player),
                            "valueColor", value ? "green" : "red",
                            "defined", definedIn[0]
                    ).toComponent());
                }
            }

            case "addgroup" ->{

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
                        "args", "(info|create)"
                ).toComponent());
            }
        }
        return true;
    }

    private String plotToToken(long plotId){
        String token = new Random(plotId).ints(0, 9).limit(4).boxed().map(i -> "" + i).collect(Collectors.joining());
        return token;
    }

    private Plot assertOnOwnPlot(Player player, PlayerData pd, PlotModule pm, boolean adminMode){
        Optional<Plot> oPlot = pm.getPlotAtChunk(ChunkKey.fromChunk(player.getChunk()));
        if(oPlot.isEmpty()){
            player.sendMessage(Lang.PLOT_NOT_AT_LOCATION.getComponent(pd.getLanguage(), player));
            return null;
        }
        Plot plot = oPlot.get();
        if(!adminMode && !player.getUniqueId().equals(plot.getPlotData().getOwner())){
            player.sendMessage(Lang.NO_MANAGEMENT_PERMISSION.getComponent(pd.getLanguage(), player));
            return null;
        }
        return plot;
    }

    private void showInfo(Player p, PlayerLanguage lang, PlotModule pm){
        Optional<Plot> oPlot = pm.getPlotAtChunk(ChunkKey.fromChunk(p.getChunk()));
        if(oPlot.isPresent()){
            Plot plot = oPlot.get();
            p.sendMessage(Lang.PLOT_INFO.getMinedown(lang, p).replace(
                    "name", plot.getPlotData().getName(),
                    "chunks", "" + plot.getPlotData().getChunks().size(),
                    "owner", plot.getPlotData().getOwner() != null ? NeincraftUtils.uuidToName(plot.getPlotData().getOwner()) :  "Server",
                    "group", plot.getPlayerGroup(p.getUniqueId()).getGroupId().getGroupName())
                    .toComponent());
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
        pm.addPlot(Plot.createNewPlot(name, ChunkKey.fromChunk(c), adminMode ? null : p.getUniqueId(), LocationData.fromBukkitlocation(p.getLocation())));
        p.sendMessage(Lang.PLOT_CREATED.getComponent(lang, p));
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
