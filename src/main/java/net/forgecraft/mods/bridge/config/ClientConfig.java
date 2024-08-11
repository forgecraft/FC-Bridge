package net.forgecraft.mods.bridge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DISABLE_RECIPE_UNLOCK_TOAST = BUILDER
            .comment("Disables recipe toasts from showing when recipes are unlocked")
            .define("alterations.toasts.disableRecipeUnlock", true);

    public static final ModConfigSpec.BooleanValue DISABLE_ADVANCEMENT_TOAST = BUILDER
            .comment("Disables advancement toasts from showing when advancements are achieved")
            .define("alterations.toasts.disableAdvancementToast", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
