package de.neincraft.neincraftplugin.modules.plots.dto.embeddable;

import de.neincraft.neincraftplugin.modules.plots.dto.PlotMemberGroup;
import de.neincraft.neincraftplugin.modules.plots.dto.SubdivisionData;
import de.neincraft.neincraftplugin.modules.plots.util.PlotPermission;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class PermissionId implements Serializable {

    @ManyToOne(optional = false)
    private PlotMemberGroup group;

    @ManyToOne(optional = false)
    private SubdivisionData subdivision;

    @Enumerated(value = EnumType.STRING)
    private PlotPermission permissionKey;

    public PermissionId(PlotMemberGroup group, SubdivisionData subdivision, PlotPermission permissionKey) {
        this.group = group;
        this.subdivision = subdivision;
        this.permissionKey = permissionKey;
    }

    public PermissionId() {
    }

    public PlotMemberGroup getGroup() {
        return group;
    }

    public void setGroup(PlotMemberGroup group) {
        this.group = group;
    }

    public SubdivisionData getSubdivision() {
        return subdivision;
    }

    public void setSubdivision(SubdivisionData subdivision) {
        this.subdivision = subdivision;
    }

    public PlotPermission getPermissionKey() {
        return permissionKey;
    }

    public void setPermissionKey(PlotPermission permissionKey) {
        this.permissionKey = permissionKey;
    }
}