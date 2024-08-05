package com.forgecraft.mods.bridge.commands;

import com.forgecraft.mods.bridge.structs.ScreenTypes;
import com.forgecraft.mods.bridge.network.ShowScreenPacket;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.command.EnumArgument;

public class ShowCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("show")
                .then(Commands.argument("type", EnumArgument.enumArgument(ScreenTypes.class))
                        .executes(ShowCommand::sendOpenScreenPacket));
    }

    private static int sendOpenScreenPacket(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var type = ScreenTypes.valueOf(context.getArgument("type", ScreenTypes.class).toString());
        PacketDistributor.sendToPlayer(context.getSource().getPlayerOrException(), new ShowScreenPacket(type.getLocation()));
        return 1;
    }
}
