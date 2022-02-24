package de.neincraft.neincraftplugin.modules.plots.dto;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Table(name = "subdivision_data")
@Entity
public class SubdivisionData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BIGINT")
    private long subdivisionId;

    @ManyToOne(fetch = FetchType.EAGER)
    private PlotData plot;

    @OneToMany(mappedBy = "settingId.subdivision", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<PlotSettingsEntry> settings;

    @Column
    private String name;

    public SubdivisionData() {
    }

    public SubdivisionData(PlotData plot, List<PlotSettingsEntry> settings, String name) {
        this.plot = plot;
        this.settings = settings;
        this.name = name;
    }

    public long getSubdivisionId() {
        return subdivisionId;
    }

    public PlotData getPlot() {
        return plot;
    }

    public void setPlot(PlotData plot) {
        this.plot = plot;
    }

    public List<PlotSettingsEntry> getSettings() {
        return settings;
    }

    public void setSettings(List<PlotSettingsEntry> settings) {
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}