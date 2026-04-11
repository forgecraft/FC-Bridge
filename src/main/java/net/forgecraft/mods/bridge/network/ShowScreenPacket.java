package net.forgecraft.mods.bridge.network;

import net.forgecraft.mods.bridge.Bridge;
import net.forgecraft.mods.bridge.structs.ScreenType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record ShowScreenPacket(
        ScreenType screenType
) implements CustomPacketPayload {
    public static final Type<ShowScreenPacket> TYPE = new Type<>(Bridge.id("show_screen"));

    public static final StreamCodec<FriendlyByteBuf, ShowScreenPacket> CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(ScreenType.class),
            ShowScreenPacket::screenType,
            ShowScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
