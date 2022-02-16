package de.neincraft.neincraftplugin.modules.plots.dto.embeddable;

import org.bukkit.Chunk;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ChunkKey implements Serializable{

    private int x;
    private int z;
    private String world;

    public static ChunkKey fromChunk(Chunk chunk){
        return new ChunkKey(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
    }

    public ChunkKey(){

    }

    public ChunkKey(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkKey)) return false;

        ChunkKey chunkKey = (ChunkKey) o;

        if (x != chunkKey.x) return false;
        if (z != chunkKey.z) return false;
        return world.equals(chunkKey.world);
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + z;
        result = 31 * result + world.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ChunkKey{" +
                "x=" + x +
                ", z=" + z +
                ", world='" + world + '\'' +
                '}';
    }
}
