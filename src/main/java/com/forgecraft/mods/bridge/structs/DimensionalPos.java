package com.forgecraft.mods.bridge.structs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", this.dimension.location().toString());
        tag.put("pos", NbtUtils.writeBlockPos(this.pos));
        tag.putFloat("rotationX", this.rotation.x);
        tag.putFloat("rotationY", this.rotation.y);
        return tag;
    }

    public static DimensionalPos load(CompoundTag tag) {
        return new DimensionalPos(
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension"))),
                NbtUtils.readBlockPos(tag, "pos").orElseThrow(),
                new Vec2(tag.getFloat("rotationX"), tag.getFloat("rotationY"))
        );
    }
}
