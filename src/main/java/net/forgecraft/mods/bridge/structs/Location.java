package net.forgecraft.mods.bridge.structs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

public record Location(
        ResourceKey<Level> dimension,
        BlockPos pos,
        Vec2 rotation,
        Component name,
        Component description,
        boolean hidden
) {
    private static final StreamCodec<FriendlyByteBuf, Vec2> ROTATION_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            (vec) -> vec.x,
            ByteBufCodecs.FLOAT,
            (vec) -> vec.y,
            Vec2::new
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, Location> STREAM_CODEC = StreamCodec.composite(
        ResourceKey.streamCodec(Registries.DIMENSION), Location::dimension,
        BlockPos.STREAM_CODEC, Location::pos,
        ROTATION_CODEC, Location::rotation,
        ComponentSerialization.STREAM_CODEC, Location::name,
        ComponentSerialization.STREAM_CODEC, Location::description,
        ByteBufCodecs.BOOL, Location::hidden,
        Location::new
    );
}
