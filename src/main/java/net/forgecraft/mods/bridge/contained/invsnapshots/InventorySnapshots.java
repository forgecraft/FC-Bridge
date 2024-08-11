package net.forgecraft.mods.bridge.contained.invsnapshots;

import net.forgecraft.mods.bridge.Bridge;
import com.google.common.eventbus.Subscribe;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * An inventory snapshotting system that will capture the contents of the players inventory at any
 * safe point in time and will create backups of the inventory contents for later use.
 */
public enum InventorySnapshots {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySnapshots.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Path snapshotsDir = Bridge.getFcDataDir().resolve("inventory");

    public void init(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::serverAboutToStart);
        NeoForge.EVENT_BUS.addListener(this::serverAboutToStop);

        if (Files.notExists(snapshotsDir)) {
            try {
                Files.createDirectories(snapshotsDir);
            } catch (Exception e) {
                LOGGER.error("Failed to create snapshots directory", e);
            }
        }
    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        executor.scheduleAtFixedRate(() -> save(event.getServer()), 1, 5, TimeUnit.MINUTES);
    }

    private void save(MinecraftServer server) {
        if (server == null) return;
        if (!server.isReady()) return;

        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        for (var player : players) {
            snapshotInventory(player);
        }

        cleanupOldSnapshots();
    }

    private void snapshotInventory(Player player) {
        var inventoryData = player.getInventory().save(new ListTag());
        LOGGER.info("Player {} has {} items in their inventory", player.getName().getString(), inventoryData.size());
    }

    private void cleanupOldSnapshots() {
        if (Files.notExists(snapshotsDir)) return;

        // Read the files in the path
        try (Stream<Path> files = Files.list(snapshotsDir)) {
            var filesList = files.toList();

            // Find any files that are older than 30 days
            for (var file : filesList) {
                var lastModified = Files.getLastModifiedTime(file).toMillis();
                var now = System.currentTimeMillis();

                if (now - lastModified > TimeUnit.DAYS.toMillis(30)) {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        LOGGER.error("Failed to delete snapshot file", e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void serverAboutToStop(ServerAboutToStartEvent event) {
        try {
            executor.shutdown();
        } catch (Exception e) {
            LOGGER.error("Failed to shutdown executor", e);
        }
    }

    @Subscribe
    public void onPlayerDataSaved(PlayerEvent.SaveToFile event) {
        Player entity = event.getEntity();

        var inventoryData = entity.getInventory().save(new ListTag());

        LOGGER.info("Player {} has {} items in their inventory", entity.getName().getString(), inventoryData.size());
        // Create a new snapshot
        // Purge old snapshots

        //LevelEvent.Save
    }
}
