package de.neincraft.neincraftplugin.modules.playerstats;

import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.database.DatabaseModule;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.FlushModeType;
import java.util.Optional;
import java.util.UUID;

public class PlayerDataRepo implements AutoCloseable{
    private final Session session;

    public static PlayerDataRepo getRepository(){
        Optional<DatabaseModule> oDatabase = Module.getInstance(DatabaseModule.class);
        if(oDatabase.isEmpty()) return null;
        return new PlayerDataRepo(oDatabase.get().getSession());
    }

    private PlayerDataRepo(Session session){
        this.session = session;
        session.setFlushMode(FlushModeType.COMMIT);
        session.beginTransaction();
    }

    public PlayerData getOrCreate(UUID uuid){
        PlayerData pd = session.get(PlayerData.class, uuid);
        if(pd == null) {
            pd = new PlayerData(uuid);
            save(pd);
        }
        return pd;
    }

    public void save(PlayerData pd){
        session.saveOrUpdate(pd);
        session.getTransaction().commit();
    }


    @Override
    public void close() {
        if(session != null)
            session.close();
    }
}
