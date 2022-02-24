package de.neincraft.neincraftplugin.modules.database;

import com.zaxxer.hikari.HikariDataSource;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
        String user = dbConfig.getString("user");
        String password = dbConfig.getString("password");
        if("root".equals(user) || "".equals(password)){
            getLogger().log(Level.SEVERE, "You are using the root user or an empty password to connect to the database! This is extremely unsafe in a production environment. It is recommended to create a separate user with access to only one database for this plugin and configure it in the database.yml config file!\n" +
                    "(You can ignore this message if you are in a testing environment)");
        }
        initSessionFactory(dbConfig.getString("url"), user, password);
        return true;
    }

    private void initSessionFactory(String url, String user, String password){
        if(sessionFactory != null)
            return;
        try{
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder();
            Map<String, Object> settings = new HashMap<>();
            settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
            settings.put(Environment.URL, url);
            settings.put(Environment.USER, user);
            settings.put(Environment.PASS, password);
            settings.put(Environment.HBM2DDL_AUTO, "update");
            settings.put(Environment.SHOW_SQL, false);
            settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
            settings.put(Environment.STORAGE_ENGINE, "hibernate.dialect.storage_engine=innodb");

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
