package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.GroupId;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.PlotMemberId;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Table(name = "plot_member_group")
@Entity
public class PlotMemberGroup implements Serializable {
    @EmbeddedId
    private GroupId groupId;

    @OneToMany(mappedBy = "plotMemberId.group", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<PlotMember> members;

    @OneToMany(mappedBy = "permissionId.group", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<PermissionFlag> groupPermissions;

    public PlotMemberGroup(GroupId groupId, List<PlotMember> members, List<PermissionFlag> groupPermissions) {
        this.groupId = groupId;
        this.members = members;
        this.groupPermissions = groupPermissions;
    }

    public PlotMemberGroup() {
    }

    public GroupId getGroupId() {
        return groupId;
    }

    public void setGroupId(GroupId groupId) {
        this.groupId = groupId;
    }

    public List<PlotMember> getMembers() {
        return members;
    }

    public void setMembers(List<PlotMember> members) {
        this.members = members;
    }

    public List<PermissionFlag> getGroupPermissions() {
        return groupPermissions;
    }

    public void setGroupPermissions(List<PermissionFlag> groupPermissions) {
        this.groupPermissions = groupPermissions;
    }
}