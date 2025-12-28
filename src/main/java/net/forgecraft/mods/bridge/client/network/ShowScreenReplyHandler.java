package net.forgecraft.mods.bridge.client.network;

import net.forgecraft.mods.bridge.client.screens.ClientSettingsScreen;
import net.forgecraft.mods.bridge.client.screens.ScreenTypeScreenMap;
import net.forgecraft.mods.bridge.client.screens.TPSScreen;
import net.forgecraft.mods.bridge.structs.ScreenType;
import net.forgecraft.mods.bridge.network.ShowScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowScreenReplyHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowScreenReplyHandler.class);

    public static void onClient(final ShowScreenPacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var type = data.screenType();

            Screen screen = null;
            if (type == ScreenType.TPS) {
                screen = new TPSScreen();
            } else if (type == ScreenType.CLIENT_SETTINGS) {
                screen = new ClientSettingsScreen();
            }

            if (screen == null) {
                LOGGER.error("No screen found for type: {}", type);
                return;
            }

            Minecraft.getInstance().setScreen(screen);
        });
    }
}
