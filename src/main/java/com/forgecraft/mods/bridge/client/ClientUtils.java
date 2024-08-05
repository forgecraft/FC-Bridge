package com.forgecraft.mods.bridge.client;

import net.minecraft.client.Minecraft;

import java.util.Optional;

public class ClientUtils {
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
}
