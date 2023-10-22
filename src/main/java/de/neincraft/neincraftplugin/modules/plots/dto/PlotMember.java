package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.PlotMemberId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "plot_member")
public class PlotMember implements Serializable {

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