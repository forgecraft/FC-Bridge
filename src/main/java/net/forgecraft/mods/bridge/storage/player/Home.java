package net.forgecraft.mods.bridge.storage.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;

public record Home(
    String name,
    String icon,
    GlobalPos pos,
    float yaw,
    float pitch
) {
    public static final Codec<Home> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(Home::name),
        Codec.STRING.fieldOf("icon").forGetter(Home::icon), // TODO: Cusotm shit.
        GlobalPos.CODEC.fieldOf("pos").forGetter(Home::pos),
        Codec.FLOAT.fieldOf("yaw").forGetter(Home::yaw),
        Codec.FLOAT.fieldOf("pitch").forGetter(Home::pitch)
    ).apply(instance, Home::new));

    public static Home create(ServerPlayer player, String name, String icon, BlockPos pos) {
        return new Home(
                name,
                icon,
                GlobalPos.of(player.level().dimension(), pos),
                player.getYRot(),
                player.getXRot()
        );
    }
}
