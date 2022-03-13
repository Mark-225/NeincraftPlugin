package de.neincraft.neincraftplugin.modules.commands;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.logging.Level;

@NeincraftModule(id = "CommandLoader", isVital = true)
public class CommandLoader extends AbstractModule {


    @Override
    protected boolean initModule() {
        return true;
    }

    @Override
    public void preInit() {
        for(Map.Entry<Class<? extends AbstractModule>, AbstractModule> entry : AbstractModule.getInstances().entrySet()){
            Class<? extends AbstractModule> moduleClass = entry.getKey();
            AbstractModule instance = entry.getValue();
            for(Field f : moduleClass.getDeclaredFields()){
                if(Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()) || !f.isAnnotationPresent(InjectCommand.class) || !CommandExecutor.class.isAssignableFrom(f.getType())) continue;
                InjectCommand annotation = f.getAnnotation(InjectCommand.class);
                PluginCommand command = Bukkit.getPluginCommand(annotation.value());
                if(command == null) continue;
                CommandExecutor executor = null;
                try {
                    executor = (CommandExecutor) f.getType().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    getLogger().log(Level.WARNING, "An error occured during command instantiation", e);
                    continue;
                }
                command.setExecutor(executor);
                if(executor instanceof TabCompleter)
                    command.setTabCompleter((TabCompleter) executor);
                f.setAccessible(true);
                try {
                    f.set(instance, executor);
                } catch (IllegalAccessException e) {
                    getLogger().log(Level.WARNING, "An error occured during command injection", e);
                    continue;
                }
            }
        }
    }



    @Override
    public void unload() {

    }
}
