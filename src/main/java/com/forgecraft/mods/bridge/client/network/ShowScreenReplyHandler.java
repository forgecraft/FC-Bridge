package com.forgecraft.mods.bridge.client.network;

import com.forgecraft.mods.bridge.client.screens.ScreenTypeScreenMap;
import com.forgecraft.mods.bridge.structs.ScreenTypes;
import com.forgecraft.mods.bridge.network.ShowScreenPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowScreenReplyHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowScreenReplyHandler.class);

    public static void onClient(final ShowScreenPacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var type = ScreenTypes.fromLocation(data.screenType());
            if (type.isEmpty()) {
                LOGGER.error("Unknown screen type: {}", data.screenType());
                return;
            }

            Minecraft.getInstance().setScreen(ScreenTypeScreenMap.getScreen(type.get()));
        });
    }
}
