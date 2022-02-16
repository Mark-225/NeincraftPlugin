package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.GroupId;
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

    @ElementCollection()
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<UUID> members;

    @OneToMany(mappedBy = "permissionId.group", cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<PermissionFlag> groupPermissions;

    public PlotMemberGroup(GroupId groupId, List<UUID> members, List<PermissionFlag> groupPermissions) {
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

    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }

    public List<PermissionFlag> getGroupPermissions() {
        return groupPermissions;
    }

    public void setGroupPermissions(List<PermissionFlag> groupPermissions) {
        this.groupPermissions = groupPermissions;
    }
}