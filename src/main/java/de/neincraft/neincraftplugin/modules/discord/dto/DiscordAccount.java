package de.neincraft.neincraftplugin.modules.discord.dto;

import de.neincraft.neincraftplugin.modules.database.util.UUIDDataType;
import jakarta.persistence.Column;
import org.hibernate.annotations.Type;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "discord_account")
public class DiscordAccount {

    @Id
    private long discordId;

    @Column(columnDefinition = "VARCHAR(40)")
    @Type(UUIDDataType.class)
    private UUID minecraftId;

    public DiscordAccount() {
    }

    public DiscordAccount(long discordId, UUID minecraftId) {
        this.discordId = discordId;
        this.minecraftId = minecraftId;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public UUID getMinecraftId() {
        return minecraftId;
    }

    public void setMinecraftId(UUID minecraftId) {
        this.minecraftId = minecraftId;
    }
}