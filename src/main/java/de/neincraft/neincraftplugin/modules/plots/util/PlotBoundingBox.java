package de.neincraft.neincraftplugin.modules.plots.util;

public class PlotBoundingBox {

    int minX;
    int maxX;
    int minZ;
    int maxZ;

    public PlotBoundingBox(int minX, int maxX, int minZ, int maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public boolean isInside(int x, int z){
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
