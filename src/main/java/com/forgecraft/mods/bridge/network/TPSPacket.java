package com.forgecraft.mods.bridge.network;

import com.forgecraft.mods.bridge.Bridge;
import com.forgecraft.mods.bridge.structs.TickTimeHolder;
import com.google.common.math.Stats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public record TPSPacket(
    Map<ResourceLocation, TickTimeHolder> dimensionMap
) implements CustomPacketPayload {
    private static final Logger LOGGER = LoggerFactory.getLogger(TPSPacket.class);

    public static final Type<TPSPacket> TYPE = new Type<>(Bridge.location("tps_reply"));

    private static final long[] UNLOADED = new long[] { 0 };

    public static final StreamCodec<FriendlyByteBuf, TPSPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, TickTimeHolder.STEAM_CODEC),
            TPSPacket::dimensionMap,
            TPSPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void onServer(final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Get the server
            MinecraftServer server = context.player().getServer();
            if (server == null) {
                LOGGER.error("Server is null");
                return;
            }

            // Find the TPS data for each dimension
            // Most of this logic is from the Neoforge TPS command
            Map<ResourceLocation, TickTimeHolder> tpsData = new HashMap<>();
            for (ServerLevel dimension : server.getAllLevels()) {
                long[] times = server.getTickTime(dimension.dimension());

                if (times == null) {
                    times = UNLOADED;
                }

                double levelTickTime = Stats.meanOf(times) * 1.0E-6D;
                double levelTPS = TimeUtil.MILLISECONDS_PER_SECOND / Math.max(levelTickTime, server.tickRateManager().millisecondsPerTick());

                tpsData.put(dimension.dimension().location(), new TickTimeHolder(levelTickTime, levelTPS));
            }

            // Send the TPS data back to the client
            context.reply(new TPSPacket(tpsData));
        });
    }
}
