package net.forgecraft.mods.bridge.mixins.client;

import net.forgecraft.mods.bridge.contained.misc.FeatureDingAtLaunch;
import net.forgecraft.mods.bridge.contained.misc.FeatureLaunchTimeToast;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    public void onGameLoadFinished(CallbackInfo ci) {
        FeatureLaunchTimeToast.createToast();
        FeatureDingAtLaunch.ding();
    }
}
