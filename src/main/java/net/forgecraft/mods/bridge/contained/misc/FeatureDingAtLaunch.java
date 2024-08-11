package net.forgecraft.mods.bridge.contained.misc;

import net.forgecraft.mods.bridge.client.ClientUtils;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.fml.ModList;

/**
 * Plays a fun little ding sound when the game starts. Credits to iChun for the idea I stole <3
 * @see <a href="https://github.com/iChun/Ding">iChun's Ding</a>
 */
public class FeatureDingAtLaunch {
    public static void ding() {
        // Don't play the sound if the Ding mod is present
        if (ModList.get().isLoaded("ding")) {
            return;
        }

        ClientUtils.getMinecraft().ifPresent(minecraft -> minecraft.getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F
        )));
    }
}
