package net.forgecraft.mods.bridge.client.screens;

import net.forgecraft.mods.bridge.config.ClientConfig;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClientSettingsScreen extends Screen {
    protected ClientSettingsScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Checkbox.builder(Component.translatable("fc.bridge.gui.toggle_recipe_toasts"), this.font)
                .pos(this.width / 2 - 100, this.height / 2 - 10)
                .selected(!ClientConfig.DISABLE_RECIPE_UNLOCK_TOAST.get())
                .onValueChange((button, value) -> {
                    // Do something
                    ClientConfig.DISABLE_RECIPE_UNLOCK_TOAST.set(!value);
                    ClientConfig.DISABLE_RECIPE_UNLOCK_TOAST.save();
                })
                .build()
        );

        this.addRenderableWidget(Checkbox.builder(Component.translatable("fc.bridge.gui.toggle_advancement_toasts"), this.font)
                .pos(this.width / 2 - 100, this.height / 2 + 10)
                .selected(!ClientConfig.DISABLE_ADVANCEMENT_TOAST.get())
                .onValueChange((button, value) -> {
                    // Do something
                    ClientConfig.DISABLE_ADVANCEMENT_TOAST.set(!value);
                    ClientConfig.DISABLE_ADVANCEMENT_TOAST.save();
                })
                .build()
        );
    }
}
