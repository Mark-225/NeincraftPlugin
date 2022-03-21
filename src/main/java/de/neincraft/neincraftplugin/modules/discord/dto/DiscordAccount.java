package de.neincraft.neincraftplugin.modules.discord.dto;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "discord_account")
public class DiscordAccount {

    @Id
    private long discordId;

    @Column(columnDefinition = "VARCHAR(40)")
    @Type(type = "uuid-char")
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