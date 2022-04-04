package de.neincraft.neincraftplugin.modules.discord.repository;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.database.DatabaseModule;
import de.neincraft.neincraftplugin.modules.discord.dto.DiscordAccount;
import org.hibernate.Session;

import javax.persistence.FlushModeType;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DiscordRepository implements AutoCloseable{

    private final Session session;

    public static DiscordRepository getRepository(){
        Optional<DatabaseModule> oDatabase = AbstractModule.getInstance(DatabaseModule.class);
        if(oDatabase.isEmpty()) return null;
        return new DiscordRepository(oDatabase.get().getSession());
    }

    private DiscordRepository(Session session){
        this.session = session;
        session.setFlushMode(FlushModeType.COMMIT);
        session.beginTransaction();
    }

    public void save(Long discordId, UUID minecraftId){
        session.saveOrUpdate(new DiscordAccount(discordId, minecraftId));
    }

    public void delete(Long discordId){
        session.delete(new DiscordAccount(discordId, null));
    }

    public Map<Long, UUID> findAll(){
        return session.createQuery("SELECT d from DiscordAccount d", DiscordAccount.class).getResultList().stream()
                .collect(Collectors.toMap(DiscordAccount::getDiscordId, DiscordAccount::getMinecraftId));
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
