package net.forgecraft.mods.bridge.client;

import com.mojang.logging.LogUtils;
import net.forgecraft.mods.bridge.client.screens.ClientSettingsScreen;
import net.forgecraft.mods.bridge.client.screens.TPSScreen;
import net.forgecraft.mods.bridge.structs.ScreenType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

import java.util.Optional;

public class ClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Sometimes you're gunna wanna try and get Minecraft before Minecraft is ready OR minecraft has finished setting up
     * this typically happens when you're doing something that happens right when minecraft loads but hasn't quite finished
     * initializing yet. This provides a safe way to get Minecraft without crashing the game.
     *
     * @return An optional containing Minecraft if it's available, otherwise an empty optional.
     */
    public static Optional<Minecraft> getMinecraft() {
        try {
            return Optional.of(Minecraft.getInstance());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static void showScreen(ScreenType type) {
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
    }
}
