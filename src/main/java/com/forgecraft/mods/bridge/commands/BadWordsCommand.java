package com.forgecraft.mods.bridge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * TODO: Opt in/out of bad words filter and store in the server data
 */
public class BadWordsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("badwords")
                .then(Commands.literal("opt-in").executes(ctx -> optIn(ctx.getSource())))
                .then(Commands.literal("opt-out").executes(ctx -> optOut(ctx.getSource())));
    }

    private static int optIn(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Not implemented yet"), false);
        return 0;
    }

    private static int optOut(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Not implemented yet"), false);
        return 0;
    }
}
