package net.forgecraft.mods.bridge.commands;

import net.forgecraft.mods.bridge.mixins.client.accessors.TeleportCommandMixin;
import net.forgecraft.mods.bridge.storage.PlayerStorage;
import net.forgecraft.mods.bridge.storage.player.Home;
import net.forgecraft.mods.bridge.storage.player.PlayerData;
import net.forgecraft.mods.bridge.utils.lang.LanguageKeys;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class HomeCommand {
    private static final DynamicCommandExceptionType DIMENSION_MISMATCH = new DynamicCommandExceptionType(obj -> Component.translatable(LanguageKeys.command("home.level_missing"), obj));
    private static final DynamicCommandExceptionType HOME_MISSING = new DynamicCommandExceptionType(obj -> Component.translatable(LanguageKeys.command("home.missing"), obj));
    private static final SimpleCommandExceptionType NO_HOMES = new SimpleCommandExceptionType(Component.translatable(LanguageKeys.command("home.no_homes")));
    private static final SimpleCommandExceptionType HOME_EXISTS = new SimpleCommandExceptionType(Component.translatable(LanguageKeys.command("home.exists")));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("homes")
                .then(Commands.literal("add")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> addHome(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(homeSuggestionProvider())
                                .executes(ctx -> removeHome(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("list").executes(HomeCommand::listHomes))
                .then(Commands.literal("clear-all").executes(HomeCommand::clearAllHomes));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerHome() {
        return Commands.literal("home")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .suggests(homeSuggestionProvider())
                        .executes(ctx -> goHome(ctx, StringArgumentType.getString(ctx, "name"))));
    }

    private static int clearAllHomes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();

        MinecraftServer server = ctx.getSource().getServer();
        PlayerStorage.getOrCreate(server).forPlayer(player).homes().clear();
        PlayerStorage.setDirty(server);

        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.clear_all")), false);

        return 0;
    }

    private static int goHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var home = findHome(player, name);

        teleportPlayer(ctx.getSource(), home);
        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.teleport"), home.name()), false);
        return 0;
    }

    private static int addHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();

        PlayerData playerData = PlayerStorage.getOrCreate(ctx.getSource().getServer()).forPlayer(player);
        LinkedList<Home> homes = playerData.homes();

        var homeExists = homes.stream().anyMatch(h -> h.name().equalsIgnoreCase(name.toLowerCase()));
        if (homeExists) {
            throw HOME_EXISTS.create();
        }

        var newHome = Home.create(player, name, "", player.blockPosition());

        playerData.homes().add(newHome);
        PlayerStorage.setDirty(ctx.getSource().getServer());

        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.added"), newHome.name()), false);

        return 0;
    }

    private static int removeHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();

        PlayerData playerData = PlayerStorage.getOrCreate(ctx.getSource().getServer()).forPlayer(player);
        LinkedList<Home> homes = playerData.homes();

        var home = homes.stream().filter(h -> h.name().equalsIgnoreCase(name.toLowerCase())).findFirst();
        if (home.isEmpty()) {
            throw HOME_MISSING.create(name);
        }

        Home foundHome = home.get();
        homes.remove(foundHome);
        PlayerStorage.setDirty(ctx.getSource().getServer());

        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.remove"), foundHome.name()), false);

        return 0;
    }

    private static int listHomes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var homes = PlayerStorage.getOrCreate(ctx.getSource().getServer()).forPlayer(player).homes();

        if (homes.isEmpty()) {
            throw NO_HOMES.create();
        }

        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.size"), homes.size()), false);

        // Create a map of dimensions to homes
        var dimensionMap = new HashMap<Identifier, LinkedList<Home>>();
        homes.forEach(home -> {
            var dimensionHomes = dimensionMap.getOrDefault(home.pos().dimension().identifier(), new LinkedList<>());
            dimensionHomes.add(home);
            dimensionMap.put(home.pos().dimension().identifier(), dimensionHomes);
        });

        dimensionMap.forEach((dim, innerHomes) -> {
            ctx.getSource().sendSuccess(() -> Component.literal("%s/%s".formatted(dim.getNamespace(), dim.getPath())).withStyle(ChatFormatting.GOLD), false);
            innerHomes.forEach(home -> {
                BlockPos location = home.pos().pos();

                var component = Component.literal("");
                component.append(Component.literal(home.name()).setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/fc home %s".formatted(home.name())))));
                component.append(Component.literal(" ("));
                component.append(Component.literal(location.toShortString()).withStyle(Style.EMPTY.withUnderlined(true).withColor(ChatFormatting.GRAY).withClickEvent(new ClickEvent.SuggestCommand("/tp %s %s %s %s".formatted(player.getName().getString(), location.getX(), location.getY(), location.getZ())))));
                component.append(Component.literal(")"));

                ctx.getSource().sendSuccess(() -> component, false);
            });
        });

        return 0;
    }

    private static Home findHome(ServerPlayer player, String homeName) throws CommandSyntaxException {
        var playerData = PlayerStorage.getOrCreate(player.level().getServer()).forPlayer(player);

        var home = playerData.homes().stream()
                .filter(h -> h.name().equalsIgnoreCase(homeName.toLowerCase()))
                .findFirst();

        if (home.isEmpty()) {
            throw HOME_MISSING.create(homeName);
        }

        return home.get();
    }

    private static void teleportPlayer(CommandSourceStack stack, Home home) throws CommandSyntaxException {
        var player = stack.getPlayerOrException();
        var vehicle = player.getVehicle();

        if (vehicle != null) {
            player.stopRiding();
        }

        GlobalPos pos = home.pos();
        ServerLevel targetLevel = player.level().getServer().getLevel(pos.dimension());
        if (targetLevel == null) {
            throw DIMENSION_MISMATCH.create(pos.dimension().identifier());
        }

        // Store XP
        var xp = player.experienceLevel;
        TeleportCommandMixin.fcbridge$performTeleport(stack, player, targetLevel,
                pos.pos().getX() + .5D, pos.pos().getY() + .1D, pos.pos().getZ() + .5D,
                Set.of(), home.yaw(), home.pitch(), null);
        player.setExperienceLevels(xp);
    }

    public static SuggestionProvider<CommandSourceStack> homeSuggestionProvider() {
        return (ctx, builder) -> {
            var player = ctx.getSource().getPlayerOrException();

            SharedSuggestionProvider.suggest(PlayerStorage.getOrCreate(player.level().getServer()).forPlayer(player)
                    .homes()
                    .stream()
                    .map(Home::name), builder
            );

            return builder.buildFuture();
        };
    }
}
