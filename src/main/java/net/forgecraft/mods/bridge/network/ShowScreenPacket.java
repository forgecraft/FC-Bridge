package net.forgecraft.mods.bridge.network;

import net.forgecraft.mods.bridge.Bridge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShowScreenPacket(
        Identifier screenType
) implements CustomPacketPayload {
    public static final Type<ShowScreenPacket> TYPE = new Type<>(Bridge.location("show_screen"));

    public static final StreamCodec<FriendlyByteBuf, ShowScreenPacket> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            ShowScreenPacket::screenType,
            ShowScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
