package com.forgecraft.mods.bridge.data;

import com.forgecraft.mods.bridge.Bridge;
import com.forgecraft.mods.bridge.utils.lang.LanguageKeys;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class BridgeLanguage extends LanguageProvider {
    public BridgeLanguage(PackOutput output) {
        super(output, Bridge.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        gui("toggle_recipe_toasts", "Toggle Recipe Toasts");
        gui("toggle_advancement_toasts", "Toggle Advancement Toasts");

        prefixed("toast.launch_time.title", "Launched in");
        prefixed("toast.launch_time", "%s seconds");

        command("sudo.not_allowed", "You are not allowed to use [%s] with sudo");

        // Home
        command("home.level_missing", "The dimension [%s] of this home seems to no longer exist...");
        command("home.missing", "The home [%s] does not exist");
        command("home.clear_all", "All homes removed!");
        command("home.teleport", "Teleported to home [%s]!");
        command("home.added", "Home [%s] added!");
        command("home.no_homes", "No homes set!");
        command("home.size", "Homes: %s");
        command("home.remove", "Home [%s] removed!");

        command("spectator.enter", "%s is now in spectator mode");
        command("spectator.exit", "%s is no longer in spectator mode");
    }

    public void gui(String key, String value) {
        prefixed(LanguageKeys.gui(key), value);
    }

    public void command(String key, String value) {
        prefixed(LanguageKeys.command(key), value);
    }

    public void prefixed(String key, String value) {
        this.add(LanguageKeys.prefixed(key), value);
    }
}
