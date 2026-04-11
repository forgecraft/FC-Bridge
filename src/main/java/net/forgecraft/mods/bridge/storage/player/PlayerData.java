package net.forgecraft.mods.bridge.storage.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.LinkedList;

public record PlayerData(
        LinkedList<Home> homes
) {
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Home.CODEC.listOf().fieldOf("homes").xmap(
                    LinkedList::new,
                    input -> input.stream().toList()
            ).forGetter(PlayerData::homes)
    ).apply(instance, PlayerData::new));
}
