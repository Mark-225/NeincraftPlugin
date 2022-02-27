package de.neincraft.neincraftplugin.modules.misc;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.modules.misc.commands.TestLang;

@NeincraftModule(id = "MiscModule")
public class MiscModule extends AbstractModule {

    @InjectCommand("testlang")
    private TestLang testLangCommand;

    @Override
    protected boolean initModule() {
        return true;
    }

    @Override
    public void unload() {

    }
}
