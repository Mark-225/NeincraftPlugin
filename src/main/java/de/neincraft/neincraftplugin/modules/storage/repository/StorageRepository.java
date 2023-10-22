package de.neincraft.neincraftplugin.modules.storage.repository;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.database.DatabaseModule;
import de.neincraft.neincraftplugin.modules.portals.dto.Portal;
import de.neincraft.neincraftplugin.modules.portals.repository.PortalRepository;
import de.neincraft.neincraftplugin.modules.storage.dto.StorageSystem;
import org.hibernate.Session;

import jakarta.persistence.FlushModeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StorageRepository implements AutoCloseable{

    private final Session session;

    public static StorageRepository getRepository(){
        Optional<DatabaseModule> oDatabase = AbstractModule.getInstance(DatabaseModule.class);
        if(oDatabase.isEmpty()) return null;
        return new StorageRepository(oDatabase.get().getSession());
    }

    private StorageRepository(Session session){
        this.session = session;
        session.setFlushMode(FlushModeType.COMMIT);
        session.beginTransaction();
    }

    public void save(StorageSystem system){
        session.saveOrUpdate(system);
    }

    public void delete(StorageSystem system){
        session.delete(system);
    }

    public StorageSystem getOrCreate(UUID owner){
        Optional<StorageSystem> fromDB = session.createQuery("select s from StorageSystem s where s.owner = :owner", StorageSystem.class).setParameter("owner", owner).getResultStream().findFirst();
        return fromDB.orElseGet(() -> new StorageSystem(owner, 1000, new ArrayList<>()));
    }

    public void commit(){
        session.getTransaction().commit();
    }

    @Override
    public void close() throws Exception {
        if(session != null)
            session.close();
    }
}
