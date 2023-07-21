package de.neincraft.neincraftplugin.modules.plots.bluemap;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector4i;
import com.google.common.html.HtmlEscapers;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import de.bluecolored.bluemap.api.math.Shape;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.plots.PlotModule;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import de.neincraft.neincraftplugin.modules.plots.Plot;
import de.neincraft.neincraftplugin.modules.plots.util.PlotUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;


import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BluemapIntegration {

    private static final int ORANGE_BORDER = 0xffff9a00;
    private static final int BLUE_BORDER = 0xff00a2ff;
    private static final int ORANGE_AREA = 0x96ff9a00;
    private static final int BLUE_AREA = 0x9600a2ff;

    private static final int RED_BORDER = 0xffff0000;

    private final Map<String, MarkerSet> borderMarkerSets = new ConcurrentHashMap<>();
    private final Map<String, MarkerSet> plotMarkerSets = new ConcurrentHashMap<>();
    private final Map<Long, List<Marker>> plotMarkers = new ConcurrentHashMap<>();;

    public BluemapIntegration(){
        BlueMapAPI.onEnable(api ->{
            initMarkerSets();
            AbstractModule.getInstance(PlotModule.class).ifPresent(plotModule -> {
                Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () -> {
                    for (Plot p : plotModule.getLoadedPlots()) {
                        p.setNeedsMarkerUpdate(true);
                    }
                    createBorderMarkers();
                });
            });
        });
    }

    private void initMarkerSets(){
        BlueMapAPI.getInstance().ifPresent(api ->{
            plotMarkerSets.clear();
            plotMarkers.clear();
            borderMarkerSets.clear();
            for(World world : Bukkit.getWorlds()) {
                Optional<BlueMapWorld> optionalWorld = api.getWorld(world.getName());
                if (optionalWorld.isEmpty()) continue;
                MarkerSet ms = new MarkerSet("Plots");
                MarkerSet bms = new MarkerSet("Borders");
                borderMarkerSets.put(world.getName(), bms);
                plotMarkerSets.put(world.getName(), ms);
                optionalWorld.get().getMaps().forEach(map -> {
                    map.getMarkerSets().put("nc_plots", ms);
                    map.getMarkerSets().put("nc_borders", bms);
                });
            }
        });
    }

    private void createBorderMarkers(){
        Color fillColor = new Color(0);
        Color lineColor = new Color(RED_BORDER);
        for(World w : Bukkit.getWorlds()){
            MarkerSet ms = borderMarkerSets.get(w.getName());
            if(ms == null) continue;
            ms.getMarkers().clear();
            WorldBorder wb = w.getWorldBorder();
            Vector2d center = new Vector2d(wb.getCenter().getX(), wb.getCenter().getZ());
            double radius = wb.getSize() / 2;
            List<Vector2d> cornerPoints = List.of(
                    Vector2d.from(center.getX() - radius, center.getY() - radius),
                    Vector2d.from(center.getX() + radius, center.getY() - radius),
                    Vector2d.from(center.getX() + radius, center.getY() + radius),
                    Vector2d.from(center.getX() - radius, center.getY() + radius)
            );
            Shape shape = new Shape(cornerPoints);
            ShapeMarker sm = ShapeMarker.builder()
                    .label("Weltgrenze")
                    .fillColor(fillColor)
                    .lineColor(lineColor)
                    .shape(shape, 63)
                    .centerPosition()
                    .depthTestEnabled(false)
                    .build();
            ms.getMarkers().put("border_outline", sm);
        }
    }

    public void updatePlotMarkers(Plot plot){
        long plotId = plot.getPlotData().getId();
        String plotName = plot.getPlotData().getName();
        String world = plot.getPlotData().getWorldName();
        String plotOwner = NeincraftUtils.uuidToName(plot.getPlotData().getOwner());
        boolean serverPlot = plot.isServerPlot();
        Set<Vector2i> chunks = plot.getPlotData().getChunks().stream().map(cd -> new Vector2i(cd.getId().getX(), cd.getId().getZ())).collect(Collectors.toSet());
        removePlot(plot);
        Bukkit.getScheduler().runTaskAsynchronously(NeincraftPlugin.getInstance(), () -> createMarkers(plotId, plotName, plotOwner, serverPlot, world, chunks));
    }

    private void createMarkers(long plotId, String plotName, String owner, boolean serverPlot, String world, Set<Vector2i> chunks){
        if(!plotMarkerSets.containsKey(world) || chunks.isEmpty()) return;
        int chunkCount = chunks.size();
        String htmlLabel = "<html><body><h1>" + plotName + "</h1><p>Owner: " + HtmlEscapers.htmlEscaper().escape(owner) + "</p><p>Chunks: " + chunkCount + "</p></body></html>";
        List<List<List<Vector2d>>> sectors = PlotUtils.areaToBlockPolygon(chunks);
        Color borderColor = new Color(serverPlot ? ORANGE_BORDER : BLUE_BORDER);
        Color areaColor = new Color(serverPlot ? ORANGE_AREA : BLUE_AREA);
        Color areaLineColor = new Color(0);
        List<Marker> addedMarkers = new ArrayList<>(areas.size() + borders.size());
        for(List<Vector2d> sector : sectors) {
            ShapeMarker sm = ShapeMarker.builder()
                    .label(plotName)
                    .detail(htmlLabel)
                    .lineColor(areaLineColor)
                    .fillColor(areaColor)
                    .depthTestEnabled(false)
                    .shape(new Shape(area), 63f)
                    .centerPosition()
                    .build();
            addedMarkers.add(sm);
        }
        plotMarkers.put(plotId, addedMarkers);
        int seq = 0;
        for(Marker marker : addedMarkers){
            plotMarkerSets.get(world).getMarkers().put(plotId + "_" + seq, marker);
            seq++;
        }
    }

    public void removePlot(Plot plot){
        List<Marker> markers = plotMarkers.remove(plot.getPlotData().getId());
        if(markers == null) return;
        MarkerSet ms = plotMarkerSets.get(plot.getPlotData().getWorldName());
        if(ms == null) return;

        ms.getMarkers().values().removeAll(markers);
    }

}
