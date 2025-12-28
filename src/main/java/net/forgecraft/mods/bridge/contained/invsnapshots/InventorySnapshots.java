package net.forgecraft.mods.bridge.contained.invsnapshots;

import com.mojang.serialization.JsonOps;
import net.forgecraft.mods.bridge.Bridge;
import net.forgecraft.mods.bridge.structs.TimestampedData;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Path snapshotsDir = Bridge.getFcDataDir().resolve("inventory");

    private final HashMap<UUID, Set<TimestampedData<Path>>> snapshots = new HashMap<>();
    private Instant lastSave = Instant.EPOCH;

    public void init(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::serverSaving);

        if (Files.notExists(snapshotsDir)) {
            try {
                Files.createDirectories(snapshotsDir);
            } catch (Exception e) {
                LOGGER.error("Failed to create snapshots directory", e);
            }
        }

        loadSnapshots();
    }

    // Assuming we back up every 5 minutes and we keep snapshots for 30 days, we'd be looking to have around
    // 24 / 5 * 30 = 144 snapshots per player stored.
    private void serverSaving(LevelEvent.Save event) {
        var rawLevel = event.getLevel();
        if (!(rawLevel instanceof Level level)) return;

        // Only back up when the overworld is being saved
        if (level.dimension() != Level.OVERWORLD) return;

        // Throttle to once every 5 minutes
        if (Instant.now().isBefore(lastSave.plusSeconds(300))) {
            return;
        }

        lastSave = Instant.now();
        save(event.getLevel().getServer());
    }

    private void snapshotInventory(Player player) {
        // TODO: Support curios and add extended support for other personal storage mods.
        var reporter = new ProblemReporter.Collector();
        TagValueOutput withContext = TagValueOutput.createWithContext(reporter, player.level().registryAccess());
        var dataOutput = withContext
                .list("Inventory", ItemStackWithSlot.CODEC);

        player.getInventory().save(dataOutput);

        if (!reporter.isEmpty()) {
            LOGGER.error("Failed to snapshot inventory for player {} due to problems during serialization: {}", player.getName().getString(), reporter.getReport());
            return;
        }

        // Write the data to disk
        CompoundTag output = withContext.buildResult();

        // Convert the compound tag to JSON
        var jsonElement = CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, output)
                .getOrThrow();

        // Nice, easy to read timestamp format (YYYY-MM-DD_HH-MM-SS)
        var timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        var filename = String.format("%s_%s.json", player.getUUID(), timestamp);
        var filePath = snapshotsDir.resolve(filename);

        try {
            Files.writeString(filePath, jsonElement.toString());
        } catch (IOException e) {
            LOGGER.error("Failed to write inventory snapshot for player {}", player.getName().getString(), e);
        }
    }

    private void loadSnapshots() {
        if (Files.notExists(snapshotsDir)) return;

        // Read the files in the path
        try (var files = Files.list(snapshotsDir)) {
            var filesList = files.toList();
            for (var file : filesList) {
                var fileName = file.getFileName().toString();
                var parts = fileName.split("_");
                if (parts.length < 2) continue;

                try {
                    var playerId = UUID.fromString(parts[0]);
                    // Reconstruct the timestamp part
                    var timestampPart = String.join("_", Stream.of(parts).skip(1).toArray(String[]::new))
                            .replace(".json", "");
                    var timestamp = LocalDateTime.parse(timestampPart, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                    var epochSeconds = timestamp.atZone(ZoneId.systemDefault()).toEpochSecond();

                    var timestampedData = new TimestampedData<>((int) epochSeconds, file);

                    snapshots.computeIfAbsent(playerId, _ -> ConcurrentHashMap.newKeySet())
                            .add(timestampedData);
                } catch (Exception e) {
                    LOGGER.error("Failed to parse snapshot file name: {}", fileName, e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void cleanupOldSnapshots() {
        if (Files.notExists(snapshotsDir)) return;

        // Read the files in the path
        try (Stream<Path> files = Files.list(snapshotsDir)) {
            var filesList = files.toList();

            // Find any files that are older than 30 days
            for (var file : filesList) {
                var lastModified = Files.getLastModifiedTime(file).toMillis();
                var now = System.currentTimeMillis();

                // If older than 30 days, delete the file
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
}
