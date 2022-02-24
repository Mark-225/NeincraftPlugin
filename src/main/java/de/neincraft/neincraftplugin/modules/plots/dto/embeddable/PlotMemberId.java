package de.neincraft.neincraftplugin.modules.plots.dto.embeddable;

import de.neincraft.neincraftplugin.modules.plots.dto.PlotMemberGroup;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class PlotMemberId implements Serializable {

    @ManyToOne(optional = false)
    private PlotMemberGroup group;

    @Type(type = "uuid-char")
    private UUID uuid;

    public PlotMemberId() {
    }

    public PlotMemberId(PlotMemberGroup group, UUID uuid) {
        this.group = group;
        this.uuid = uuid;
    }

    public PlotMemberGroup getGroup() {
        return group;
    }

    public void setGroup(PlotMemberGroup group) {
        this.group = group;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}