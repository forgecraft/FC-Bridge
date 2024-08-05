package com.forgecraft.mods.bridge.client.screens;

import com.forgecraft.mods.bridge.structs.ScreenTypes;
import net.minecraft.client.gui.screens.Screen;

public class ScreenTypeScreenMap {
    /**
     * Get a screen based on the type
     *
     * @implNote We're using a switch statement instead of a direct map so that the compiler can check if we've missed any cases.
     * @param type The type of screen to get
     * @return The screen
     */
    public static Screen getScreen(ScreenTypes type) {
        return switch (type) {
            case TPS -> new TPSScreen();
            case CLIENT_SETTINGS -> new ClientSettingsScreen();
        };
    }
}
