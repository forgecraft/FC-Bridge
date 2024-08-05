package com.forgecraft.mods.bridge.structs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TickTimeHolder(
        double meanTickTime,
        double meanTPS
) {
    public static StreamCodec<FriendlyByteBuf, TickTimeHolder> STEAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            TickTimeHolder::meanTickTime,
            ByteBufCodecs.DOUBLE,
            TickTimeHolder::meanTPS,
            TickTimeHolder::new
    );
}
