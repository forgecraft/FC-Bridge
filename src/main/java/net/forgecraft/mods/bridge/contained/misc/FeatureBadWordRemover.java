package net.forgecraft.mods.bridge.contained.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic system to attempt to remove bad words from players messages at the point of the
 * server relaying the message back to the other players.
 * <p>
 * This is not intended as a full proof system, it is simply a preventative measure to help safeguard
 * players that opt-in to the bad word filter.
 */
public enum FeatureBadWordRemover {
    INSTANCE;

    private final List<String> badWords = new ArrayList<>();

    public void init() {
        // Register the event bus
        NeoForge.EVENT_BUS.addListener(this::beforePlayerMessageSend);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoinServer);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLeaveServer);

        loadBadWords();
    }

    @Subscribe
    public void beforePlayerMessageSend(ServerChatEvent event) {
        var server = event.getPlayer().getServer();
        if (server == null) {
            return;
        }

        var message = event.getMessage();
        var rawMessage = message.getString();

        if (!checkForSafeGuardedPlayers(server)) {
            return;
        }

        if (containsBadWords(rawMessage)) {
            event.setCanceled(true);
            // TODO: translate
            event.getPlayer().sendSystemMessage(Component.literal("Your message contained bad words and has been censored as there are players online that have opted in to the bad word filter."));
        }
    }

    private boolean containsBadWords(String message) {
        System.out.println("Checking message for bad words: " + message);

        var lowered = message.toLowerCase();
        for (var badWord : badWords) {
            if (lowered.contains(badWord)) {
                return true;
            }
        }

        return false;
    }

    @Subscribe
    public void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent loggedInEvent) {
        this.breakCache();
    }

    @Subscribe
    public void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent loggedOutEvent) {
        this.breakCache();
    }

    /**
     * Checks the servers playerlist for any players that are protected from seeing bad words.
     *
     * @param server The server to check
     * @return True if any players are protected, false otherwise
     */
    private boolean checkForSafeGuardedPlayers(MinecraftServer server) {
        List<String> protectedPlayers = List.of(); // TODO: Get list from server data
        var players = server.getPlayerList().getPlayers();

        for (var player : players) {
            if (protectedPlayers.contains(player.getName().getString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Loads the bad words from file
     */
    private void loadBadWords() {

    }

    private void breakCache() {

    }
}
