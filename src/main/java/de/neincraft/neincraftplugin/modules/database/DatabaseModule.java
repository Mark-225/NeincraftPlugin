package de.neincraft.neincraftplugin.modules.database;

import com.zaxxer.hikari.HikariDataSource;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.NeincraftUtils;
import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.reflections.Reflections;

import javax.persistence.Entity;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@NeincraftModule(
        id = "database",
        isVital = true
)
public class DatabaseModule extends Module {

    HikariDataSource hikariDataSource;

    private SessionFactory sessionFactory;
    private StandardServiceRegistry registry;

    @Override
    protected boolean initModule() {
        NeincraftPlugin.getInstance().saveResource("database.yml", false);
        File configFile = new File(NeincraftPlugin.getInstance().getDataFolder(), "database.yml");
        FileConfiguration dbConfig = new YamlConfiguration();
        try {
            dbConfig.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.WARNING, "Could not load database config file.", e);
            return false;
        }
        if(List.of("url", "user", "password").stream().anyMatch(key -> !dbConfig.contains(key) || !dbConfig.isString(key))){
            getLogger().log(Level.WARNING, "Could not find all necessary values in database config.");
        }
        initSessionFactory(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"));
        return true;
    }

    private void initSessionFactory(String url, String user, String password){
        if(sessionFactory != null)
            return;
        try{
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder();
            Map<String, Object> settings = new HashMap<>();
            settings.put(Environment.DRIVER, "org.mariadb.jdbc.Driver");
            settings.put(Environment.URL, url);
            settings.put(Environment.USER, user);
            settings.put(Environment.PASS, password);
            settings.put(Environment.HBM2DDL_AUTO, "update");
            settings.put(Environment.SHOW_SQL, true);

            settings.put("hibernate.hikari.connectionTimeout", "20000");
            settings.put("hibernate.hikari.minimumIdle", "10");
            settings.put("hibernate.hikari.maximumPoolSize", "20");
            settings.put("hibernate.hikari.idleTimeout", "300000");

            registryBuilder.applySettings(settings);

            registry = registryBuilder.build();

            MetadataSources sources = new MetadataSources(registry);

            Reflections reflections = NeincraftUtils.buildReflections();
            reflections.getTypesAnnotatedWith(Entity.class).forEach(sources::addAnnotatedClass);

            Metadata metadata = sources.getMetadataBuilder().build();
            sessionFactory = metadata.getSessionFactoryBuilder().build();
        }catch(Exception e){
            getLogger().log(Level.WARNING, "Error creating SessionFactory!", e);
            shutdown();
        }
    }

    public Session getSession(){
        return sessionFactory.openSession();
    }


    public void shutdown(){
        if(registry != null){
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Override
    public void unload() {
        shutdown();
    }
}
