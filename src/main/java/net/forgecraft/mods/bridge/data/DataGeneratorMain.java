package net.forgecraft.mods.bridge.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DataGeneratorMain {
    @SubscribeEvent
    public static void onGather(GatherDataEvent event) {
        var generator = event.getGenerator();
        var pack = event.getGenerator().getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();

        if (event.includeClient()) {
            generator.addProvider(true, new BridgeLanguage(pack));
        }
    }
}
