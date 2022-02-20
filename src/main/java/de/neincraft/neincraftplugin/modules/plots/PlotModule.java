package de.neincraft.neincraftplugin.modules.plots;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.plots.protection.PlotProtection;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.modules.plots.bluemap.BluemapIntegration;
import de.neincraft.neincraftplugin.modules.plots.dto.PlotData;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import de.neincraft.neincraftplugin.modules.plots.repository.PlotRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NeincraftModule(id = "plots", requiredModules = {"database"})
public class PlotModule extends Module {

    public static final Pattern PLOT_NAME_PATTERN = Pattern.compile("[\\wÄÖÜäöüßẞ]{3,32}");

    private List<Plot> loadedPlots = new ArrayList<>();
    private BluemapIntegration bluemapIntegration;
    private PlotProtection plotProtection;

    @InjectCommand("plot")
    private PlotCommand plotCommand;

    @InjectCommand("plotadmin")
    private PlotCommand plotAdminCommand;


    @Override
    protected boolean initModule() {
        loadAllPlots();
        if(Bukkit.getPluginManager().isPluginEnabled("BlueMap")) {
            bluemapIntegration = new BluemapIntegration();
            Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), () -> {
                for (Plot p : loadedPlots) {
                    if(p.needsMarkerUpdate()) {
                        bluemapIntegration.updatePlotMarkers(p);
                        p.resetMarkerUpdateStatus();
                    }
                }
            }, 200, 200);
        }
        plotProtection = new PlotProtection();
        return true;
    }

    private void loadAllPlots(){
        PlotRepository repository = PlotRepository.getRepository();
        if(repository == null){
            getLogger().log(Level.SEVERE, "Could not create database repository while loading plots!");
            return;
        }
        List<PlotData> plots = repository.findAll();
        repository.close();
        loadedPlots = plots.stream().map(Plot::getPlotFromData).collect(Collectors.toList());
    }

    public Optional<Plot> getPlotAtChunk(ChunkKey chunk){
        return loadedPlots.stream().filter(plot -> plot.hasChunk(chunk)).findFirst();
    }

    public void addPlot(Plot plot){
        if(getLoadedPlotById(plot.getPlotData().getId()).isEmpty())
            loadedPlots.add(plot);
    }

    public void deletePlot(Plot plot){
        PlotRepository pr = PlotRepository.getRepository();
        if(pr == null) return;
        pr.delete(plot.getPlotData());
        loadedPlots.remove(plot);
        pr.close();
    }

    public Optional<Plot> getLoadedPlotById(long plotId){
        return loadedPlots.stream().filter(plot -> plot.getPlotData().getId() == plotId).findAny();
    }

    @SuppressWarnings("deprecated")
    public boolean isPublicWorld(World w){
        return w.getGameRuleValue("isPlotWorld").equalsIgnoreCase("true");
    }

    public int getAvailablePlots(PlayerData pd, boolean includeBonus){
        return (int) (Math.sqrt((double) pd.getPoints() / 2000d) + 1 + (includeBonus ? pd.getBonusPlots() : 0));
    }

    public List<Plot> getPlotsOfOwner(UUID uuid){
        if(uuid != null)
            return loadedPlots.stream().filter(plot -> uuid.equals(plot.getPlotData().getOwner())).toList();
        return loadedPlots.stream().filter(Plot::isServerPlot).toList();
    }

    public int getFreePlots(PlayerData pd){
        return getAvailablePlots(pd, true) - getPlotsOfOwner(pd.getUuid()).size();
    }

    public long getPointsForPlots(int plots){
        if(plots <= 1) return 0;
        return (long) (2000 * Math.pow(plots - 1, 2));
    }

    public long getPointsForChunks(int chunks){
        return (long) (chunks-8) * 200 + (long) (chunks - 8) * (chunks - 7);
    }

    public int getChunksOfOwner(UUID uuid){
        return getPlotsOfOwner(uuid).stream().mapToInt(plot -> plot.getPlotData().getChunks().size()).sum();
    }

    public int getAvailableChunks(PlayerData pd, boolean includeBonus){
        return (int) ((-185d + Math.sqrt(4d*pd.getPoints() + 40401d)) / 2d + (includeBonus ? pd.getBonusChunks() : 0));
    }

    public int getFreeChunks(PlayerData pd){
        return getAvailableChunks(pd, true) - getChunksOfOwner(pd.getUuid());
    }

    public Optional<Plot> findByName(String name, UUID owner){
        return getPlotsOfOwner(owner).stream().filter(plot -> plot.getPlotData().getName().equalsIgnoreCase(name)).findFirst();
    }

    public Optional<Plot> findByName(String name){
        if(!name.contains(":")) return Optional.empty();
        String player = name.substring(0, name.indexOf(":"));
        String plot = name.substring(name.indexOf(":")+1);
        if(player.equalsIgnoreCase("")) return findByName(plot, null);
        Optional<UUID> playerUuid = NeincraftUtils.nameToUuid(name);
        if(playerUuid.isEmpty()) return Optional.empty();
        return findByName(plot, playerUuid.get());
    }

    public Map<Optional<UUID>, Plot> findAllByName(String name){
        return loadedPlots.stream().filter(plot -> plot.getPlotData().getName().equalsIgnoreCase(name)).collect(Collectors.toMap(plot -> Optional.of(plot.getPlotData().getOwner()), plot -> plot));
    }

    @Override
    public void unload() {

    }
}
