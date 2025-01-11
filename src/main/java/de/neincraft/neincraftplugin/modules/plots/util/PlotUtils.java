package de.neincraft.neincraftplugin.modules.plots.util;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.plots.Plot;
import de.neincraft.neincraftplugin.modules.plots.PlotModule;
import de.neincraft.neincraftplugin.modules.plots.dto.ChunkData;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class PlotUtils {

    private static ConcurrentHashMap<UUID, BukkitTask> visualizationTasks = new ConcurrentHashMap<>();

    public record Polygon(List<Vector2i> points){
        public Polygon {
            if(points == null)
                points = new ArrayList<>();
        }
        public List<Vector2d> scale(double factor){
            return points.stream().map(v2i -> v2i.toDouble().mul(factor)).toList();
        }
    }


    public record ShapeDescriptor(Polygon main, List<Polygon> holes){
        public ShapeDescriptor {
            if(main == null)
                main = new Polygon(null);
            if(holes == null)
                holes = new ArrayList<>();
        }
    }

    /**
     * Entrypoint for the polygon conversion algorithm
     * @param chunks the chunks to convert
     */
    public static List<ShapeDescriptor> areaToPolygons(Set<Vector2i> chunks) {
        List<ShapeDescriptor> shapes = new ArrayList<>();
        List<Set<Vector2i>> sectors = findSectors(chunks);
        for(Set<Vector2i> sector : sectors) {
            shapes.add(traceArea(sector));
        }
        return shapes;
    }

    /**
     * Creates border polygons and splits the area into two sectors if a hole is found. Then recursively calls itself for each sector.
     * @param chunks the sector to convert into polygons
     * @param shape the list to add the polygons to
     */
    private static ShapeDescriptor traceArea(Set<Vector2i> chunks){
        Set<Vector2i> westBorders = new HashSet<>();
        Set<Vector2i> southBorders = new HashSet<>();
        Set<Vector2i> northBorders = new HashSet<>();
        Set<Vector2i> eastBorders = new HashSet<>();

        chunks.forEach(chunk -> {
            if(!chunks.contains(chunk.add(-1, 0)))
                westBorders.add(chunk);
            if(!chunks.contains(chunk.add(1, 0)))
                eastBorders.add(chunk);
            if(!chunks.contains(chunk.add(0, 1)))
                southBorders.add(chunk);
            if(!chunks.contains(chunk.add(0, -1)))
                northBorders.add(chunk);
        });

        Map<Vector2i, Vector2i> westSegments = findSegments(westBorders, Vector2i.from(0, -1));
        Map<Vector2i, Vector2i> southSegments = findSegments(southBorders, Vector2i.from(-1, 0));
        Map<Vector2i, Vector2i> eastSegments = findSegments(eastBorders, Vector2i.from(0, 1));
        Map<Vector2i, Vector2i> northSegments = findSegments(northBorders, Vector2i.from(1, 0));

        Map<Direction, Map<Vector2i, Vector2i>> segments = Map.of(
                Direction.NORTH, new HashMap<>(northSegments),
                Direction.EAST, new HashMap<>(eastSegments),
                Direction.SOUTH, new HashMap<>(southSegments),
                Direction.WEST, new HashMap<>(westSegments));
        Polygon main = null;
        List<Polygon> holes = new ArrayList<>();
        while(!segments.get(Direction.NORTH).isEmpty()) {
            Vector2i startVector = findNext(segments.get(Direction.NORTH).keySet());
            List<Vector2i> coordinates = new ArrayList<>();
            coordinates.add(startVector);
            coordinates.addAll(traceOneLoop(segments, startVector));
            if(main == null) {
                main = new Polygon(coordinates);
            }else{
                holes.add(new Polygon(coordinates));
            }
        }
        return new ShapeDescriptor(main, holes);
    }

    /**
     * Creates a polygon by tracing border vectors until it reaches the starting vector again.
     * @param segments the border segments (represented by vector pairs) to trace
     * @param start the starting vector
     * @return A list of vectors representing the polygon
     */
    private static List<Vector2i> traceOneLoop(Map<Direction, Map<Vector2i, Vector2i>> segments, Vector2i start){
        List<Vector2i> coordinates = new ArrayList<>();
        Direction currentDirection = Direction.NORTH;
        Vector2i currentTarget;
        Vector2i currentStart = start;
        while((currentTarget = segments.get(currentDirection).get(currentStart)) != null){
            Vector2i coordinateTarget = currentTarget.add(currentDirection == Direction.NORTH || currentDirection == Direction.EAST ? 1 : 0, currentDirection == Direction.EAST || currentDirection == Direction.SOUTH ? 1 : 0);
            coordinates.add(coordinateTarget);
            segments.get(currentDirection).remove(currentStart);
            if(segments.get(currentDirection.getPrimary()).containsKey(currentTarget)){
                currentDirection = currentDirection.getPrimary();
                currentStart = currentTarget;
            }else if(segments.get(currentDirection.getSecondary()).containsKey(currentTarget.add(currentDirection.getSecondaryOffset()))){
                currentStart = currentTarget.add(currentDirection.getSecondaryOffset());
                currentDirection = currentDirection.getSecondary();
            }else{
                break;
            }
        }
        return coordinates;
    }

    /**
     * Splits a set of chunks into sectors using breadth-first search. A sector is a subset of chunks in which each chunk can be reached by every other chunk using only straight lines.
     * @param chunks The chunks to split into sectors.
     * @return A list of sectors.
     */
    private static List<Set<Vector2i>> findSectors(Set<Vector2i> chunks){
        List<Set<Vector2i>> sectors = new ArrayList<>();
        while (!chunks.isEmpty()){
            Set<Vector2i> sector = new HashSet<>();
            boolean changed = true;
            Set<Vector2i> searchSources = new HashSet<>();
            Vector2i firstChunk = chunks.iterator().next();
            sector.add(firstChunk);
            searchSources.add(firstChunk);
            while(changed){
                changed = false;
                Set<Vector2i> addedChunks = new HashSet<>();
                for(Vector2i chunk : searchSources){
                    addedChunks.addAll(Stream.of(chunk.add(0, -1),
                            chunk.add(0, 1),
                            chunk.add(1, 0),
                            chunk.add(-1, 0))
                            .filter(c -> !sector.contains(c) && chunks.contains(c)).collect(Collectors.toCollection(HashSet::new)));
                }
                if(!addedChunks.isEmpty()){
                    changed = true;
                    searchSources = addedChunks;
                    sector.addAll(addedChunks);
                }
            }
            sectors.add(sector);
            chunks.removeAll(sector);
        }
        return sectors;
    }

    /**
     * Finds the most north-west vector in a set of vectors.
     * Ensures some predictability for the order in which the algorithm operates
     * @param vectors the set of vectors
     * @return
     */
    private static Vector2i findNext(Set<Vector2i> vectors){
        Vector2i min = null;
        for(Vector2i cur : vectors){
            if(min == null || cur.getY() < min.getY()|| (cur.getY() == min.getY() && cur.getX() < min.getX())){
                min = cur;
            }
        }
        return min;
    }

    /**
     * Creates border vector pairs from a collection of border chunks
     * @param borders the collection of border chunks
     * @param forwardOffset the vector that defines the "forward" direction for the given border chunks
     * @return
     */
    private static Map<Vector2i, Vector2i> findSegments(Set<Vector2i> borders, Vector2i forwardOffset){
        Map<Vector2i, Vector2i> segments = new HashMap<>();
        while(!borders.isEmpty()){
            Vector2i start = borders.iterator().next();
            Vector2i prev;
            while(borders.contains(prev = start.sub(forwardOffset))) start = prev;

            Vector2i end = start;
            Vector2i next;
            borders.remove(start);
            while(borders.contains(next = end.add(forwardOffset))){
                end = next;
                borders.remove(next);
            }
            segments.put(start, end);
        }
        return segments;
    }

    public static void visualize(Plot plot, Player player){
        if(!player.getWorld().getName().equals(plot.getPlotData().getWorldName())) return;
        final ChunkKey playerChunk = ChunkKey.fromChunk(player.getChunk());
        final Set<Vector2i> relevantChunks = plot.getPlotData().getChunks().stream().map(ChunkData::getId).filter(chunk -> playerChunk.simpleDistance(chunk) <= 8).map(ck -> Vector2i.from(ck.getX(), ck.getZ())).collect(Collectors.toCollection(HashSet::new));
        final World world = player.getWorld();
        final int height = player.getLocation().getBlockY() + 2;
        final UUID playerUUID = player.getUniqueId();
        if(relevantChunks.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(NeincraftPlugin.getInstance(), () ->{
            CopyOnWriteArrayList<Vector3i> particleLocations = new CopyOnWriteArrayList<>(createBorderParticles(relevantChunks, Vector2i.from(playerChunk.getX(), playerChunk.getZ()), 8, height));
            if(visualizationTasks.containsKey(playerUUID) && !visualizationTasks.get(playerUUID).isCancelled()){
                try{
                    visualizationTasks.get(playerUUID).cancel();
                }catch (Exception e){
                    //
                }
            }
            visualizationTasks.remove(player.getUniqueId());
            if(particleLocations.isEmpty()) return;
            final long startTime = System.currentTimeMillis();
            final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.YELLOW, 3f);
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(NeincraftPlugin.getInstance(), () ->{
                for(Vector3i loc : particleLocations){
                    player.spawnParticle(Particle.DUST, loc.getX(), loc.getY(), loc.getZ(), 1, 0, 0, 0, 0, dustOptions);
                }
            }, 5, 20);

            visualizationTasks.put(playerUUID, task);
            Bukkit.getScheduler().runTaskLaterAsynchronously(NeincraftPlugin.getInstance(), () ->{
                if(!task.isCancelled()){
                    try{
                        task.cancel();
                    }catch(Exception e){
                        //
                    }
                }
            }, 120);
        });
    }

    private static List<Vector3i> createBorderParticles(Set<Vector2i> chunks, Vector2i center, int radius, int height){
        Set<Vector2i> displayedChunks = chunks.stream().filter(chunk -> Math.max(Math.abs(chunk.getX() - center.getX()), Math.abs(chunk.getY() - center.getY())) < radius).collect(Collectors.toCollection(HashSet::new));
        List<Vector3i> borderPreset = IntStream.range(0, 8).boxed().flatMap(integer -> Stream.of(new Vector3i(integer*2, height, 0), new Vector3i(integer*2+1, height+2, 0))).toList();
        List<Vector3i> vectors = new ArrayList<>();
        for(Vector2i chunk : displayedChunks){
            List<Vector3i> thisChunk = new ArrayList<>();
            if(!chunks.contains(chunk.add(0, -1))){
                thisChunk.addAll(borderPreset);
            }

            if(!chunks.contains(chunk.add(-1, 0))){
                thisChunk.addAll(borderPreset.stream().map(v -> Vector3i.from(0, v.getY(), 16 - v.getX())).toList());
            }

            if(!chunks.contains(chunk.add(0, 1))){
                thisChunk.addAll(borderPreset.stream().map(v -> Vector3i.from(16 - v.getX(), v.getY(), 16)).toList());
            }

            if(!chunks.contains(chunk.add(1, 0))){
                thisChunk.addAll(borderPreset.stream().map(v -> Vector3i.from(16, v.getY(), v.getX())).toList());
            }
            Vector3i chunkVector = Vector3i.from(chunk.getX() * 16, 0, chunk.getY() * 16);
            vectors.addAll(thisChunk.stream().map(v -> v.add(chunkVector)).toList());
        }

        return vectors;
    }

    public static boolean canPropagate(PlotModule plotModule, ChunkKey from, ChunkKey to){
        if(from == to) return true;
        return canPropagate(plotModule.getPlotAtChunk(from).orElse(null), plotModule.getPlotAtChunk(to).orElse(null));
    }

    public static boolean canPropagate(Plot from, Plot to){
        if(from == to || to == null) return true;
        if(from == null) return false;
        return Objects.equals(from.getPlotData().getOwner(), to.getPlotData().getOwner());
    }

    public static enum Direction{
        NORTH(1, 3, Vector2i.from(1, -1)),
        EAST(2, 0, Vector2i.from(1, 1)),
        SOUTH(3, 1, Vector2i.from(-1, 1)),
        WEST(0, 2, Vector2i.from(-1, -1));

        private final int primary;
        private final int secondary;
        private final Vector2i secondaryOffset;
        Direction(int primary, int secondary, Vector2i secondaryOffset) {
            this.primary = primary;
            this.secondary = secondary;
            this.secondaryOffset = secondaryOffset;
        }

        public Direction getPrimary(){
            return Direction.values()[primary];
        }

        public Direction getSecondary(){
            return Direction.values()[secondary];
        }

        public Vector2i getSecondaryOffset() {
            return secondaryOffset;
        }
    }
}
