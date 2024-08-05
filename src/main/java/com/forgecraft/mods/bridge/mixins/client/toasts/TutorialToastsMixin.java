package com.forgecraft.mods.bridge.mixins.client.toasts;

import net.minecraft.client.tutorial.Tutorial;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tutorial.class)
public abstract class TutorialToastsMixin {
    @Shadow public abstract void stop();

    /**
     * Stop the tutorial from starting as soon as the game starts
     */
    @Inject(at = @At("HEAD"), method = "start()V", cancellable = true)
    private void start(CallbackInfo info) {
        // Don't do anything if the Toast Control mod is present
        if (ModList.get().isLoaded("toastcontrol")) {
            return;
        }

        this.stop();
        info.cancel();
    }

    /**
     * No point in ticking the tutorial
     */
    @Inject(at = @At("HEAD"), method = "tick()V", cancellable = true)
    private void tick(CallbackInfo info) {
        // Don't do anything if the Toast Control mod is present
        if (ModList.get().isLoaded("toastcontrol")) {
            return;
        }

        info.cancel();
    }
}
