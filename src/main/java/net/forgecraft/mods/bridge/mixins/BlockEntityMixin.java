package net.forgecraft.mods.bridge.mixins;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockEntityMixin.class);

    @Shadow public abstract BlockEntityType<?> getType();

    @Inject(method = "isValidBlockState", at = @At("HEAD"), cancellable = true)
    private void isValidBlockState(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        boolean valid = this.getType().isValid(state);
        if (!valid) {
            LOGGER.warn("BlockEntity {} has invalid block state {} due to it's parent type isValid call", this, state);
        }
        cir.setReturnValue(valid);
    }
}
