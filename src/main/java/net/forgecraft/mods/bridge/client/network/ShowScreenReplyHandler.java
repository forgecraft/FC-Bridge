package net.forgecraft.mods.bridge.client.network;

import net.forgecraft.mods.bridge.client.ClientUtils;
import net.forgecraft.mods.bridge.client.screens.ClientSettingsScreen;
import net.forgecraft.mods.bridge.client.screens.TPSScreen;
import net.forgecraft.mods.bridge.structs.ScreenType;
import net.forgecraft.mods.bridge.network.ShowScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowScreenReplyHandler {
    public static void onClient(final ShowScreenPacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var type = data.screenType();
            ClientUtils.showScreen(type);
        });
    }
}
