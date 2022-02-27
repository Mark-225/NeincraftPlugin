package de.neincraft.neincraftplugin.modules.portals.repository;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.database.DatabaseModule;
import de.neincraft.neincraftplugin.modules.plots.repository.PlotRepository;
import de.neincraft.neincraftplugin.modules.portals.dto.Portal;
import org.hibernate.Session;

import javax.persistence.FlushModeType;
import java.util.List;
import java.util.Optional;

public class PortalRepository implements AutoCloseable{

    private final Session session;

    public static PortalRepository getRepository(){
        Optional<DatabaseModule> oDatabase = AbstractModule.getInstance(DatabaseModule.class);
        if(oDatabase.isEmpty()) return null;
        return new PortalRepository(oDatabase.get().getSession());
    }

    private PortalRepository(Session session){
        this.session = session;
        session.setFlushMode(FlushModeType.COMMIT);
        session.beginTransaction();
    }

    public void save(Portal portal){
        session.saveOrUpdate(portal);
    }

    public void delete(Portal portal){
        session.delete(portal);
    }

    public List<Portal> findAll(){
        return session.createQuery("SELECT p from Portal p", Portal.class).getResultList();
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
