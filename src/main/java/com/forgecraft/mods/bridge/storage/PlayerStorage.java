package com.forgecraft.mods.bridge.storage;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record PlayerStorage() {
    public static PlayerStorage getOrCreate(Player player) {
        return null;
    }

    public static PlayerStorage getOrCreate(UUID player) {
        return null;
    }
}
