package de.neincraft.neincraftplugin.util;

import com.destroystokyo.paper.ClientOption;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.hibernate.cfg.Settings;

import javax.annotation.Nullable;

public enum Lang {
    //General
    PLAYER_NOT_FOUND("&red&Spieler %player% nicht gefunden!", "&red&Player %player% not found!"),
    WRONG_EXECUTOR("&red&Du kannst diesen Command nicht ausführen!", "&red&You can't execute this command!"),
    WRONG_SYNTAX("&red&Falsche Syntax. Bitte verwende das Format:\n" +
        "[\\[](color=aqua)[/%label% ](color=gold)[%args%](color=yellow)[\\]](color=aqua)",
    "&red&Wrong syntax. Please use the following structure:\n" +
        "[\\[](color=aqua)[/%label% ](color=gold)[%args%](color=white)[\\]](color=aqua)"),
    FATAL_ERROR("&red&Ein unerwarteter Fehler ist aufgetreten! Sollte der Fehler bestehen bleiben, wende dich bitte an die Admins.",
            "&red&An unexpected error occurred! If the issue persists, please consult an administrator."),
    YES("Ja", "Yes"),
    NO("Nein", "No"),
    DEFAULT("Standard", "Default"),

    //Plots
    INVALID_WORLD("&red&In dieser Welt können keine Grundstücke erstellt werden!", "&red&Can't create plots in this world!"),
    PLOT_NOT_FOUND("&red&Grundstück nicht gefunden. Bitte überprüfe den eingegebenen Namen!", "&red&Plot not found. Please verify your provided name!"),
    ALREADY_OCCUPIED("&red&An dieser Position befindet sich bereits ein Grundstück!", "&red&There is already a plot at this location!"),
    INSUFFICIENT_PLOTS("&red&Du kannst keine weiteren Grundstücke erstellen!", "&red&You can't create any further plots!"),
    INSUFFICIENT_CHUNKS("&red&Du kannst keine weiteren Chunks zuweisen!", "&red&You can't assign any further chunks to your plots!"),
    PLOT_CREATED("&green&Grundstück erfolgreich erstellt!", "&green&Plot created successfully!"),
    CHUNK_ASSIGNED("&green&Chunk erfolgreich zugewiesen!", "&green&Chunk assigned successfully!"),
    PLOT_DELETED("&green&Grundstück gelöscht!", "&green&Plot deleted!"),
    PLOT_DELETION_CONFIRM("&red&Achtung!\n&yellow&Diese Aktion wird dein Grundstück permanent löschen!\nZum Bestätigen folgenden Command ausführen oder anklicken:\n&aqua&[%command%](run_command=%command%)",
            "&red&Caution!\n&yellow&This will irreversibly delete your plot!\nTo confirm, enter or click on the command below:\n&aqua&[%command%](run_command=%command%)"),
    CHUNK_DELETED("&green&Chunk entfernt!", "&green&Chunk deleted!"),
    PLOT_ENCLOSES_AREA("&red&Nach dieser Aktion würde dein Grundstück fremde Chunks einschließen!", "&red&By doing this, your plot would enclose other chunks!"),
    PLOT_SEPARATED("&red&Du kannst dein Grundstück nicht in mehrere Teile trennen!", "&red&You can't separate your plot into multiple parts!"),
    LAST_REMAINING_CHUNK("&red&Du kannst den letzten Chunk eines Grundstücks nicht entfernen!", "&red&You can't remove the last remaining chunk of a plot!"),
    HOME_SET("&green&Home-Punkt gesetzt!", "&green&Home location set!"),
    HOME_CHUNK_REMOVED("&red&Du kannst den chunk mit deinem Home-Punkt nicht entfernen!", "&red&You can't remove the chunk containing your home location!"),
    NO_MANAGEMENT_PERMISSION("&red&Du darfst dieses Grundstück nicht verwalten!", "&red&You are not allowed to manage this plot!"),
    PLOT_RENAMED("&green&Das Grundstück wurde erfolgreich umbenannt!", "&green&The plot was renamed successfully"),
    PLOT_NAME_INVALID("&red&Der eingegebene Name ist ungültig. Bitte verwende zwischen %min% und %max% Zeichen der folgenden Auswahl: [%chars%]", "&red&The name you entered is invalid. Please use between %min% and %max% characters of the following selection: [%chars%]"),
    PLOT_NAME_OCCUPIED("&red&Du besitzt bereits ein Grundstück mit diesem Namen! (Die Überprüfung achtet nicht auf Groß-/Kleinschreibung)", "&red&You already own a plot with this name! (Validation is not case-sensitive)"),
    PLOT_NOT_AT_LOCATION("&red&An dieser Position befindet sich kein Grundstück!", "&red&There is no plot at this location!"),
    PLOT_INFO("""
            [  =====](color=blue)[\\[](color=aqua)[%name%](color=white)[\\]](color=aqua)[=====  ](color=blue)
            [Besitzer: ](color=yellow)[%owner%](color=white)
            [Chunks: ](color=yellow)[%chunks%](color=white)
            [Deine Gruppe: ](color=yellow)[%group%](color=white)\040
            """,
    """
            [  =====](color=blue)[\\[](color=aqua)[%name%](color=white)[\\]](color=aqua)[=====  ](color=blue)
            [Owner: ](color=yellow)[%owner%](color=white)
            [Chunks: ](color=yellow)[%chunks%](color=white)
            [Your Group: ](color=yellow)[%group%](color=white)\040
            """),
    PLOT_CONFIRMATION_FAILED("&red&Der eingegebene Bestätigungscode ist falsch!", "&red&The provided confirmation token is wrong!"),
    MULTIPLE_PLOTS_SURROUNDING("&yellow&Es gibt mehrere angrenzende Grundstücke. Bitte wähle eines aus folgender Liste:\n" +
            "&gray&(Anklicken oder Namen an den vorherigen Command anfügen)", "&yellow&Multiple plots surround this chunk. Please select a plot from the following list:\n" +
            "&gray&(By clicking or appending the name to your previous command)"),
    NO_PLOTS_SURROUNDING("&red&Es wurden keine Grundstücke auf angrenzenden Chunks gefunden!", "&red&There are no plots on nearby chunks!"),
    PLOT_RELOADED("&green&Grundstücksdaten aus der Datenbank geladen. Lokale Änderungen wurden überschrieben!", "&green&Plot data loaded from database. Local changes were overwritten!"),
    PLOT_SUBDIVISION_EXISTS("&red&Ein Bereich mit diesem Namen existiert bereits!", "&red&A subdivision with this name already exists!"),
    PLOT_SUBDIVISION_CREATED("&green&Der neue Bereich wurde erfolgreich angelegt!", "&green&New subdivision created successfully!"),
    PLOT_SUBDIVISION_NOT_FOUND("&red&Es existiert kein Bereich mit dem angegebenen Namen!", "&red&A subdivision with the provided name does not exist!"),
    PLOT_SUBDIVISION_DELETED("&green&Bereich erfolgreich gelöscht!", "&green&Subdivision deleted successfully!"),
    PLOT_SUBDIVISION_PROTECTED("&red&Der Bereich ist geschützt und kann nicht gelöscht werden!", "&red&The subdivision is protected and can't be deleted!"),
    PLOT_SUBDIVISION_LIST("&yellow&Die folgenden Bereiche sind für dieses Grundstück definiert:", "&yellow&The following subdivisions are defined on this plot:"),

    //Settings
    PLOT_SETTINGS_DEFAULT_DESC("Diese Einstellung hat keine Beschreibung", "This setting does not have a description"),
    PLOT_SETTING_NOT_FOUND("&red&Diese Einstellung existiert nicht!", "&red&This setting does not exist!"),
    PLOT_SETTING_VALUE_INCORRECT("&red&Ungültiger Wert für Einstellung. Nutze \"true\", \"false\" oder \"delete\"", "&red&Invalid value for plot setting. Use \"true\", \"false\" or \"delete\""),
    PLOT_SETTING_UPDATED("&green&Einstellung gespeichert!", "&green&Plot Setting saved!"),
    PLOT_SETTINGS_LIST("""
            [Die folgenden Einstellungen sind für diesen Bereich Konfiguriert:](color=yellow)
            [(Für Erklärungen die Maus über die jeweiligen Namen halten)](color=gray)\040
            """, """
            [The following settings are configured for this subdivision:](color=yellow)
            [(Hover over the names for detailed descriptions)](color=gray)\040
            """),
    PLOT_SETTINGS_ENTRY("[%setting%](color=white hover=%description%) [=](color=gray) [%value%](color=%valueColor%) [|](color=gray) [Definiert in](color=white) [%defined%](color=yellow)",
            "[%setting%](color=white hover=%description%) [=](color=gray) [%value%](color=%valueColor%) [|](color=gray) [Defined in](color=white) [%defined%](color=yellow)"),
    PLOT_SETTING_PROTECTED("&red&Nur Admins können diese Einstellung konfigurieren!", "&red&Only administrators can configure this setting!"),
    //groups
    PLOT_GROUP_EXISTS("&red&Eine Gruppe mit diesem Namen existiert bereits!", "&red&A group with this name already exists!"),
    PLOT_GROUP_NOT_FOUND("&red&Eine gruppe mit diesem Namen existiert nicht!", "&red&A group with this name does not exist!"),
    PLOT_GROUP_CREATED("&green&Gruppe erfolgreich erstellt!", "&green&Group created successfully!"),
    PLOT_GROUP_DELETED("&green&Gruppe gelöscht. Alle Mitglieder gehören jetzt wieder \"everyone\" an!", "&green&Group deleted. All members are now part of \"everyone\""),
    PLOT_GROUP_PROTECTED("&red&Die Gruppe ist geschützt und kann nicht gelöscht werden!", "&red&The subdivision ist protected and can't be deleted!"),
    PLOT_GROUP_LIST("&yellow&Die folgenden Gruppen sind auf diesem Grundstück registriert:", "&yellow&The following groups are registered on this plot"),
    PLOT_GROUP_MEMBERS_LIST("&yellow&Folgende Spieler gehören der Gruppe %group% an:", "&yellow&The following players are part of the group %group%"),
    PLOT_GROUP_MEMBER_QUERY("&yellow&Der Spieler %player% gehört der Gruppe %group% an.", "&yellow&The player %player% is member of the group %group%."),
    PLOT_GROUP_EVERYONE_LIST("&yellow&Alle Spieler außer folgende gehören der Standardgruppe \"everyone\" an:", "&yellow&All players except the following are part of the default group \"everyone\":"),
    PLOT_PERMISSION_UPDATED("&green&Berechtigung gespeichert!", "&green&Permission saved!"),
    PLOT_PERMISSION_VALUE_INCORRECT("&red&Ungültiger Wert für Berechtigung. Nutze \"true\", \"false\" oder \"delete\"", "&red&Invalid value for plot permission. Use \"true\", \"false\" or \"delete\""),
    PLOT_PERMISSION_NOT_FOUND("&red&Es existiert keine Berechtigung mit diesem Namen!", "&red&A permission with this name does not exist!"),
    PLOT_PERMISSION_LIST("&yellow&Die folgenden Berechtigungen sind für die Gruppe %group% im Bereich %subdivision% konfiguriert:", "&yellow&The following permissions are configured for group %group% in subdivision %subdivision%:"),
    PLOT_PERMISSION_ENTRY("[%permission%](color=white) [=](color=gray) [%value%](color=%valueColor%) [|](color=gray) [Definiert in](color=white) [%group% - %subdivision%](color=yellow)", "[%permission%](color=white) [=](color=gray) [%value%](color=%valueColor%) [|](color=gray) [Defined in](color=white) [%group% - %subdivision%](color=yellow)");


    private final String german;
    private final String english;

    Lang(String german, String english){
        this.german = german;
        this.english = english;
    }

    public String getRawString(@Nullable PlayerLanguage language, @Nullable Player player){
        if(language == null) return english;
        switch(language){
            case ENGLISH -> {return english;}
            case GERMAN -> {return german;}
            case AUTO -> {
                if(player != null){
                    String locale = player.getClientOption(ClientOption.LOCALE);
                    return getRawString(PlayerLanguage.fromLocale(player.locale()), null);
                }
            }
        }
        return english;
    }

    public MineDown getMinedown(@Nullable PlayerLanguage language, @Nullable Player player){
        return new MineDown(getRawString(language, player));
    }

    public Component getComponent(@Nullable PlayerLanguage language, @Nullable Player player){
        return getMinedown(language, player).toComponent();
    }
}
