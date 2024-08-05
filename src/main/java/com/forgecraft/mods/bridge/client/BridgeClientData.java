package com.forgecraft.mods.bridge.client;

import com.forgecraft.mods.bridge.network.TPSPacket;
import com.forgecraft.mods.bridge.structs.TickTimeHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public enum BridgeClientData {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeClientData.class);

    private HashMap<ResourceLocation, TickTimeHolder> serverTps = new HashMap<>();

    public void setServerTps(Map<ResourceLocation, TickTimeHolder> serverTps) {
        this.serverTps = new HashMap<>(serverTps);
    }

    public HashMap<ResourceLocation, TickTimeHolder> getServerTps() {
        return serverTps;
    }

    public void requestServerTpsUpdate() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            LOGGER.error("Connection is null, unable to request TPS data");
            return;
        }

        connection.send(new TPSPacket(HashMap.newHashMap(0)));
    }

    public void clearServerTps() {
        serverTps.clear();
    }
}
