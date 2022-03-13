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
    LIST_EMPTY("&red&Keine Einträge gefunden", "&red&No entries found"),
    INVENTORY_FULL("&red&Du hast keinen freien Platz im Inventar!", "&red&Your Inventory is full!"),
    UNKNOWN_COMMAND("&red&Unbekannter Befehl", "&red&Unknown Command"),

    //stats
    FIRST_JOIN("&green&Willkommen zu deinem ersten Besuch auf Neincraft, [%player%](color=yellow)!", "&green&Welcome to your first visit on Neincraft, [%player%](color=yellow)!"),
    CHAT_TEMPLATE("%label% [%name%](white) [>>](gray) ", "%label% [%name%](white) [>>](gray) "),
    MSG_TEMPLATE("[%name%](white) [->](gray) [%target%](white) [>>](gray) ", "[%name%](white) [->](gray) [%target%](white) [>>](gray) "),
    PLAYER_AFK("&yellow&[AFK] %player% ist nun abwesend", "&yellow&[AFK] %player% is now afk"),
    PLAYER_AFK_RND_1("&yellow&[AFK] Hat jemand %player% gesehen?", "&yellow&[AFK] Has somebody seen %player%?"),
    PLAYER_AFK_RND_2("&yellow&[AFK] %player% hat keine Lust mehr", "&yellow&[AFK] %player% doesn't feel like playing anymore"),
    PLAYER_AFK_RND_3("&yellow&[AFK] %player% ignoriert uns", "&yellow&[AFK] %player% is ignoring us"),
    PLAYER_AFK_RND_4("&yellow&[AFK] %player% braucht eine Pause", "&yellow&[AFK] %player% needs a break"),
    PLAYER_AFK_RND_5("&yellow&[AFK] Psst... %player% ist eingeschlafen", "&yellow&[AFK] Shh... %player% is taking a nap"),
    PLAYER_AFK_RETURN("&yellow&[AFK] %player% ist wieder da", "&yellow&[AFK] %player% is back"),
    PLAYER_STATS("""
            &yellow&Statistiken für [%player%](white):
            
            [Aktive Spielzeit:](white) [%playtime%](gold)
            
            [Grundstücke:](white) [%plots%](gold) [(%pointplots%+%bonusplots%)](gray)
            [Davon frei:](white) [%freeplots%](gold)
            [Fortschritt:](white) %plotprogress% [(%plotpoints%/%requiredplot%)](gray)
            
            [Chunks:](white) [%chunks%](gold) [(%pointchunks%+%bonuschunks%)](gray)
            [Davon frei:](white) [%freechunks%](gold)
            [Fortschritt:](white) %chunkprogress% [(%chunkpoints%/%requiredchunk%)](gray)
            """,
            """
            &yellow&Statistics for [%player%](white):
            
            [Active playtime:](white) [%playtime%](gold)
            
            [Plots:](white) [%plots%](gold) [(%pointplots%+%bonusplots%)](gray)
            [Free:](white) [%freeplots%](gold)
            [Progress:](white) %plotprogress% [(%plotpoints%/%requiredplot%)](gray)
            
            [Chunks:](white) [%chunks%](gold) [(%pointchunks%+%bonuschunks%)](gray)
            [Free:](white) [%freechunks%](gold)
            [Progress:](white) %chunkprogress% [(%chunkpoints%/%requiredchunk%)](gray)
            """),
    LABEL_UPDATED("[Label aktualisiert!](green)\n%german%\n%english%", "[Label updated!](green)\n%german%\n%english%"),

    //Plots
    INVALID_WORLD("&red&In dieser Welt können keine Grundstücke erstellt werden!", "&red&Can't create plots in this world!"),
    PLOT_LIST("&yellow&Grundstücksliste [%player%](white):", "&yellow&Plot list [%player%](white):"),
    PLOT_LIST_OWNER("\n&yellow&Besitzt:", "\n&yellow&Owns:"),
    PLOT_LIST_MEMBER("\n&yellow&Ist einer Gruppe zugewiesen:", "\n&yellow&Is member of a group:"),
    PLOT_LIST_HOME("\n&yellow&Hat Teleportrechte:", "\n&yellow&Has teleportation permissions:"),
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
            [  =====](color=blue)[\\[](color=aqua)[%name% \\[%subdivision%\\]](color=white)[\\]](color=aqua)[=====  ](color=blue)
            [Besitzer: ](color=yellow)[%owner%](color=white)
            [Chunks: ](color=yellow)[%chunks%](color=white)
            [Deine Gruppe: ](color=yellow)[%group%](color=white)\040
            """,
    """
            [  =====](color=blue)[\\[](color=aqua)[%name% \\[%subdivision%\\]](color=white)[\\]](color=aqua)[=====  ](color=blue)
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
    PLOT_SETTINGS_ENTRY("[%setting%](color=white show_text=%description%) [=](color=gray) [%value%](color=%valueColor%) [|](color=gray) [Definiert in](color=white) [%defined%](color=yellow)",
            "[%setting%](color=white hover=%description%) [=](color=gray) [%value%](color=%valueColor%) [|](color=gray) [Defined in](color=white) [%defined%](color=yellow)"),
    PLOT_SETTING_PROTECTED("&red&Nur Admins können diese Einstellung konfigurieren!", "&red&Only administrators can configure this setting!"),
    //groups
    PLOT_GROUP_EXISTS("&red&Eine Gruppe mit diesem Namen existiert bereits!", "&red&A group with this name already exists!"),
    PLOT_GROUP_NOT_FOUND("&red&Eine gruppe mit diesem Namen existiert nicht!", "&red&A group with this name does not exist!"),
    PLOT_GROUP_CREATED("&green&Gruppe erfolgreich erstellt!", "&green&Group created successfully!"),
    PLOT_GROUP_DELETED("&green&Gruppe gelöscht. Alle Mitglieder gehören jetzt wieder \"everyone\" an!", "&green&Group deleted. All members are now part of \"everyone\""),
    PLOT_GROUP_PROTECTED("&red&Die Gruppe ist geschützt und kann nicht gelöscht werden!", "&red&The subdivision ist protected and can't be deleted!"),
    PLOT_GROUP_LIST("&yellow&Die folgenden Gruppen sind auf diesem Grundstück registriert:", "&yellow&The following groups are registered on this plot"),
    PLOT_GROUP_MEMBERS_LIST("&yellow&Folgende Spieler gehören der Gruppe [%group%](white) an:", "&yellow&The following players are part of the group [%group%](white):"),
    PLOT_GROUP_MEMBER_QUERY("&yellow&Der Spieler [%player%](white) gehört der Gruppe [%group%](white) an.", "&yellow&The player [%player%](white) is member of the group [%group%](white)."),
    PLOT_GROUP_MEMBER_ADDED("&yellow&Der Spieler [%player%](white) ist jetzt Mitglied der Gruppe [%group%](white)!", "&yellow&Player [%player%](white) is now member of [%group%](white)!" ),
    PLOT_GROUP_EVERYONE_LIST("&yellow&Alle Spieler außer folgende gehören der Standardgruppe \"everyone\" an:", "&yellow&All players except the following are part of the default group \"everyone\":"),
    PLOT_PERMISSION_UPDATED("&green&Berechtigung für den Bereich an deiner Position gespeichert!", "&green&Permission saved for the subdivision at your location!"),
    PLOT_PERMISSION_VALUE_INCORRECT("&red&Ungültiger Wert für Berechtigung. Nutze \"true\", \"false\" oder \"delete\"", "&red&Invalid value for plot permission. Use \"true\", \"false\" or \"delete\""),
    PLOT_PERMISSION_NOT_FOUND("&red&Es existiert keine Berechtigung mit diesem Namen!", "&red&A permission with this name does not exist!"),
    PLOT_PERMISSION_LIST("&yellow&Die folgenden Berechtigungen sind für die Gruppe %group% im Bereich %subdivision% konfiguriert:", "&yellow&The following permissions are configured for group %group% in subdivision %subdivision%:"),
    PLOT_PERMISSION_ENTRY("[%permission%](color=white) [=](color=gray) [%value%](color=%valueColor%) [|](color=gray) [Definiert in](color=white) [%group% - %subdivision%](color=yellow)", "[%permission%](color=white) [=](color=gray) [%value%](color=%valueColor%) [|](color=gray) [Defined in](color=white) [%group% - %subdivision%](color=yellow)"),

    //protection
    PLOT_ENTER("&yellow&Du betrittst das Grundstück [%plot% \\[%subdivision%\\]](color=white) von [%owner%](color=white)", "&yellow&You are entering the plot [%plot% \\[%subdivision%\\]](color=white) by [%owner%](color=white)"),
    PLOT_LEAVE("&green&Du bist nun auf freiem Gebiet", "&green&You are now on unclaimed land"),
    PLOT_CANT_ENTER("&red&Du darfst diesen Bereich nicht betreten!", "&red&You are not allowed to enter this area!"),
    PLOT_CANT_MODIFY("&red&Das darfst du hier nicht tun!", "&red&You are not allowed to do that here!"),
    PLOT_PVP_ACTIVE("&red&PVP AKTIV", "&red&PVP ACTIVE"),
    PLOT_CANT_TELEPORT("&red&Du darfst dich nicht auf dieses Grundstück teleportieren!", "&red&You are not allowed to teleport to this plot!"),

    //portals
    PORTAL_ALREADY_EXISTS("&reed&Ein Portal mit diesem Namen existiert bereits!", "&red&A portal with this name already exists"),
    PORTAL_CREATED("&green&Portal erstellt! Lege Bereich und Zielpunkt mit [/portal edit %portal%](color=yellow run_command=/portal edit %portal% show_text=Klicken zum Ausführen)", "&green&Portal created! Define an area and target using [/portal edit %portal%](color=yellow run_command=/portal edit %portal% show_text=Click to execute)"),
    PORTAL_DELETED("&green&Portal gelöscht.", "&green&Portal deleted"),
    PORTAL_BEGIN_EDIT("&dark_purple&Portal-Tool für [%portal%](white): Zwei Blöcke linksklicken zum Definieren des Portalbereichs, rechtsklicken zum Setzen des Zielpunktes auf deine aktuelle position.", "&dark_purple&Portal tool for [%portal%](white): Left-click two blocks to define the area, right-click to set your current location as the portal's destination."),
    PORTAL_NOT_FOUND("&red&Ein portal mit diesem Namen existiert nicht.", "&red&A portal with this name does not exist."),
    PORTAL_FIRST_POS_SET("&yellow&Erste Position gesetzt.", "&yellow&First position set"),
    PORTAL_AREA_DEFINED("&green&Zwei positionen gesetzt. Der Portalbereich ist jetzt vollständig definiert. Linksklicke erneut, um den Bereich neu zu definieren.", "&green&Two positions set. The portal area is now defined completely. Left-click again to redefine the area."),
    PORTAL_DEST_SET("&green&Portal-Ziel auf aktuelle Position gesetzt.", "&green&Portal destination set to current location!"),

    //lifts
    LIFT_ENTER("&yellow&Du hast einen Aufzug betreten. Hoch: Springen, Runter: Schleichen", "&yellow&You entered a lift. Up: Jump, Down: Sneak"),

    //timber
    TIMBER_ACTIVE("&green&Timber Modus aktiviert!", "&green&Timber mode activated!"),
    TIMBER_INACTIVE("&green&Timber Modus deaktiviert!", "&green&Timber mode deactivated!"),
    TIMBER_WARNING("&red&Timber Modus ist aktiv!", "&red&Timber mode is active!"),

    //BlockEntityProperties
    BE_HEADER("Blockeigenschaften", "Block properties"),
    BE_ACTIVATE("&green&Aktivieren", "&green&Activate"),
    BE_DEACTIVATE("&red&Deaktivieren", "&red&Deactivate"),
    BE_NAME_INFDISP("Unendlicher Dispenser", "Infinite dispenser"),
    BE_DESC_INFDISP("Der Dispenser/Dropper verbraucht keine Items", "The dispenser/dropper does not consume items"),
    BE_NAME_PUBLIC("Öffentlicher Container", "Public container"),
    BE_DESC_PUBLIC("Deaktiviert den Schutz vor fremden Spielern.","Deactivates protection from other players."),

    //storage
    STORAGE_CAPTION("Storage", "Storage"),
    STORAGE_INFO_ITEM("Informationen:", "Information:"),
    STORAGE_CAPACITY("&yellow&Kapazität: [%used%](gold)/[%total%](gold)", "&yellow&Capacity: [%used%](gold)/[%total%](gold)"),
    STORAGE_PAGE("&yellow& Seite: [%current%](gold)/[%total%](gold)", "&yellow& Page: [%current%](gold)/[%total%](gold)"),
    STORAGE_NEXT_PAGE("Nächste Seite", "Next page"),
    STORAGE_PREV_PAGE("Vorherige Seite", "Previous page"),
    STORAGE_ITEM_AMOUNT_PREFIX("&light_purple&Anzahl in Storage:", "&light_purple&Amount in storage:"),
    STORAGE_LOADING("&yellow&Storage wird initialisiert... Dieser Vorgang könnte einen Moment dauern.", "&yellow&Storage is being initialized... This process could take a moment."),

    //teleport
    TP_REQUEST_SENT("&green&Anfrage gesendet!", "&green&Request Sent!"),
    NO_OPEN_REQUESTS("&red&Keine offenen Anfragen vorhanden!", "&red&No open requests available!"),
    TP_REQUEST_COOLDOWN("&red&Bitte warte, bis deine vorherige Anfrage abgelaufen ist!", "&red&Please wait until your previous request expired!"),
    TP_REQUEST_ACCEPTED("&green&Anfrage akzeptiert!", "&green&Request accepted!"),
    TPA_REQUEST_RECEIVED("&yellow& [%player%](white) möchte sich zu dir teleportieren!\nAnnehmen mit [\\[/tpaccept %player%\\]](color= gold run_command=/tpaccept %player%)",
            "&yellow& [%player%](white) wants to teleport to you\nAccept using [\\[/tpaccept %player%\\]](color= gold run_command=/tpaccept %player%)"),
    TPAHERE_REQUEST_RECEIVED("&yellow& [%player%](white) dich zu sich teleportieren!\nAnnehmen mit [\\[/tpaccept %player%\\]](color= gold run_command=/tpaccept %player%)",
            "&yellow& [%player%](white) wants yout to teleport to them!\nAccept using [\\[/tpaccept %player%\\]](color= gold run_command=/tpaccept %player%)");


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
        return new MineDown(getRawString(language, player)).replaceFirst(true);
    }

    public Component getComponent(@Nullable PlayerLanguage language, @Nullable Player player){
        return getMinedown(language, player).toComponent();
    }

    public String getRawString(@Nullable Player player){
        return getRawString(NeincraftUtils.getPlayerLanguage(player), player);
    }

    public MineDown getMinedown(@Nullable Player player){
        return getMinedown(NeincraftUtils.getPlayerLanguage(player), player);
    }

    public Component getComponent(@Nullable Player player){
        return getComponent(NeincraftUtils.getPlayerLanguage(player), player);
    }

}
