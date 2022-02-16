package de.neincraft.neincraftplugin.modules.plots.dto.embeddable;

import de.neincraft.neincraftplugin.modules.plots.dto.PlotData;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class GroupId implements Serializable{

    @ManyToOne(optional = false)
    private PlotData plot;

    private String groupName;

    public GroupId(PlotData plot, String groupName) {
        this.plot = plot;
        this.groupName = groupName;
    }


    public GroupId() {

    }

    public PlotData getPlot() {
        return plot;
    }

    public void setPlot(PlotData plot) {
        this.plot = plot;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}