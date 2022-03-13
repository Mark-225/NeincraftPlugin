package de.neincraft.neincraftplugin.modules.plots.dto.embeddable;

import de.neincraft.neincraftplugin.modules.plots.dto.PlotData;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class SubdivisionId implements Serializable {

    @ManyToOne
    private PlotData plot;

    @Column
    private String name;

    public SubdivisionId(PlotData plot, String name) {
        this.plot = plot;
        this.name = name;
    }

    public SubdivisionId() {
    }

    public void setPlot(PlotData plot) {
        this.plot = plot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlotData getPlot() {
        return plot;
    }


}