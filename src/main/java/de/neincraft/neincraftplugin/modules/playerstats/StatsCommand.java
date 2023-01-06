package de.neincraft.neincraftplugin.modules.playerstats;

import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.modules.plots.PlotModule;
import de.neincraft.neincraftplugin.util.lang.Lang;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import de.neincraft.neincraftplugin.modules.AbstractModule;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StatsCommand implements CommandExecutor, SimpleTabCompleter {
    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
        if(args.length != 1 || !commandSender.hasPermission("neincraft.stats.others")) return;
        completions.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1 && sender.hasPermission("neincraft.stats.others")){
            Optional<UUID> target = NeincraftUtils.nameToUuid(args[0]);
            if(target.isEmpty()){
                sender.sendMessage(Lang.PLAYER_NOT_FOUND.getComponent(sender instanceof Player player ? player : null));
                return true;
            }
            sender.sendMessage(createStats(target.get(), sender instanceof Player player ? player : null));
        }else if(args.length == 0 && sender instanceof Player player){
            player.sendMessage(createStats(player.getUniqueId(), player));
        }else{
            sender.sendMessage(Lang.WRONG_SYNTAX.getMinedown(sender instanceof Player player ? player : null).replace(
                    "label", label,
                    "args", ""
            ).toComponent());
        }
        return true;
    }

    private Component createStats(UUID target, @Nullable Player viewer){
        Optional<PlayerStats> statsModule = AbstractModule.getInstance(PlayerStats.class);
        Optional<PlotModule> plotModule = AbstractModule.getInstance(PlotModule.class);
        if(statsModule.isEmpty() || plotModule.isEmpty()) return Lang.FATAL_ERROR.getComponent(viewer);
        PlayerStats stats = statsModule.get();
        PlotModule pm = plotModule.get();
        PlayerData pd = stats.getOrCreate(target);
        long playtimeSeconds = pd.getSecondsPlayed();
        StringBuilder timeBuilder = new StringBuilder();
        if(playtimeSeconds >= 86_400) timeBuilder.append(playtimeSeconds / 86_400).append("d ");
        if(playtimeSeconds >= 3_600) timeBuilder.append((playtimeSeconds % 86_400) / 3_600).append("h ");
        if(playtimeSeconds >= 60) timeBuilder.append((playtimeSeconds % 3_600) / 60).append("m ");
        timeBuilder.append(playtimeSeconds % 60).append("s");
        String timeStr = timeBuilder.toString();
        int pointPlots = pm.getAvailablePlots(pd, false);
        long pointsForCurrentPlot = pm.getPointsForPlots(pointPlots);
        long pointsForNextPlot = pm.getPointsForPlots(pointPlots+1);
        double plotProgress = (double) (pd.getPoints() - pointsForCurrentPlot) / (double) (pointsForNextPlot - pointsForCurrentPlot);
        int pointChunks = pm.getAvailableChunks(pd, false);
        long pointsForCurrentChunk = pm.getPointsForChunks(pointChunks);
        long pointsForNextChunk = pm.getPointsForChunks(pointChunks+1);
        double chunkProgress = (double) (pd.getPoints() - pointsForCurrentChunk) / (double) (pointsForNextChunk - pointsForCurrentChunk);
        return Lang.PLAYER_STATS.getMinedown(viewer).replace(
                "player", NeincraftUtils.uuidToName(target),
                "playtime", timeStr,
                "plots", ""+(pointPlots + pd.getBonusPlots()),
                "pointplots", ""+pointPlots,
                "bonusplots", ""+pd.getBonusPlots(),
                "freeplots", ""+pm.getFreePlots(pd),
                "plotprogress", NeincraftUtils.createProgressBar("aqua", "green", "gray", 10, plotProgress),
                "plotpoints", ""+ (pd.getPoints() - pointsForCurrentPlot),
                "requiredplot", ""+ (pointsForNextPlot - pointsForCurrentPlot),
                "chunks", ""+(pointChunks + pd.getBonusChunks()),
                "pointchunks", ""+pointChunks,
                "bonuschunks", ""+pd.getBonusChunks(),
                "freechunks", ""+pm.getFreeChunks(pd),
                "chunkProgress", NeincraftUtils.createProgressBar("aqua", "green", "gray", 10, chunkProgress),
                "chunkpoints", ""+ (pd.getPoints() - pointsForCurrentChunk),
                "requiredchunk", ""+ (pointsForNextChunk - pointsForCurrentChunk)
        ).toComponent();
    }
}
