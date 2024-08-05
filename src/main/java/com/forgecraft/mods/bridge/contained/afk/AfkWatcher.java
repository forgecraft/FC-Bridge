package com.forgecraft.mods.bridge.contained.afk;

import com.forgecraft.mods.bridge.config.ServerConfig;
import com.google.common.eventbus.Subscribe;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.UUID;

public enum AfkWatcher {
    INSTANCE;

    private static final int AFK_CHECK_INTERVAL = 10; // 10 seconds

    private final HashMap<UUID, AfkData> afkData = new HashMap<>();

    private boolean initialized = false;

    public void init() {
        if (initialized) {
            throw new IllegalStateException("AfkWatcher has already been initialized");
        }

        initialized = true;
        if (!ServerConfig.AFK_CHECKER_ENABLED.get()) {
            return;
        }

        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::setPlayerTabName);
    }

    @Subscribe
    public void onServerTick(ServerTickEvent.Post event) {
        var ticks = event.getServer().getTickCount();

        // Only check every 10 seconds
        if (ticks % (AFK_CHECK_INTERVAL * 20) != 0) {
            return;
        }

        event.getServer().getPlayerList().getPlayers()
                .forEach(this::performCheckAndUpdate);
    }

    private void performCheckAndUpdate(ServerPlayer player) {
        LocationData location = new LocationData(player.position(), player.getRotationVector());

        // Get the data from the afkData map
        var data = afkData.computeIfAbsent(player.getUUID(), key ->
                // Add zero's to avoid comparing against the below check on the first run
                new AfkData(new LocationData(Vec3.ZERO, Vec2.ZERO), 0, false));

        if (data.lastLocation.equals(location) && !data.afk) {
            data.time += 1;
            data.afk = data.time >= (ServerConfig.AFK_CHECKER_TIME.get() / AFK_CHECK_INTERVAL); // The check runs every 10 seconds so divide by 10
            if (data.afk) {
                player.refreshTabListName();
            }
            afkData.put(player.getUUID(), data);
        } else if (!data.lastLocation.equals(location)) {
            data.lastLocation = location;
            data.time = 0;
            if (data.afk) {
                data.afk = false;
                player.refreshTabListName();
            }
            player.refreshTabListName();
            afkData.put(player.getUUID(), data);
        }
    }

    /**
     * Attempt to apply the AFK tag to the player's tab name if they are AFK, we simply do nothing if they are not AFK
     * as this will allow the following events to do what they need to do
     */
    @Subscribe
    public void setPlayerTabName(PlayerEvent.TabListNameFormat event) {
        var playerData = afkData.get(event.getEntity().getUUID());
        if (playerData == null) {
            // Don't touch
            return;
        }

        var player = event.getEntity();
        var existingName = event.getDisplayName();
        var baseName = existingName == null ? player.getName() : existingName;

        if (playerData.afk) {
            event.setDisplayName(Component.literal("[AFK] ").append(baseName).withStyle(ChatFormatting.GRAY));
        }
    }

    private static class AfkData {
        public LocationData lastLocation;
        public int time;
        public boolean afk;

        public AfkData(LocationData lastLocation, int time, boolean afk) {
            this.lastLocation = lastLocation;
            this.time = time;
            this.afk = afk;
        }
    }

    private record LocationData(
            Vec3 location,
            Vec2 rotation
    ) {
        public boolean equals(LocationData other) {
            return location.equals(other.location) && rotation.equals(other.rotation);
        }
    }
}
