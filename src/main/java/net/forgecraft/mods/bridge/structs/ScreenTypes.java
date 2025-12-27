package net.forgecraft.mods.bridge.structs;

import net.minecraft.resources.Identifier;

import java.util.Optional;

import static net.forgecraft.mods.bridge.Bridge.location;

public enum ScreenTypes {
    TPS(location("tps")),
    CLIENT_SETTINGS(location("client_settings"));

    private final Identifier location;

    ScreenTypes(Identifier location) {
        this.location = location;
    }

    public Identifier getLocation() {
        return location;
    }

    public static Optional<ScreenTypes> fromLocation(Identifier location) {
        for (ScreenTypes type : values()) {
            if (type.location.equals(location)) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }
}
