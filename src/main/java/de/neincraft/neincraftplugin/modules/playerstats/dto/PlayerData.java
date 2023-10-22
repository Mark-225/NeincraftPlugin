package de.neincraft.neincraftplugin.modules.playerstats.dto;

import de.neincraft.neincraftplugin.modules.database.util.UUIDDataType;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.util.UUID;

@Table(name = "player")
@Entity
public class PlayerData {
    @Id
    @Column(name = "uuid", nullable = false, columnDefinition = "VARCHAR(40)")
    @Type(UUIDDataType.class)
    private UUID uuid;

    @Column
    private long points = 0;

    @Column
    private int bonusPlots = 0;

    @Column
    private int bonusChunks = 0;

    @Column
    private long secondsPlayed = 0;

    @Column
    private String germanLabel = "[\\[](dark_aqua)[Spieler](aqua)[\\]](dark_aqua)";

    @Column
    private String englishLabel = "[\\[](dark_aqua)[Player](aqua)[\\]](dark_aqua)";

    @Column
    @Enumerated(EnumType.STRING)
    private PlayerLanguage language = PlayerLanguage.AUTO;

    @Transient
    private long expiryTimestamp;

    public PlayerData(UUID uuid){
        this.uuid = uuid;
    }

    protected PlayerData() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public int getBonusPlots() {
        return bonusPlots;
    }

    public void setBonusPlots(int bonusPlots) {
        this.bonusPlots = bonusPlots;
    }

    public int getBonusChunks() {
        return bonusChunks;
    }

    public void setBonusChunks(int bonusChunks) {
        this.bonusChunks = bonusChunks;
    }

    public PlayerLanguage getLanguage() {
        return language;
    }

    public void setLanguage(PlayerLanguage language) {
        this.language = language;
    }

    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }

    public long getSecondsPlayed() {
        return secondsPlayed;
    }

    public void setSecondsPlayed(long secondsPlayed) {
        this.secondsPlayed = secondsPlayed;
    }

    public String getGermanLabel() {
        return germanLabel;
    }

    public void setGermanLabel(String germanLabel) {
        this.germanLabel = germanLabel;
    }

    public String getEnglishLabel() {
        return englishLabel;
    }

    public void setEnglishLabel(String englishLabel) {
        this.englishLabel = englishLabel;
    }
}