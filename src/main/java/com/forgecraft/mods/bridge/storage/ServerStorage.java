package com.forgecraft.mods.bridge.storage;

import com.forgecraft.mods.bridge.structs.DimensionalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ServerStorage extends SavedData {
    private static ServerStorage INSTANCE;

    private HashMap<UUID, DimensionalPos> spectatorLocationsMemory = new HashMap<>();

    public static ServerStorage getOrCreate(MinecraftServer server) {
        if (INSTANCE == null) {
            INSTANCE = server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(new Factory<>(
                    ServerStorage::new,
                    ServerStorage::load,
                    null
            ), "fcbridge_global_data");
        }

        return INSTANCE;
    }

    public ServerStorage() {
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

    public static ServerStorage load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        var storage = new ServerStorage();
        storage.spectatorLocationsMemory = new HashMap<>();

        var locations = compoundTag.getList("spectatorLocations", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < locations.size(); i++) {
            var locationHolder = locations.getCompound(i);
            if (!locationHolder.contains("uuid") || !locationHolder.contains("location")) {
                continue;
            }

            var uuid = NbtUtils.loadUUID(Objects.requireNonNull(locationHolder.get("uuid")));
            var location = DimensionalPos.load(locationHolder.getCompound("location"));
            storage.spectatorLocationsMemory.put(uuid, location);
        }

        return storage;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        var locations = new ListTag();
        for (var entry : spectatorLocationsMemory.entrySet()) {
            var locationHolder = new CompoundTag();
            locationHolder.put("uuid", NbtUtils.createUUID(entry.getKey()));
            locationHolder.put("location", entry.getValue().save());
            locations.add(locationHolder);
        }

        compoundTag.put("spectatorLocations", locations);
        return compoundTag;
    }
}
