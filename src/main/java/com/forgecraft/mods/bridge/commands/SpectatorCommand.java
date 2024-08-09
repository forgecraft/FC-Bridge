package com.forgecraft.mods.bridge.commands;

import com.forgecraft.mods.bridge.storage.ServerStorage;
import com.forgecraft.mods.bridge.structs.DimensionalPos;
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
                        ctx.getSource().sendSystemMessage(Component.translatable(LanguageKeys.command(beforeGameMode == GameType.SPECTATOR ? "spectator.enter" : "spectator.exit")).withStyle(BridgeCommon.WISPER_STYLE));
                    }

                    DimensionalPos resetPos = null;
                    ServerStorage serverData = ServerStorage.getOrCreate(ctx.getSource().getServer());

                    if (beforeGameMode == GameType.SPECTATOR) {
                        resetPos = serverData.getSpectatorLocation(player.getUUID());
                    } else {
                        serverData.setSpectatorLocation(player.getUUID(), DimensionalPos.of(player));
                    }

                    player.setGameMode(afterGameMode);
                    if (resetPos != null) {
                        serverData.removeSpectatorLocation(player.getUUID());
                        var level = player.getServer().getLevel(resetPos.dimension());
                        if (level != null) {
                            player.teleportTo(level, resetPos.pos().getX(), resetPos.pos().getY(), resetPos.pos().getZ(), player.getYRot(), player.getXRot());
                        }
                    }

                    return 0;
                });
    }
}
