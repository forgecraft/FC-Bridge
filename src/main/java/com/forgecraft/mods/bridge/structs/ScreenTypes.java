package com.forgecraft.mods.bridge.structs;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static com.forgecraft.mods.bridge.Bridge.location;

public enum ScreenTypes {
    TPS(location("tps")),
    CLIENT_SETTINGS(location("client_settings"));

    private final ResourceLocation location;

    ScreenTypes(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public static Optional<ScreenTypes> fromLocation(ResourceLocation location) {
        for (ScreenTypes type : values()) {
            if (type.location.equals(location)) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }
}
