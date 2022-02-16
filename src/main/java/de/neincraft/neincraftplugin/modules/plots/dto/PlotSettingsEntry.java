package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.SettingId;
import de.neincraft.neincraftplugin.modules.plots.util.PlotSetting;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Table(name = "plot_settings_entry")
@Entity
public class PlotSettingsEntry implements Serializable {

    @EmbeddedId
    private SettingId settingId;

    @Column
    private boolean value;

    public PlotSettingsEntry() {
    }

    public PlotSettingsEntry(SettingId settingId, boolean value) {
        this.settingId = settingId;
        this.value = value;
    }

    public SettingId getSettingId() {
        return settingId;
    }

    public void setSettingId(SettingId settingId) {
        this.settingId = settingId;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public static List<PlotSettingsEntry> createDefaults(SubdivisionData subdivision){
        return Arrays.stream(PlotSetting.values()).map(setting -> new PlotSettingsEntry(new SettingId(subdivision, setting), setting.getDefaultValue())).collect(Collectors.toCollection(ArrayList::new));
    }
}