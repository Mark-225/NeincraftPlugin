package de.neincraft.neincraftplugin.modules.plots;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.plots.protection.PlotProtection;
import de.neincraft.neincraftplugin.modules.plots.util.HomeCommand;
import de.neincraft.neincraftplugin.modules.plots.util.PlotPermission;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.modules.plots.bluemap.BluemapIntegration;
import de.neincraft.neincraftplugin.modules.plots.dto.PlotData;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import de.neincraft.neincraftplugin.modules.plots.repository.PlotRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NeincraftModule(id = "plots", requiredModules = {"database"})
public class PlotModule extends AbstractModule {

    public static final Pattern PLOT_NAME_PATTERN = Pattern.compile("[\\wÄÖÜäöüßẞ]{3,32}");

    private List<Plot> loadedPlots = new ArrayList<>();
    private BluemapIntegration bluemapIntegration;
    private PlotProtection plotProtection;
    private HashSet<String> publicWorlds = new HashSet<>();

    @InjectCommand("plot")
    private PlotCommand plotCommand;

    @InjectCommand("plotadmin")
    private PlotCommand plotAdminCommand;

    @InjectCommand("home")
    private HomeCommand homeCommand;

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

        NeincraftPlugin.getInstance().saveResource("worlds.yml", false);
        File configFile = new File(NeincraftPlugin.getInstance().getDataFolder(), "worlds.yml");
        FileConfiguration worldsConfig = new YamlConfiguration();
        try {
            worldsConfig.load(configFile);
            if(worldsConfig.contains("publicWorlds") && worldsConfig.isList("publicWorlds"))
                publicWorlds.addAll(worldsConfig.getStringList("publicWorlds"));
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.WARNING, "Could not load worlds config file.", e);
        }

        plotProtection = new PlotProtection();
        return true;
    }

    private void loadAllPlots(){
        try(PlotRepository repository = PlotRepository.getRepository()){
            if(repository != null){
                List<PlotData> plots = repository.findAll();
                repository.close();
                loadedPlots = plots.stream().map(Plot::getPlotFromData).collect(Collectors.toList());
            }
        }catch(Exception e){
            getLogger().log(Level.SEVERE, "Could not load plots!", e);
        }

    }

    public Optional<Plot> getPlotAtChunk(ChunkKey chunk){
        return loadedPlots.stream().filter(plot -> plot.hasChunk(chunk)).findFirst();
    }

    public void addPlot(Plot plot){
        if(getLoadedPlotById(plot.getPlotData().getId()).isEmpty())
            loadedPlots.add(plot);
    }

    public void deletePlot(Plot plot){
        try(PlotRepository pr = PlotRepository.getRepository()){
            if(pr != null){
                pr.delete(plot.getPlotData());
                pr.commit();
                loadedPlots.remove(plot);
                removeMarkers(plot);
            }
        }catch(Exception e){
            getLogger().log(Level.WARNING, "Could not delete Plot", e);
        }
    }

    private void removeMarkers(Plot plot){
        if(Bukkit.getPluginManager().isPluginEnabled("BlueMap")) {
            bluemapIntegration.removePlot(plot);
        }
    }

    public Optional<Plot> getLoadedPlotById(long plotId){
        return loadedPlots.stream().filter(plot -> plot.getPlotData().getId() == plotId).findAny();
    }


    public boolean isPublicWorld(World w){
        return publicWorlds.contains(w.getName());
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
        Optional<UUID> playerUuid = NeincraftUtils.nameToUuid(player);
        if(playerUuid.isEmpty()) return Optional.empty();
        return findByName(plot, playerUuid.get());
    }

    public Map<Optional<UUID>, Plot> findAllByName(String name){
        return loadedPlots.stream().filter(plot -> plot.getPlotData().getName().equalsIgnoreCase(name)).collect(Collectors.toMap(plot -> Optional.of(plot.getPlotData().getOwner()), plot -> plot));
    }

    public List<Plot> getPlotsWithGroupMember(UUID player){
        return loadedPlots.stream().filter(plot -> !player.equals(plot.getPlotData().getOwner()) && !plot.getPlayerGroup(player).getGroupId().getGroupName().equalsIgnoreCase("everyone")).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Plot> getPlotsWithPermissionOnMain(UUID player, PlotPermission permission){
        return loadedPlots.stream().filter(plot -> plot.resolvePermission(plot.getSubdivision("main"), plot.getPlayerGroup(player), permission)).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Plot> getLoadedPlots(){
        return Collections.unmodifiableList(loadedPlots);
    }

    @Override
    public void unload() {

    }
}
