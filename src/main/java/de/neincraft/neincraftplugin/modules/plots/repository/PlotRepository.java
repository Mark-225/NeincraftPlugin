package de.neincraft.neincraftplugin.modules.plots.repository;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.database.DatabaseModule;
import de.neincraft.neincraftplugin.modules.plots.dto.PlotData;
import org.hibernate.Session;

import javax.persistence.FlushModeType;
import java.util.List;
import java.util.Optional;

public class PlotRepository implements AutoCloseable{

    private final Session session;

    public static PlotRepository getRepository(){
        Optional<DatabaseModule> oDatabase = AbstractModule.getInstance(DatabaseModule.class);
        if(oDatabase.isEmpty()) return null;
        return new PlotRepository(oDatabase.get().getSession());
    }

    private PlotRepository(Session session){
        this.session = session;
        session.setFlushMode(FlushModeType.COMMIT);
        session.beginTransaction();
    }

    public List<PlotData> findAll(){
        return session.createQuery("SELECT p from PlotData p", PlotData.class).getResultList();
    }

    public void deleteById(long plotID){
        session.createQuery("DELETE FROM PlotData p WHERE p.id = :id").setParameter("id", plotID).executeUpdate();
    }

    public void delete(PlotData plot){
        session.delete(plot);
    }

    public PlotData findById(long id){
        return session.find(PlotData.class, id);
    }

    public void persist(PlotData plotData){
        session.saveOrUpdate(plotData);
    }

    public void commit(){
        session.getTransaction().commit();
    }


    @Override
    public void close(){
        if(session != null)
            session.close();
    }
}
