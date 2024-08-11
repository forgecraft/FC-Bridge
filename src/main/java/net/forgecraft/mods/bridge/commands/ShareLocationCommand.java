package net.forgecraft.mods.bridge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

/**
 * TODO: Open a GUI via a command to trigger a screen to show that will provide options:
 * - Add to FTB Chunks
 * - Add to JourneyMap
 * - Add to ZeonsMap
 */
public class ShareLocationCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("share-location")
                .executes(ShareLocationCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        BlockPos blockPos = player.blockPosition();
        var text = player.getDisplayName().copy().append(" is at ")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName().getString() + " " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ())))
                .append(Component.literal("[x: " + blockPos.getX() + " y: " + blockPos.getY() + " z: " + blockPos.getZ() + "]").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" in "))
                .append(Component.literal(player.level().dimension().location().toString()).withStyle(ChatFormatting.GRAY));

        player.getServer().getPlayerList().broadcastSystemMessage(text, false);
        return 0;
    }
}
