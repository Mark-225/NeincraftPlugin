package de.neincraft.neincraftplugin.modules.portals.dto.embeddable;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Embeddable
public class PortalCoordinates {
    private int x;
    private int y;
    private int z;

    public PortalCoordinates() {
    }

    public PortalCoordinates(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortalCoordinates)) return false;

        PortalCoordinates that = (PortalCoordinates) o;

        if (getX() != that.getX()) return false;
        if (getY() != that.getY()) return false;
        return getZ() == that.getZ();
    }

    @Override
    public int hashCode() {
        int result = getX();
        result = 31 * result + getY();
        result = 31 * result + getZ();
        return result;
    }
}