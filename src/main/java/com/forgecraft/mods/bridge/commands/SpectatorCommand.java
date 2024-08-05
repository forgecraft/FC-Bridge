package com.forgecraft.mods.bridge.commands;

import com.forgecraft.mods.bridge.utils.lang.BridgeCommon;
import com.forgecraft.mods.bridge.utils.lang.LanguageKeys;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

/**
 * Wrapper around the normal gamemode functionality but locked to just the spectator mode.
 * <p>
 * TODO: Store players location and rotation before going into spectator mode and restore it when they leave.
 */
public class SpectatorCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("spectator")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    var beforeGameMode = player.gameMode.getGameModeForPlayer();
                    var afterGameMode = beforeGameMode == GameType.SPECTATOR ? GameType.SURVIVAL : GameType.SPECTATOR;

                    // Allow admins to enter spectator mode quietly
                    if (!player.hasPermissions(Commands.LEVEL_ADMINS)) {
                        if (beforeGameMode == GameType.SPECTATOR) {
                            ctx.getSource().sendSystemMessage(Component.translatable(LanguageKeys.command("spectator.enter")).withStyle(BridgeCommon.WISPER_STYLE));
                        } else {
                            ctx.getSource().sendSystemMessage(Component.translatable(LanguageKeys.command("spectator.exit")).withStyle(BridgeCommon.WISPER_STYLE));
                        }
                    }

                    player.setGameMode(afterGameMode);

                    return 0;
                });
    }
}
