package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import org.hibernate.annotations.Cascade;
import org.hibernate.engine.spi.CascadeStyle;

import jakarta.persistence.*;
import java.io.Serializable;

@Table(name = "chunk_data")
@Entity
public class ChunkData implements Serializable {

    @EmbeddedId
    private ChunkKey id;

    @ManyToOne(fetch = FetchType.EAGER)
    private PlotData plot;

    @ManyToOne(fetch = FetchType.EAGER)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.PERSIST})
    private SubdivisionData subdivision;

    public ChunkData() {
    }

    public ChunkData(ChunkKey id, PlotData plot, SubdivisionData subdivision) {
        this.id = id;
        this.plot = plot;
        this.subdivision = subdivision;
    }

    public ChunkKey getId() {
        return id;
    }

    public void setId(ChunkKey id) {
        this.id = id;
    }

    public PlotData getPlot() {
        return plot;
    }

    public void setPlot(PlotData plot) {
        this.plot = plot;
    }

    public SubdivisionData getSubdivision() {
        return subdivision;
    }

    public void setSubdivision(SubdivisionData subdivision) {
        this.subdivision = subdivision;
    }
}