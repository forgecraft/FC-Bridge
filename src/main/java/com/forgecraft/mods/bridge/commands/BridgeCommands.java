package com.forgecraft.mods.bridge.commands;

import com.google.common.eventbus.Subscribe;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class BridgeCommands {
    @Subscribe
    public static void register(RegisterCommandsEvent event) {
        var baseCommand = Commands.literal("fc");

        var debugCommands = Commands.literal("dev")
                .then(SudoCommand.register())
                .then(ShowCommand.register())
                .then(SpectatorCommand.register());

//        var adminCommands = Commands.literal("admin")
//                .then(InventoryBackupCommand.register());

        baseCommand
                .then(debugCommands)
//                .then(adminCommands)
                .then(ShareLocationCommand.register())
                .then(BadWordsCommand.register());
//                .then(HomeCommand.register());

        event.getDispatcher().register(baseCommand);
    }
}
