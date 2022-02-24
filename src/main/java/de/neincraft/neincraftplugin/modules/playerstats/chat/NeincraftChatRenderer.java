package de.neincraft.neincraftplugin.modules.playerstats.chat;

import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerStats;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.util.Lang;
import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class NeincraftChatRenderer implements ChatRenderer {

    private final PlayerStats playerStats;

    public NeincraftChatRenderer(PlayerStats playerStats){
        this.playerStats = playerStats;
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        String messageRaw = PlainTextComponentSerializer.plainText().serialize(message);
        String name = source.getName();
        MineDown preset;
        PlayerLanguage lang = PlayerLanguage.ENGLISH;
        if(viewer instanceof Player player){
            lang = playerStats.getOrCreate(player.getUniqueId()).getLanguage();
            preset = Lang.CHAT_TEMPLATE.getMinedown(lang, player);
        }else{
            preset = Lang.CHAT_TEMPLATE.getMinedown(PlayerLanguage.ENGLISH, null);
        }
        String label;
        PlayerData pd = playerStats.getOrCreate(source.getUniqueId());
        if (lang == PlayerLanguage.GERMAN) {
            label = pd.getGermanLabel();
        } else {
            label = pd.getEnglishLabel();
        }
        preset.replace(
                "label", label,
                "name", name
        );
        MineDown messageMineDown = new MineDown(messageRaw).filter(MineDownParser.Option.ADVANCED_FORMATTING);
        return preset.toComponent().append(messageMineDown.toComponent());
    }
}
