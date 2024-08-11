package net.forgecraft.mods.bridge.client.network;

import net.forgecraft.mods.bridge.client.BridgeClientData;
import net.forgecraft.mods.bridge.network.TPSPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TPSReplyHandler {
    public static void onClient(final TPSPacket data, final IPayloadContext context) {
        context.enqueueWork(() -> BridgeClientData.INSTANCE.setServerTps(data));
    }
}
