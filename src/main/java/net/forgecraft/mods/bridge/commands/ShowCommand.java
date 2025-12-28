package net.forgecraft.mods.bridge.commands;

import net.forgecraft.mods.bridge.structs.ScreenType;
import net.forgecraft.mods.bridge.network.ShowScreenPacket;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.network.PacketDistributor;

public record ShowCommand(String name, ScreenType screenType) {
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal(name).executes(this::sendOpenScreenPacket);
    }

    private int sendOpenScreenPacket(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PacketDistributor.sendToPlayer(context.getSource().getPlayerOrException(), new ShowScreenPacket(screenType));
        return 1;
    }
}
