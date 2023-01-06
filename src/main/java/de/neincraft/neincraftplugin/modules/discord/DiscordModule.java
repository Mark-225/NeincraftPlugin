package de.neincraft.neincraftplugin.modules.discord;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.modules.discord.repository.DiscordRepository;
import de.neincraft.neincraftplugin.util.lang.Lang;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;

@NeincraftModule(id = "discord", requiredModules = {"database"})
public class DiscordModule extends AbstractModule implements Listener {

    private static final NamespacedKey PLAYERHEAD_CACHE =  new NamespacedKey(NeincraftPlugin.getInstance(), "playerhead-cache");
    public static final NamespacedKey DISCORD_OPT_OUT = new NamespacedKey(NeincraftPlugin.getInstance(), "discord-opt-out");

    private Map<Long, UUID> discordAccountMap = new ConcurrentHashMap<>();
    private BiMap<String, Long> verificationTokens = Maps.synchronizedBiMap(HashBiMap.create());
    private File playerHeadDirectory;
    private File steveHead;
    private IncomingWebhook webhook;
    private DiscordApi discordApi;
    private long chatChannelId;
    private long verificationChannelId;
    private Role ingameChatRole;
    private String urlPattern;

    private final Queue<Map.Entry<UUID, String>> chatMessageQueue = new ConcurrentLinkedQueue<>();

    @InjectCommand("discord")
    private DiscordCommand discordCommand;

    @Override
    protected boolean initModule() {
        try(DiscordRepository repository = DiscordRepository.getRepository()){
            if(repository != null)
                discordAccountMap = repository.findAll();
        }catch(Exception e){
            getLogger().log(Level.WARNING, "Could not load Discord account map!", e);
        }

        NeincraftPlugin.getInstance().saveResource("discord.yml", false);
        File configFile = new File(NeincraftPlugin.getInstance().getDataFolder(), "discord.yml");
        FileConfiguration discordConfig = new YamlConfiguration();
        try {
            discordConfig.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.WARNING, "Could not load discord config file.", e);
            return false;
        }

        String rootPath;
        if((rootPath = discordConfig.getString("cacheRoot")) != null)
            playerHeadDirectory = new File(rootPath);
        if(playerHeadDirectory == null || !playerHeadDirectory.exists() || !playerHeadDirectory.isDirectory()){
            getLogger().log(Level.WARNING, "Could not create player head cache");
            return false;
        }

        urlPattern = discordConfig.getString("cacheUrlPattern");
        steveHead = new File(playerHeadDirectory, "steve.png");

        discordApi = new DiscordApiBuilder().setToken(discordConfig.getString("botToken")).login().join();
        webhook = discordApi.getIncomingWebhookByUrl(discordConfig.getString("chatWebhook")).join();
        chatChannelId = discordConfig.getLong("chatChannel");
        ingameChatRole = discordApi.getRoleById(discordConfig.getLong("ingameChatRole")).orElse(null);

        if(discordConfig.contains("verificationChannel")) {
            verificationChannelId = discordConfig.getLong("verificationChannel");
            if(discordApi.getTextChannelById(verificationChannelId).isPresent())
                setupVerification();
        }
        discordApi.getTextChannelById(chatChannelId).ifPresent(chatChannel -> chatChannel.addMessageCreateListener(this::onDiscordMessage));


        Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), this::chatTask, 20, 20);
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        Bukkit.getScheduler().runTaskLaterAsynchronously(NeincraftPlugin.getInstance(), this::cleanup, 200);
        return true;
    }

    public void sendVerificationMessage(){
        discordApi.getTextChannelById(verificationChannelId).ifPresent(this::sendVerificationMessageChannel);
    }

    private void sendVerificationMessageChannel(TextChannel verificationChannel){
        new MessageBuilder()
                .setContent("""
                            Um auf den Ingame Chat zugreifen zu können muss du deinen Account mit einem Minecraft Account verbinden.
                            Klicke dazu unten auf den Button und folge den weiteren Anweisungen.
                            
                            Du kannst mehrere Discord Accounts einem Minecraft Account zuweisen, jedoch nicht mehrere Minecraft Accounts einem Discord Account. Ist dein Account bereits verbunden, überschreibt ein erneutes Verbinden den aktuell zugewiesenen Minecraft account.
                            
                            **Achtung!** Der Button "Verbindung trennen" trennt deinen Account und entzieht den Zugang zum Chat ohne weitere Bestätigung.
                            """)
                .addComponents(
                        ActionRow.of(Button.success("link_account", "Account verbinden"),
                                Button.danger("unlink_account", "Verbindung trennen"))
                )
                .send(verificationChannel);
    }

    private void onDiscordMessage(MessageCreateEvent event){
        if(event.getMessageAuthor().isWebhook() || event.getMessageAuthor().isYourself()) return;
        if(discordAccountMap.containsKey(event.getMessageAuthor().getId())){
            UUID minecraftUUID = discordAccountMap.get(event.getMessage().getAuthor().getId());
            OfflinePlayer op = Bukkit.getOfflinePlayer(minecraftUUID);
            if(!op.hasPlayedBefore()){
                event.getMessage().delete();
                return;
            }
            NeincraftUtils.formattedBroadcast(Lang.DISCORD_CHAT_PRESET, MessageType.CHAT, true, "name", op.getName(), "message", event.getMessage().getContent());
        }else {
            event.getMessage().delete();
        }
    }

    private void setupVerification(){
        discordApi.addMessageComponentCreateListener(event -> {
            MessageComponentInteraction messageComponentInteraction = event.getMessageComponentInteraction();
            String customId = messageComponentInteraction.getCustomId();
            User user = messageComponentInteraction.getUser();
            if(messageComponentInteraction.getChannel().isEmpty() || messageComponentInteraction.getChannel().get().getId() != verificationChannelId) return;
            switch (customId){
                case "link_account" -> {
                    String token = verificationTokens.inverse().computeIfAbsent(user.getId(), this::generateNewToken);
                    messageComponentInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent(String.format("""
                            Bitte gebe den folgenden Command auf dem Server ein:
                            `/discord link %s`
                            (Der Code läuft in 10 Minuten ab)""", token))
                            .respond();
                }
                case "unlink_account" -> {
                    try (DiscordRepository repository = DiscordRepository.getRepository()){
                        if(repository  == null) return;
                        repository.delete(user.getId());
                        repository.commit();
                        discordAccountMap.remove(user.getId());
                        user.removeRole(ingameChatRole);
                        messageComponentInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("Wenn du einen Minecraft Account verbunden hattest, wurde er jetzt getrennt!").respond();
                    }catch (Exception e) {
                        getLogger().log(Level.WARNING, "Could not unlink Discord account", e);
                    }
                }
            }
        });
    }

    private String generateNewToken(long userId){
        Random random = new Random();
        String t;
        do {
            byte[] bytes = new byte[8];
            random.nextBytes(bytes);
            t = Base64.getEncoder().encodeToString(bytes);
        } while (verificationTokens.containsKey(t));
        final String finalToken = t;
        Bukkit.getScheduler().runTaskLaterAsynchronously(NeincraftPlugin.getInstance(), () -> verificationTokens.remove(finalToken, userId), 12_000);
        return t;
    }

    public void cleanup(){
        discordApi.getTextChannelById(chatChannelId).ifPresent(this::cleanupChannel);
    }

    private void cleanupChannel(TextChannel chatChannel){
        if(chatChannel == null) return;
        long time = System.currentTimeMillis() / 1000;
        List<Message> messages = chatChannel.getMessagesAsStream().dropWhile(message -> time - message.getCreationTimestamp().getEpochSecond() <= 86_400).limit(100).toList();
        if(messages.size() > 0)
            chatChannel.deleteMessages(messages).join();
        Bukkit.getScheduler().runTaskLaterAsynchronously(NeincraftPlugin.getInstance(), this::cleanup, 6000);
    }

    private void chatTask(){
        if(chatMessageQueue.isEmpty()) return;
        UUID uuid = chatMessageQueue.peek().getKey();
        String name = NeincraftUtils.uuidToName(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(NeincraftPlugin.getInstance(), () -> {
            List<String> messages = new ArrayList<>();
            while(!chatMessageQueue.isEmpty() && chatMessageQueue.peek().getKey().equals(uuid)) {
                messages.add(stripFormat(chatMessageQueue.poll().getValue()));
            }
            String finalMessage = String.join("\n", messages);
            if(finalMessage.length() >= 2000) return;
            File head = new File(playerHeadDirectory, uuid + ".png");
            if(!head.exists())
                head = steveHead;
            try {
                webhook.sendMessage(finalMessage, name, new URL(urlPattern + head.getName())).whenComplete((m, ex) -> {
                    if(ex != null)
                        getLogger().log(Level.WARNING, "Could not send chat message to discord", ex);
                });
            } catch (MalformedURLException e) {
                getLogger().log(Level.WARNING, "Could not send chat message to discord", e);
            }
        });
    }

    private String stripFormat(String in){
        return in.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("*", "\\*")
                .replace("`", "\\`");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws MalformedURLException {
        Player p = event.getPlayer();

        webhook.sendMessage(new EmbedBuilder().setColor(Color.GREEN).addInlineField(p.getName(), "hat das Spiel betreten")).whenComplete((m, ex) -> {
            if(ex != null)
                getLogger().log(Level.WARNING, "Could not send join message to discord", ex);
        });

        PersistentDataContainer pdc = p.getPersistentDataContainer();
        Long cachedTime;
        if((cachedTime = pdc.get(PLAYERHEAD_CACHE, PersistentDataType.LONG)) != null && System.currentTimeMillis() - cachedTime <= 900_000) return;
        pdc.set(PLAYERHEAD_CACHE, PersistentDataType.LONG, System.currentTimeMillis());
        UUID uuid = p.getUniqueId();
        PlayerProfile profile =  Bukkit.createProfile(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(NeincraftPlugin.getInstance(), () ->{
            profile.complete(true);
            File f = new File(playerHeadDirectory, uuid + ".png");
            if(profile.getTextures().getSkin() == null) return;
            try {
                BufferedImage skin = ImageIO.read(profile.getTextures().getSkin());
                BufferedImage face = skin.getSubimage(8, 8, 8, 8);
                BufferedImage overlay = skin.getSubimage(40, 8, 8, 8);
                BufferedImage scaledFace = new BufferedImage(270, 270, BufferedImage.TYPE_INT_ARGB);
                Graphics2D scaledFaceGraphics = scaledFace.createGraphics();
                scaledFaceGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                scaledFaceGraphics.drawImage(face, 15, 15, 255, 255, 0, 0, 8, 8, null);
                scaledFaceGraphics.drawImage(overlay, 0, 0, 270, 270, 0, 0, 8, 8, null);
                scaledFaceGraphics.dispose();
                ImageIO.write(scaledFace, "png", f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player p = event.getPlayer();
        webhook.sendMessage(new EmbedBuilder().setColor(Color.RED).addInlineField(p.getName(), "hat das Spiel verlassen")).whenComplete((m, ex) -> {
            if(ex != null)
                getLogger().log(Level.WARNING, "Could not send leave message to discord", ex);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        if(event.isCancelled()) return;
        Player p = event.getPlayer();
        String deathMessage = "-";
        Component c;
        if((c = event.deathMessage()) != null){
            deathMessage = PlainTextComponentSerializer.plainText().serialize(c);
        }
        webhook.sendMessage(new EmbedBuilder().setColor(Color.BLACK).addField(p.getName() + " ist gestorben", deathMessage)).whenComplete((m, ex) -> {
            if(ex != null)
                getLogger().log(Level.WARNING, "Could not send death message to discord", ex);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncChatEvent event){
        final String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        final Player p = event.getPlayer();
        Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () -> {
                    if(p.getPersistentDataContainer().has(DISCORD_OPT_OUT, PersistentDataType.BYTE)) return;
                    chatMessageQueue.add(new AbstractMap.SimpleEntry<>(p.getUniqueId(), message));
        });
    }

    public boolean linkAccount(UUID minecraftUUID, String token){
        if(!verificationTokens.containsKey(token)) return false;
        Long discordId = verificationTokens.remove(token);
        if(discordId == null) return false;
        User user = null;
        try {
            user = discordApi.getUserById(discordId).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            getLogger().log(Level.WARNING, "Could not get user from discord", e);
        }
        if(user == null) return false;
        try(DiscordRepository repo = DiscordRepository.getRepository()){
            if(repo != null) {
                repo.save(discordId, minecraftUUID);
                repo.commit();
                discordAccountMap.put(discordId, minecraftUUID);
                user.addRole(ingameChatRole);
                return true;
            }
        }catch(Exception e){
            getLogger().log(Level.WARNING, "Could not save discord account to database", e);
        }


        return false;
    }

    public void unlinkAccount(UUID minecraftUUID){
        try(DiscordRepository repo = DiscordRepository.getRepository()){
            if(repo != null) {
                discordAccountMap.entrySet().stream().filter(entry -> entry.getValue().equals(minecraftUUID)).forEach(entry -> repo.delete(entry.getKey()));
                repo.commit();
                discordAccountMap.values().removeIf(uuid -> uuid.equals(minecraftUUID));
            }
        }catch (Exception e){
            getLogger().log(Level.WARNING, "Could not delete discord account from database", e);
        }
    }

    @Override
    public void unload() {
        discordApi.disconnect().join();
    }
}
