package com.forgecraft.mods.bridge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue AFK_CHECKER_ENABLED = BUILDER
            .comment("Enables the AFK checker", "If enabled, the tab list will show AFK players after a certain amount of time")
            .define("afk.enable", true);

    public static final ModConfigSpec.IntValue AFK_CHECKER_TIME = BUILDER
            .comment("The amount of time in seconds before a layer is considered AFK", "This is only used if the AFK checker is enabled")
            .comment("This value is in seconds", "The default is 300 seconds (5 minutes)")
            .defineInRange("afk.time", 300, 30, Integer.MAX_VALUE); // Default 5 minutes

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_SUDO_COMMANDS = BUILDER
            .comment("A list of commands that can be used with the sudo command")
            .comment("Each command is used with a 'Starts With' check meaning arguments can be used but are not required")
            .comment("Do not include the '/' in the command")
            .defineList("commands.sudo.allowed", new ArrayList<>(), it -> it instanceof String);

    public static ModConfigSpec.ConfigValue<String> DISCORD_TOKEN = BUILDER
            .comment("The token for the Discord bot")
            .define("discord.token", "");

    public static ModConfigSpec.ConfigValue<String> DISCORD_CHANNEL_ID = BUILDER
            .comment("The channel ID for the Discord bot")
            .define("discord.channel_id", "");

    public static ModConfigSpec.ConfigValue<String> DISCORD_CONNECT_IP = BUILDER
            .comment("The IP address to connect to the Discord bot")
            .define("discord.connect_using", "one.forgecraft.net");

    public static ModConfigSpec.ConfigValue<String> DISCORD_SPL_INFO = BUILDER
            .comment("The SPL info for connecting to the server")
            .define("discord.spl_info", "one.forgecraft.net");

    public static ModConfigSpec.ConfigValue<String> DISCORD_MODLIST_INFO = BUILDER
            .comment("The modlist info for connecting to the server")
            .define("discord.modlist_info", "one.forgecraft.net");

    public static final ModConfigSpec SPEC = BUILDER.build();
}
