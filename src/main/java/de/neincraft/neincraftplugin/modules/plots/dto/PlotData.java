package de.neincraft.neincraftplugin.modules.plots.dto;

import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.LocationData;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Table(name = "plot_data")
@Entity
public class PlotData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(columnDefinition = "BIGINT")
    private long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "VARCHAR(40)")
    @Type(type = "uuid-char")
    private UUID owner;

    @OneToMany(mappedBy = "plot", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<SubdivisionData> subdivisions;

    @OneToMany(mappedBy = "groupId.plot", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<PlotMemberGroup> groups;

    @OneToMany(mappedBy = "plot", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ChunkData> chunks;


    @Column
    @Embedded
    private LocationData home;

    @Column
    private String worldName;

    public List<ChunkData> getChunks() {
        return chunks;
    }

    public List<SubdivisionData> getSubdivisions() {
        return subdivisions;
    }

    public PlotData() {
    }

    public PlotData(long id, String name, UUID owner, LocationData home, String worldName, List<ChunkData> chunks, List<SubdivisionData> subdivisions, List<PlotMemberGroup> groups) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.home = home;
        this.worldName = worldName;
        this.chunks = chunks;
        this.subdivisions = subdivisions;
        this.groups = groups;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID player) {
        this.owner = player;
    }

    public void setChunks(List<ChunkData> chunks) {
        this.chunks = chunks;
    }

    public void setSubdivisions(List<SubdivisionData> subdivisions) {
        this.subdivisions = subdivisions;
    }

    public List<PlotMemberGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<PlotMemberGroup> groups) {
        this.groups = groups;
    }

    public LocationData getHome() {
        return home;
    }

    public void setHome(LocationData home) {
        this.home = home;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
}