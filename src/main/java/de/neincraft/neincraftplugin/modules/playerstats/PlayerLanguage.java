package de.neincraft.neincraftplugin.modules.playerstats;

import java.util.Locale;

public enum PlayerLanguage {
    AUTO,
    ENGLISH,
    GERMAN;

    public static PlayerLanguage fromLocale(Locale locale){
        if(locale == null) return null;
        if(locale.getLanguage().equals("en")) return ENGLISH;
        if(locale.getLanguage().equals("de")) return GERMAN;
        return ENGLISH;
    }
}
