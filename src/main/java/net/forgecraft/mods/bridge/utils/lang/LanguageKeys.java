package net.forgecraft.mods.bridge.utils.lang;

public class LanguageKeys {
    private static final String PREFIX = "fc.bridge.";
    private static final String GUI_PREFIX = "gui.";
    private static final String COMMAND_PREFIX = "commands.";

    public static String prefixed(String key) {
        return PREFIX + key;
    }

    public static String gui(String key) {
        return PREFIX + GUI_PREFIX + key;
    }

    public static String command(String key) {
        return PREFIX + COMMAND_PREFIX + key;
    }
}
