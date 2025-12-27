package net.forgecraft.mods.bridge.structs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

/**
 * Represents a position in a dimension with rotation
 *
 * @param dimension The dimension
 * @param pos The position
 * @param rotation The rotation
 */
public record DimensionalPos(
        ResourceKey<Level> dimension,
        BlockPos pos,
        Vec2 rotation
) {
    public static final Codec<DimensionalPos> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionalPos::dimension),
                    BlockPos.CODEC.fieldOf("pos").forGetter(DimensionalPos::pos),
                    Vec2.CODEC.fieldOf("rotation").forGetter(DimensionalPos::rotation)
            ).apply(instance, DimensionalPos::new)
    );

    /**
     * Creates a new instance of DimensionalPos from a given player
     * @param player The player
     * @return The new instance
     */
    public static DimensionalPos of(Player player) {
        return new DimensionalPos(
                player.level().dimension(),
                player.blockPosition(),
                player.getRotationVector()
        );
    }
}
