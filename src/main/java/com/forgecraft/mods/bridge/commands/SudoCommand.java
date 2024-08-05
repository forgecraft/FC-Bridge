package com.forgecraft.mods.bridge.commands;

import com.forgecraft.mods.bridge.config.ServerConfig;
import com.forgecraft.mods.bridge.utils.lang.LanguageKeys;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class SudoCommand {
    private static final DynamicCommandExceptionType COMMAND_NOT_ALLOWED
            = new DynamicCommandExceptionType(obj -> Component.translatable(LanguageKeys.command("sudo.not_allowed"), obj));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("sudo")
                .then(Commands.argument("command", StringArgumentType.greedyString()).suggests(SudoCommand::suggester)
                        .executes(SudoCommand::executeCommand));
    }

    private static int executeCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
        var sudoSourceStack = createSudoStack(commandSourceStackCommandContext.getSource().getPlayerOrException());
        var command = StringArgumentType.getString(commandSourceStackCommandContext, "command");
        if (command.startsWith("/")) command = command.substring(1);

        // Ensure it's an allowed command
        for (var allowedCommand : ServerConfig.ALLOWED_SUDO_COMMANDS.get()) {
            if (!command.startsWith(allowedCommand)) {
                throw COMMAND_NOT_ALLOWED.create(command);
            }
        }

        sudoSourceStack.getServer().getCommands().performPrefixedCommand(sudoSourceStack, command);

        return 0;
    }

    private static CompletableFuture<Suggestions> suggester(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        for (var command : ServerConfig.ALLOWED_SUDO_COMMANDS.get()) {
            suggestionsBuilder.suggest(command);
        }

        return suggestionsBuilder.buildFuture();
    }

    private static CommandSourceStack createSudoStack(ServerPlayer player) {
        var sudoSource = new SudoCommandSource(player);

        return new CommandSourceStack(
                sudoSource,
                player.position(),
                player.getRotationVector(),
                (ServerLevel) player.level(),
                Commands.LEVEL_OWNERS,
                player.getName().getString(),
                player.getName(),
                player.level().getServer(),
                sudoSource.playerDelegate
        );
    }

    private static class SudoCommandSource implements CommandSource {
        public ServerPlayer playerDelegate;

        public SudoCommandSource(ServerPlayer player) {
            this.playerDelegate = player;
        }

        @Override
        public void sendSystemMessage(Component pComponent) {
            this.playerDelegate.sendSystemMessage(pComponent);
        }

        @Override
        public boolean acceptsSuccess() {
            return this.playerDelegate.acceptsSuccess();
        }

        @Override
        public boolean acceptsFailure() {
            return this.playerDelegate.acceptsFailure();
        }

        @Override
        public boolean shouldInformAdmins() {
            return true;
        }
    }
}
