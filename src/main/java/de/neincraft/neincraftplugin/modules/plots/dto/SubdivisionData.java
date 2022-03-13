package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.SubdivisionId;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Table(name = "subdivision_data")
@Entity
public class SubdivisionData implements Serializable {

    @EmbeddedId
    private SubdivisionId subdivisionId;

    @OneToMany(mappedBy = "settingId.subdivision", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<PlotSettingsEntry> settings;

    public SubdivisionData() {
    }

    public SubdivisionData(SubdivisionId subdivisionId, List<PlotSettingsEntry> settings) {
        this.subdivisionId = subdivisionId;
        this.settings = settings;
    }

    public SubdivisionId getSubdivisionId() {
        return subdivisionId;
    }

    public List<PlotSettingsEntry> getSettings() {
        return settings;
    }

    public void setSettings(List<PlotSettingsEntry> settings) {
        this.settings = settings;
    }
}