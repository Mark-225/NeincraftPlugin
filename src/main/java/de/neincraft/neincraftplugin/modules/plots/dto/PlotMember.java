package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.PlotMemberId;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "plot_member")
public class PlotMember {

    @EmbeddedId
    private PlotMemberId plotMemberId;

    public PlotMember(PlotMemberId plotMemberId) {
        this.plotMemberId = plotMemberId;
    }

    public PlotMember() {
    }

    public PlotMemberId getPlotMemberId() {
        return plotMemberId;
    }

    public void setPlotMemberId(PlotMemberId plotMemberId) {
        this.plotMemberId = plotMemberId;
    }
}