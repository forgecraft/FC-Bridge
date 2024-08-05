package com.forgecraft.mods.bridge.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class InventoryBackupCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("inv-snapshots")
                .then(Commands.literal("get").then(
                        Commands.argument("player", EntityArgument.player()).executes(context -> getSnapshots(context.getSource(), EntityArgument.getPlayer(context, "player")))
                ))
                .then(Commands.literal("restore").then(
                        Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("snapshot-id", StringArgumentType.string())).executes(context -> restoreSnapshot(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "snapshot-id")))
                ));
    }

    private static int getSnapshots(CommandSourceStack source, Player player) {
        return 0;
    }

    private static int restoreSnapshot(CommandSourceStack source, ServerPlayer player, String string) {
        return 0;
    }
}
