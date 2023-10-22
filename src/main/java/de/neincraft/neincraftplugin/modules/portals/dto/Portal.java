package de.neincraft.neincraftplugin.modules.portals.dto;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.LocationData;
import de.neincraft.neincraftplugin.modules.portals.dto.embeddable.PortalCoordinates;

import jakarta.persistence.*;

@Entity
@Table(name = "portal")
public class Portal {

    @Id
    private String name;

    @Column(nullable = true)
    private String world;

    @Column(nullable = true)
    private String targetWorld;

    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name="min_x")),
            @AttributeOverride(name = "y", column = @Column(name="min_y")),
            @AttributeOverride(name = "z", column = @Column(name="min_z"))
    })
    @Embedded
    private PortalCoordinates minCorner;

    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name="max_x")),
            @AttributeOverride(name = "y", column = @Column(name="max_y")),
            @AttributeOverride(name = "z", column = @Column(name="max_z"))
    })
    @Embedded
    private PortalCoordinates maxCorner;

    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name="target_x")),
            @AttributeOverride(name = "y", column = @Column(name="target_y")),
            @AttributeOverride(name = "z", column = @Column(name="target_z")),
            @AttributeOverride(name = "yaw", column = @Column(name="target_yaw")),
            @AttributeOverride(name = "pitch", column = @Column(name="target_pitch"))
    })
    @Embedded
    private LocationData target;

    public Portal() {
    }

    public Portal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PortalCoordinates getMinCorner() {
        return minCorner;
    }

    public void setMinCorner(PortalCoordinates minCorner) {
        this.minCorner = minCorner;
    }

    public PortalCoordinates getMaxCorner() {
        return maxCorner;
    }

    public void setMaxCorner(PortalCoordinates maxCorner) {
        this.maxCorner = maxCorner;
    }

    public LocationData getTarget() {
        return target;
    }

    public void setTarget(LocationData target) {
        this.target = target;
    }

    public Vector3d getCenterPoint(){
        return Vector3d.from(minCorner.getX() + (double) (maxCorner.getX() - minCorner.getX()) / 2d,
                minCorner.getY() + (double) (maxCorner.getY() - minCorner.getY()) / 2d,
                minCorner.getZ() + (double) (maxCorner.getZ() - minCorner.getZ()) / 2d);
    }

    public long getVolume(){
        return (long) (maxCorner.getX() - minCorner.getX()) * (maxCorner.getY() - minCorner.getY()) * (maxCorner.getZ() - minCorner.getZ());
    }

    public boolean isInside(Vector3i position){
        if(!isAreaDefined()) return false;
        return position.getX() >= minCorner.getX() && position.getY() >= minCorner.getY() && position.getZ() >= minCorner.getZ() &&
                position.getX() <= maxCorner.getX() && position.getY() <= maxCorner.getY() && position.getZ() <= maxCorner.getZ();
    }

    public boolean isAreaDefined(){
        return minCorner != null && maxCorner != null && world != null;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getTargetWorld() {
        return targetWorld;
    }

    public void setTargetWorld(String targetWorld) {
        this.targetWorld = targetWorld;
    }
}