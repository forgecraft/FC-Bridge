package net.forgecraft.mods.bridge.network;

import net.forgecraft.mods.bridge.client.network.ShowScreenReplyHandler;
import net.forgecraft.mods.bridge.client.network.TPSReplyHandler;
import com.google.common.eventbus.Subscribe;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class BridgeNetwork {
    @Subscribe
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(
                TPSPacket.TYPE,
                TPSPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        TPSReplyHandler::onClient,
                        TPSPacket::onServer
                ));

        registrar.playToClient(
                ShowScreenPacket.TYPE,
                ShowScreenPacket.CODEC,
                ShowScreenReplyHandler::onClient
        );
    }
}
