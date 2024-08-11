package net.forgecraft.mods.bridge.contained.misc;

import net.forgecraft.mods.bridge.client.ClientUtils;
import net.forgecraft.mods.bridge.mixins.client.MinecraftMixin;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

import java.lang.management.ManagementFactory;

/**
 * Just a tidy home for the creation of the launch time toast.
 * (Called from the {@link MinecraftMixin})
 */
public class FeatureLaunchTimeToast {
    public static void createToast() {
        // Get the launch time by looking up the runtime bean
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        // Convert the milliseconds to seconds
        long seconds = uptime / 1000;

        // Round to first 4 decimal places
        String secondsString = String.format("%.2f", (double) seconds);

        // Create the toast
        var toast = new SystemToast(
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.translatable("fc.bridge.toast.launch_time.title"),
                Component.translatable("fc.bridge.toast.launch_time", secondsString)
        );

        ClientUtils.getMinecraft().ifPresent(minecraft -> minecraft.getToasts().addToast(toast));
    }
}
