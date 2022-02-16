package de.neincraft.neincraftplugin.modules.plots.dto.embeddable;

import de.neincraft.neincraftplugin.modules.plots.dto.SubdivisionData;
import de.neincraft.neincraftplugin.modules.plots.util.PlotSetting;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class SettingId implements Serializable {
    @ManyToOne
    private SubdivisionData subdivision;

    @Column
    @Enumerated(value = EnumType.STRING)
    PlotSetting setting;

    public SettingId() {
    }

    public SettingId(SubdivisionData subdivision, PlotSetting setting) {
        this.subdivision = subdivision;
        this.setting = setting;
    }

    public SubdivisionData getSubdivision() {
        return subdivision;
    }

    public void setSubdivision(SubdivisionData subdivision) {
        this.subdivision = subdivision;
    }

    public PlotSetting getSetting() {
        return setting;
    }

    public void setSetting(PlotSetting setting) {
        this.setting = setting;
    }
}