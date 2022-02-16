package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.PermissionId;
import de.neincraft.neincraftplugin.modules.plots.util.PermissionValue;
import de.neincraft.neincraftplugin.modules.plots.util.PlotPermission;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Table(name = "permission_flag")
@Entity
public class PermissionFlag implements Serializable {

    public static List<PermissionFlag> getMemberDefaults(PlotMemberGroup group, SubdivisionData subdivision){
        return Arrays.stream(PlotPermission.values()).map(perm -> new PermissionFlag(new PermissionId(group, subdivision, perm), perm.getMemberDefault())).collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<PermissionFlag> getOwnerDefaults(PlotMemberGroup group, SubdivisionData subdivision){
        return Arrays.stream(PlotPermission.values()).map(perm -> new PermissionFlag(new PermissionId(group, subdivision, perm), perm.getOwnerDefault())).collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<PermissionFlag> getEveryoneDefault(PlotMemberGroup group, SubdivisionData subdivision){
        return Arrays.stream(PlotPermission.values()).map(perm -> new PermissionFlag(new PermissionId(group, subdivision, perm), perm.getPublicDefault())).collect(Collectors.toCollection(ArrayList::new));
    }

    @EmbeddedId
    private PermissionId permissionId;

    @Enumerated(value = EnumType.STRING)
    private PermissionValue value;

    public PermissionFlag() {
    }

    public PermissionFlag(PermissionId permissionId, PermissionValue value) {
        this.permissionId = permissionId;
        this.value = value;
    }

    public PermissionId getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(PermissionId permissionId) {
        this.permissionId = permissionId;
    }

    public PermissionValue getValue() {
        return value;
    }

    public void setValue(PermissionValue value) {
        this.value = value;
    }
}