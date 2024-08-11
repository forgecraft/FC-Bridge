package net.forgecraft.mods.bridge.client;

import net.forgecraft.mods.bridge.network.TPSPacket;
import net.forgecraft.mods.bridge.structs.TickTimeHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public enum BridgeClientData {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeClientData.class);

    @Nullable
    private TPSPacket serverTps;
    
    public void setServerTps(@Nullable TPSPacket packet) {
        this.serverTps = packet;
    }

    @Nullable
    public TPSPacket getServerTps() {
        return serverTps;
    }

    public void requestServerTpsUpdate() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            LOGGER.error("Connection is null, unable to request TPS data");
            return;
        }

        connection.send(new TPSPacket());
    }

    public void clearServerTps() {
        serverTps = null;
    }
}
