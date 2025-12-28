package net.forgecraft.mods.bridge.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.forgecraft.mods.bridge.storage.player.PlayerData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class PlayerStorage extends SavedData {
    private final HashMap<UUID, PlayerData> data;

    private static final Codec<PlayerStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, PlayerData.CODEC)
                    .fieldOf("player_data")
                    .forGetter(PlayerStorage::playerData)
    ).apply(instance, PlayerStorage::new));

    private static final SavedDataType<PlayerStorage> TYPE = new SavedDataType<>(
            "fcbridge_player_data",
            PlayerStorage::new,
            CODEC
    );

    public static PlayerStorage getOrCreate(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD)
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public PlayerStorage() {
        super();
        this.data = new HashMap<>();
    }

    public PlayerStorage(Map<UUID, PlayerData> data) {
        this.data = new HashMap<>(data);
    }

    public PlayerData forPlayer(Player player) {
        if (!data.containsKey(player.getUUID())) {
            data.put(player.getUUID(), new PlayerData(new LinkedList<>()));
            setDirty();
        }

        return data.get(player.getUUID());
    }

    public static void setDirty(MinecraftServer server) {
        PlayerStorage storage = getOrCreate(server);
        storage.setDirty();
    }

    public HashMap<UUID, PlayerData> playerData() {
        return data;
    }
}
