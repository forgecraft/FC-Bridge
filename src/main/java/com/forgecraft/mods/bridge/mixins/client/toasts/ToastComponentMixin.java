package com.forgecraft.mods.bridge.mixins.client.toasts;

import com.forgecraft.mods.bridge.config.ClientConfig;
import net.minecraft.client.gui.components.toasts.*;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This is basically identical to how I archived this same feature in FTB Pack Companion but I have no idea how
 * I would have done it differently.
 */
@Mixin(ToastComponent.class)
public class ToastComponentMixin {
    @Inject(method = "addToast(Lnet/minecraft/client/gui/components/toasts/Toast;)V", at = @At("HEAD"), cancellable = true)
    public void addToast(Toast toast, CallbackInfo ci) {
        // Don't do anything if the Toast Control mod is present
        if (ModList.get().isLoaded("toastcontrol")) {
            return;
        }

        if (!ClientConfig.DISABLE_RECIPE_UNLOCK_TOAST.get() && !ClientConfig.DISABLE_ADVANCEMENT_TOAST.get()) {
            return;
        }

        // Just reject the toast if it's a tutorial toast
        if (toast instanceof TutorialToast) {
            ci.cancel();
        }

        if (toast instanceof RecipeToast && ClientConfig.DISABLE_RECIPE_UNLOCK_TOAST.get()) {
            ci.cancel();
        }

        if (toast instanceof AdvancementToast && ClientConfig.DISABLE_ADVANCEMENT_TOAST.get()) {
            ci.cancel();
        }
    }
}
