package de.neincraft.neincraftplugin.modules.plots.util;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PlotUtils {

    public static List<List<Vector2i>> areaToPolygons(Set<Vector2i> chunks){
        List<Set<Vector2i>> sectors = findSectors(chunks);
        return sectors.stream().map(PlotUtils::traceArea).collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<Vector2i> traceArea(Set<Vector2i> chunks){
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

        //This will totally 100% definitely for sure never ever happen but just in case
        if(northSegments.size() < 1) return Collections.emptyList();

        Vector2i startVector = findNext(northSegments.keySet());
        List<Vector2i> coordinates = new ArrayList<>();
        coordinates.add(startVector);
        coordinates.addAll(traceOneLoop(segments, startVector));

        return coordinates;
    }

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
            List<Set<Vector2i>> subSectors = sector.stream().collect(Collectors.groupingBy(v -> v.getY() / 2)).values().stream().map(HashSet::new).collect(Collectors.toCollection(ArrayList::new));
            sectors.addAll(subSectors);
            chunks.removeAll(sector);
        }
        return sectors;
    }

    private static Vector2i findNext(Set<Vector2i> vectors){
        Vector2i min = null;
        for(Vector2i cur : vectors){
            if(min == null || cur.getY() < min.getY()|| (cur.getY() == min.getY() && cur.getX() < min.getX())){
                min = cur;
            }
        }
        return min;
    }

    public static List<List<Vector2d>> areaToBlockPolygon(Set<Vector2i> chunks){
        return areaToPolygons(chunks).stream().map(sector -> sector.stream().map(chunk -> chunk.mul(16).toDouble()).collect(Collectors.toCollection(ArrayList::new))).collect(Collectors.toCollection(ArrayList::new));
    }

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
