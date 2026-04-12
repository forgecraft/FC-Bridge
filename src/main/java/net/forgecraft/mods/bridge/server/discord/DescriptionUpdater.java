package net.forgecraft.mods.bridge.server.discord;

import com.google.common.hash.Hashing;
import net.forgecraft.mods.bridge.config.CommonConfig;
import com.google.gson.Gson;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public enum DescriptionUpdater {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionUpdater.class);

    private static final String SPL_DOWNLOAD = "https://github.com/forgecraft/ServerPackLocator/releases/";
    private static final String NEOFORGE_DOWNLOAD = "https://maven.neoforged.net/releases/net/neoforged/neoforge/{version}/neoforge-{version}-installer.jar";

    public void init() {
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
    }

    private void onServerStarted(ServerStartedEvent event) {
        event.getServer().execute(this::updateDescription);
        event.getServer().execute(this::updateModListChannel);
    }

    private void updateDescription() {
        String channelId = CommonConfig.DISCORD_CHANNEL_ID.get();
        if (channelId.isEmpty()) {
            LOGGER.info("Discord channel ID is empty, cannot update description");
            return;
        }

        // Get the needed data
        List<IModInfo> mods = ModList.get().getMods();

        // Neoforge version
        var minecraftVersion = modVersionOrUnknown("minecraft", mods);
        var neoforgeVersion = modVersionOrUnknown("neoforge", mods);
        var splVersion = splVersionFinder();
        var splDownloadVersion = splVersion.equals("latest") ? "latest" : "tag/v" + splVersion;

        StringBuilder description = new StringBuilder();
        description.append("Connect with ").append(bold(CommonConfig.DISCORD_CONNECT_IP.get())).append(" · ").append("\n");
        description.append("SPL Url: ").append(bold(CommonConfig.DISCORD_SPL_INFO.get())).append(" · ").append("\n");
        description.append("Modlist Url: ").append(bold(CommonConfig.DISCORD_MODLIST_INFO.get())).append(" · ").append("\n");
        description.append("\n");
        description.append("Minecraft: ").append(bold(minecraftVersion)).append(" · ").append("\n");
        description.append("Neoforge: ").append(bold(neoforgeVersion)).append(" · ").append("\n");
        description.append("SPL: ").append(bold(splVersion)).append("\n");
        description.append("\n");
        description.append("Neoforge: ").append(NEOFORGE_DOWNLOAD.replace("{version}", neoforgeVersion)).append("\n");
        description.append("SPL: ").append(SPL_DOWNLOAD).append(splDownloadVersion).append("\n");

        String originalDescription = "";
        String channelInfoRaw = sendDiscordReq("/channels/" + channelId, "GET", null);
        if (channelInfoRaw != null && channelInfoRaw.startsWith("{")) {
            originalDescription = new Gson().fromJson(channelInfoRaw, Map.class).get("topic").toString();
        }

        if (!Objects.equals(originalDescription.trim(), description.toString().trim())) {
            LOGGER.info("Updating description");
            LOGGER.info("Old description: {}", originalDescription);
            LOGGER.info("New description: {}", description);

            // Update the description
            sendDiscordReq("/channels/" + channelId, "PATCH", Map.of("topic", description.toString()));

            String diffString = createDiffFromDesc(originalDescription, description.toString());

            String updateMessage = "## Server information has been updated\n\n" + diffString;
            sendDiscordReq("/channels/" + channelId + "/messages", "POST", Map.of(
                    "content", updateMessage,
                    "flags", 1 << 2,
                    "components", List.of(
                            Map.of("type", 1, "components", List.of(
                                    Map.of("type", 2, "style", 5, "label", "Neoforge (" + neoforgeVersion + ")", "url", NEOFORGE_DOWNLOAD.replace("{version}", neoforgeVersion)),
                                    Map.of("type", 2, "style", 5, "label", "SPL (" + splDownloadVersion + ")", "url", SPL_DOWNLOAD + splDownloadVersion)
                            ))
                    )
            ));
        } else {
            LOGGER.info("Description is already up to date");
        }
    }

    private String createDiffFromDesc(String originalDescription, String newDescription) {
        var diffBuilder = new StringBuilder();
        diffBuilder.append("```diff\n");

        // Only add the lines that have actually changes so if the line is identical, we skip it
        var originalLines = originalDescription.split("\n");
        var newLines = newDescription.split("\n");

        if (originalLines.length != newLines.length) {
            diffBuilder.append("Description length mismatch\n");
            return diffBuilder.append("```").toString();
        }

        for (int i = 0; i < originalLines.length; i++) {
            if (!originalLines[i].equals(newLines[i])) {
                diffBuilder.append("- ").append(originalLines[i]).append("\n");
                diffBuilder.append("+ ").append(newLines[i]).append("\n");
            }
        }

        return diffBuilder.append("```").toString();
    }

    @Nullable
    private String sendDiscordReq(String endpoint, String method, @Nullable  Object body) {
        // Send the request to discord
        if (CommonConfig.DISCORD_TOKEN.get().isEmpty()) {
            LOGGER.info("Discord token is empty, cannot update description");
            return null;
        }

        // Send the request
        try (var client = HttpClient.newHttpClient()) {
            var jsonBody = new Gson().toJson(body);

            var requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create("https://discord.com/api/v10" + endpoint))
                    .header("Authorization", "Bot " + CommonConfig.DISCORD_TOKEN.get());

            if (body != null) {
                requestBuilder.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            var response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                if (response.statusCode() == 429) {
                    LOGGER.error("Failed to update description, rate limited");

                    // Log the rate limit headers
                    response.headers()
                            .map()
                            .entrySet()
                            .stream().filter(e -> e.getKey().contains("x-ratelimit"))
                            .forEach(e -> LOGGER.error("{}: {}", e.getKey(), e.getValue()));
                }

                LOGGER.error("Failed to update description, response code: {}", response.statusCode());
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to update description", e);
        }

        return null;
    }

    private String modVersionOrUnknown(String modid, List<IModInfo> mod) {
        return mod.stream()
                .filter(e -> e.getModId().equals(modid))
                .findFirst().map(e -> e.getVersion().toString())
                .orElse("Unknown");
    }

    private String splVersionFinder() {
        var modsDir = FMLPaths.MODSDIR.get();
        try (var mods = Files.list(modsDir)) {
            var mod = mods
                    .filter(e -> e.getFileName().toString().startsWith("serverpacklocator-"))
                    .findFirst();

            if (mod.isPresent()) {
                // Split at the -
                var split = mod.get().getFileName().toString().split("-");
                return split[1].replace(".jar", "");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to find SPL version", e);
        }

        return "latest";
    }

    private static String bold(String text) {
        return "**" + text + "**";
    }

    private void updateModListChannel() {
        String modListChannelId = CommonConfig.DISCORD_MODS_CHANNEL_ID.get();
        if (modListChannelId.isEmpty()) {
            LOGGER.info("Discord mod list channel ID is empty, cannot update mod list channel");
            return;
        }

        var lastHash = "";
        Path hashPath = FMLPaths.GAMEDIR.get().resolve(".modlist_hash");
        if (Files.exists(hashPath)) {
            try {
                lastHash = Files.readString(hashPath);
            } catch (IOException e) {
                LOGGER.error("Failed to read mod list hash", e);
            }
        }

        var newHash = "";
        var modsPath = FMLPaths.MODSDIR.get();
        try (var filesStream = Files.walk(modsPath)) {
            var hashBuilder = new StringBuilder();
            for (Path path : filesStream.toList()) {
                var fileSize = Files.size(path);
                var lastModified = Files.getLastModifiedTime(path).toMillis();
                hashBuilder.append(path.getFileName().toString()).append(fileSize).append(lastModified);
            }
            newHash = Hashing.sha256().hashString(hashBuilder.toString(), StandardCharsets.UTF_8).toString();
        } catch (IOException e) {
            LOGGER.error("Failed to read mods directory", e);
        }

        if (newHash.isEmpty()) {
            LOGGER.error("Failed to compute mod list hash, skipping mod list channel update");
            return;
        }

        if (newHash.equals(lastHash)) {
            LOGGER.info("Mod list has not changed, skipping mod list channel update");
            return;
        }

        // Write the new hash to the file
        try {
            Files.writeString(hashPath, newHash);
        } catch (IOException e) {
            LOGGER.error("Failed to write mod list hash", e);
        }

        sendDiscordReq("/channels/" + modListChannelId + "/messages", "POST", Map.of(
                "content", "## Mods pushed to server.\n\nAny mods after this message have not yet been pushed to the server",
                "flags", 1 << 2 // Suppress embeds and notifications
        ));
    }
}
