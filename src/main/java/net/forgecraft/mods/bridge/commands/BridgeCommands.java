package net.forgecraft.mods.bridge.commands;

import com.google.common.eventbus.Subscribe;
import com.mojang.brigadier.RedirectModifier;
import net.forgecraft.mods.bridge.structs.ScreenType;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class BridgeCommands {
    @Subscribe
    public static void register(RegisterCommandsEvent event) {
        //        var adminCommands = Commands.literal("admin")
//                .then(InventoryBackupCommand.register());

        var homeCommand = HomeCommand.registerHome();

        var baseCommand = Commands.literal("fc")
                .then(new ShowCommand("tps", ScreenType.TPS).register())
                .then(new ShowCommand("client_settings", ScreenType.CLIENT_SETTINGS).register())
                .then(SudoCommand.register())
                .then(SpectatorCommand.register())
//                .then(adminCommands)
                .then(ShareLocationCommand.register())
                .then(BadWordsCommand.register())
                .then(HomeCommand.register())
                .then(homeCommand);

        // Add a root /home command that maps to /fc home for convenience
        event.getDispatcher().register(homeCommand);
        event.getDispatcher().register(baseCommand);
    }
}
