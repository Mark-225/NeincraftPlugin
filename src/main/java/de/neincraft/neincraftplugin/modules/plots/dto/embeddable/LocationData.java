package de.neincraft.neincraftplugin.modules.plots.dto.embeddable;


import org.bukkit.Location;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class LocationData implements Serializable{
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public LocationData() {
    }

    public LocationData(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public static LocationData fromBukkitlocation(Location loc){
        return new LocationData(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationData)) return false;

        LocationData that = (LocationData) o;

        if (Double.compare(that.getX(), getX()) != 0) return false;
        if (Double.compare(that.getY(), getY()) != 0) return false;
        if (Double.compare(that.getZ(), getZ()) != 0) return false;
        if (Float.compare(that.getPitch(), getPitch()) != 0) return false;
        return Float.compare(that.getYaw(), getYaw()) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getX());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getZ());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getPitch() != +0.0f ? Float.floatToIntBits(getPitch()) : 0);
        result = 31 * result + (getYaw() != +0.0f ? Float.floatToIntBits(getYaw()) : 0);
        return result;
    }
}
