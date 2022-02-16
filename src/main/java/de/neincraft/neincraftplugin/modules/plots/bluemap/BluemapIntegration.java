package de.neincraft.neincraftplugin.modules.plots.bluemap;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.ShapeMarker;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.NeincraftUtils;
import de.neincraft.neincraftplugin.modules.plots.Plot;
import de.neincraft.neincraftplugin.modules.plots.util.PlotUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BluemapIntegration {

    public BluemapIntegration(){
        BlueMapAPI.onEnable(api ->{
            createMarkerSet();
        });
    }

    private void createMarkerSet(){
        BlueMapAPI.getInstance().ifPresent(api ->{
            try {
                MarkerAPI mApi = api.getMarkerAPI();
                MarkerSet ms = mApi.createMarkerSet("neincraft_plots");
                ms.setLabel("Grundstücke");
                mApi.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void updatePlotMarkers(Plot plot){
        World w = Bukkit.getWorld(plot.getPlotData().getWorldName());
        if(w == null) return;
        final UUID worldUuid = w.getUID();
        final long plotId = plot.getPlotData().getId();
        final String plotName = plot.getPlotData().getName();
        final String plotOwner = plot.isServerPlot() ? "Server" : NeincraftUtils.uuidToName(plot.getPlotData().getOwner());
        final boolean adminPlot = plot.isServerPlot();
        final Set<Vector2i> chunks = plot.getPlotData().getChunks().stream().map(cd -> new Vector2i(cd.getId().getX(), cd.getId().getZ())).collect(Collectors.toCollection(HashSet::new));
        Bukkit.getScheduler().runTaskAsynchronously(NeincraftPlugin.getInstance(), () ->{
            BlueMapAPI.getInstance().ifPresent(api ->{
                Optional<BlueMapWorld> bmw = api.getWorld(worldUuid);
                if(bmw.isEmpty()) return;
                BlueMapWorld world = bmw.get();
                if(world.getMaps().isEmpty()) return;
                List<List<Vector2d>> plotSectors = PlotUtils.areaToBlockPolygon(chunks);
                try {
                    MarkerAPI mApi = api.getMarkerAPI();
                    Optional<MarkerSet> oMs = mApi.getMarkerSet("neincraft_plots");
                    if(oMs.isEmpty()) return;
                    MarkerSet ms = oMs.get();
                    for(BlueMapMap map : world.getMaps()){
                        String idPrefix = worldUuid.toString() + "-" + map.getId() + "-" + plotId + "-" + "plot";
                        ms.getMarkers().stream().filter(marker -> marker.getId().startsWith(idPrefix)).forEach(ms::removeMarker);
                        for(int i = 0; i < plotSectors.size(); i++) {
                            List<Vector2d> sectorCoordinates = plotSectors.get(i);
                            String id = idPrefix + "-" + i;
                            ShapeMarker sm = ms.createShapeMarker(id, map, new Shape(sectorCoordinates.toArray(new Vector2d[0])), 63f);
                            sm.setLabel(plotName + " - " + plotOwner);
                            if(adminPlot){
                                sm.setColors(new Color(0, 0, 0, 0), new Color(255, 165, 64, 166));
                            }else {
                                sm.setColors(new Color(0, 0, 0, 0), new Color(27, 170, 255, 166));
                            }
                        }
                    }
                    mApi.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

}
