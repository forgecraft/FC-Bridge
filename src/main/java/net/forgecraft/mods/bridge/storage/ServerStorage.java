package net.forgecraft.mods.bridge.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.forgecraft.mods.bridge.structs.DimensionalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ServerStorage extends SavedData {
    private static final Codec<ServerStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, DimensionalPos.CODEC)
                    .fieldOf("spectator_locations")
                    .forGetter(ServerStorage::spectatorLocationsMemory)
    ).apply(instance, ServerStorage::new));

    private static final SavedDataType<ServerStorage> TYPE = new SavedDataType<>(
            "fcbridge_global_data",
            ServerStorage::new,
            CODEC
    );

    private final HashMap<UUID, DimensionalPos> spectatorLocationsMemory;

    public static ServerStorage getOrCreate(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD)
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public ServerStorage() {
        super();
        this.spectatorLocationsMemory = new HashMap<>();
    }

    public ServerStorage(Map<UUID, DimensionalPos> spectatorLocationsMemory) {
        this.spectatorLocationsMemory = new HashMap<>(spectatorLocationsMemory);
    }

    public void setSpectatorLocation(UUID player, DimensionalPos location) {
        spectatorLocationsMemory.put(player, location);
        setDirty();
    }

    public void removeSpectatorLocation(UUID player) {
        spectatorLocationsMemory.remove(player);
        setDirty();
    }

    @Nullable
    public DimensionalPos getSpectatorLocation(UUID player) {
        return spectatorLocationsMemory.get(player);
    }

    public HashMap<UUID, DimensionalPos> spectatorLocationsMemory() {
        return spectatorLocationsMemory;
    }
}
