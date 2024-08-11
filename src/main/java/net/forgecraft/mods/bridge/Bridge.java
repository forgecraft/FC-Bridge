package net.forgecraft.mods.bridge;

import net.forgecraft.mods.bridge.commands.BridgeCommands;
import net.forgecraft.mods.bridge.config.ClientConfig;
import net.forgecraft.mods.bridge.config.CommonConfig;
import net.forgecraft.mods.bridge.config.ServerConfig;
import net.forgecraft.mods.bridge.contained.afk.AfkWatcher;
import net.forgecraft.mods.bridge.network.BridgeNetwork;
import net.forgecraft.mods.bridge.server.discord.DescriptionUpdater;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@Mod(Bridge.MODID)
public class Bridge {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bridge.class);

    public static final String MODID = "fcbridge";

    private static Path fcDataDir = null;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

//    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
//            .title(Component.translatable("itemGroup.examplemod"))
//            .withTabsBefore(CreativeModeTabs.COMBAT)
//            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
//            .displayItems((parameters, output) -> {
//                output.accept(EXAMPLE_ITEM.get());
//            }).build());

    public Bridge(IEventBus modEventBus, ModContainer modContainer) {
        fcDataDir = FMLPaths.GAMEDIR.get().resolve("forgecraft");

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        modEventBus.addListener(this::gameReady);

        modEventBus.addListener(BridgeNetwork::register);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(BridgeCommands::register);

//        InventorySnapshots.INSTANCE.init(modEventBus);
    }

    @SubscribeEvent
    private void gameReady(final FMLLoadCompleteEvent event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            DescriptionUpdater.INSTANCE.init();
            AfkWatcher.INSTANCE.init();
        }
    }

    @NotNull
    public static Path getFcDataDir() {
        return fcDataDir;
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
