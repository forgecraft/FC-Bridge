//package com.forgecraft.mods.bridge.commands;
//
//import com.forgecraft.mods.bridge.utils.lang.LanguageKeys;
//import com.mojang.brigadier.arguments.StringArgumentType;
//import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//import com.mojang.brigadier.context.CommandContext;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
//import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
//import com.mojang.brigadier.suggestion.SuggestionProvider;
//import net.minecraft.ChatFormatting;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.commands.Commands;
//import net.minecraft.commands.SharedSuggestionProvider;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.GlobalPos;
//import net.minecraft.network.chat.ClickEvent;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.Style;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.server.level.ServerPlayer;
//
//import java.util.LinkedList;
//
//public class HomeCommand {
//    private static final DynamicCommandExceptionType DIMENSION_MISMATCH = new DynamicCommandExceptionType(obj -> Component.translatable(LanguageKeys.command("home.level_missing"), obj));
//    private static final DynamicCommandExceptionType HOME_MISSING = new DynamicCommandExceptionType(obj -> Component.translatable(LanguageKeys.command("home.missing"), obj));
//    private static final SimpleCommandExceptionType NO_HOMES = new SimpleCommandExceptionType(Component.translatable(LanguageKeys.command("home.no_homes")));
//
//    public static LiteralArgumentBuilder<CommandSourceStack> register() {
//        return Commands.literal("home")
//                .then(Commands.literal("add")
//                        .then(Commands.argument("name", StringArgumentType.greedyString())
//                                .executes(ctx -> addHome(ctx, StringArgumentType.getString(ctx, "name")))))
//                .then(Commands.literal("remove")
//                        .then(Commands.argument("name", StringArgumentType.greedyString())
//                                .suggests(homeSuggestionProvider())
//                                .executes(ctx -> removeHome(ctx, StringArgumentType.getString(ctx, "name")))))
//                .then(Commands.literal("list").executes(HomeCommand::listHomes))
//                .then(Commands.literal("clear-all").executes(HomeCommand::clearAllHomes))
//                .then(Commands.argument("name", StringArgumentType.greedyString())
//                        .suggests(homeSuggestionProvider())
//                        .executes(ctx -> goHome(ctx, StringArgumentType.getString(ctx, "name"))));
//    }
//
//    private static int clearAllHomes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
//        var player = ctx.getSource().getPlayerOrException();
//        PlayerData.get(player).homes().clear();
//        PlayerData.get(player).save(player);
//
//        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.clear_all")), false);
//
//        return 0;
//    }
//
//    private static int goHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
//        var player = ctx.getSource().getPlayerOrException();
//        var home = findHome(player, name);
//
//        teleportPlayer(player, home);
//        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.teleport"), home.name()), false);
//        return 0;
//    }
//
//    private static int addHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
//        var player = ctx.getSource().getPlayerOrException();
//        var newHome = Home.create(name, player);
//
//        PlayerData playerData = PlayerData.get(player);
//        playerData.addHome(newHome);
//        playerData.save(player);
//
//        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.added"), newHome.name()), false);
//
//        return 0;
//    }
//
//    private static int removeHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
//        var player = ctx.getSource().getPlayerOrException();
//
//        PlayerData playerData = PlayerData.get(player);
//        LinkedList<Home> homes = playerData.homes();
//        var home = homes.stream().filter(h -> h.name().equalsIgnoreCase(name.toLowerCase())).findFirst();
//        if (home.isEmpty()) {
//            throw HOME_MISSING.create(name);
//        }
//
//        Home foundHome = home.get();
//        homes.remove(foundHome);
//        playerData.save(player);
//
//        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.remove"), foundHome.name()), false);
//
//        return 0;
//    }
//
//    private static int listHomes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
//        var player = ctx.getSource().getPlayerOrException();
//        var homes = PlayerData.get(player).homes();
//
//        if (homes.isEmpty()) {
//            throw NO_HOMES.create();
//        }
//
//        ctx.getSource().sendSuccess(() -> Component.translatable(LanguageKeys.command("home.size"), homes.size()), false);
//
//        // Create a map of dimensions to homes
//        var dimensionMap = new HashMap<ResourceLocation, LinkedList<Home>>();
//        homes.forEach(home -> {
//            var dimensionHomes = dimensionMap.getOrDefault(home.pos().dimension().location(), new LinkedList<>());
//            dimensionHomes.add(home);
//            dimensionMap.put(home.pos().dimension().location(), dimensionHomes);
//        });
//
//        dimensionMap.forEach((dim, innerHomes) -> {
//            ctx.getSource().sendSuccess(() -> Component.literal("%s/%s".formatted(dim.getNamespace(), dim.getPath())).withStyle(ChatFormatting.GOLD), false);
//            innerHomes.forEach(home -> {
//                BlockPos location = home.pos().pos();
//
//                var component = Component.literal("");
//                component.append(Component.literal(home.name()).setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fc home %s".formatted(home.name())))));
//                component.append(Component.literal(" ("));
//                component.append(Component.literal(location.toShortString()).withStyle(Style.EMPTY.withUnderlined(true).withColor(ChatFormatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp %s %s %s %s".formatted(player.getName().getString(), location.getX(), location.getY(), location.getZ())))));
//                component.append(Component.literal(")"));
//
//                ctx.getSource().sendSuccess(() -> component, false);
//            });
//        });
//
//        return 0;
//    }
//
//    private static Home findHome(ServerPlayer player, String homeName) throws CommandSyntaxException {
//        var home = PlayerData.get(player).getHome(homeName);
//        if (home.isEmpty()) {
//            throw HOME_MISSING.create(homeName);
//        }
//
//        return home.get();
//    }
//
//    private static void teleportPlayer(ServerPlayer player, Home home) throws CommandSyntaxException {
//        var vehicle = player.getVehicle();
//
//        if (vehicle != null) {
//            player.stopRiding();
//        }
//
//        GlobalPos pos = home.pos();
//        ServerLevel playerLevel = player.server.getLevel(pos.dimension());
//        if (playerLevel == null) {
//            throw DIMENSION_MISMATCH.create(pos.dimension().location());
//        }
//
//        // Store XP
//        var xp = player.experienceLevel;
//        player.teleportTo(playerLevel, pos.pos().getX() + .5D, pos.pos().getY() + .1D, pos.pos().getZ() + .5D, home.yaw(), home.pitch());
//        player.setExperienceLevels(xp);
//    }
//
//    public static SuggestionProvider<CommandSourceStack> homeSuggestionProvider() {
//        return (ctx, builder) -> {
//            SharedSuggestionProvider.suggest(PlayerData
//                    .get(ctx.getSource().getPlayerOrException())
//                    .homes()
//                    .stream()
//                    .map(Home::name), builder
//            );
//
//            return builder.buildFuture();
//        };
//    }
//}
