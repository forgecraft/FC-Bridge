package net.forgecraft.mods.bridge.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber()
public class DataGeneratorMain {
    @SubscribeEvent
    public static void onGather(GatherDataEvent.Client event) {
        var generator = event.getGenerator();
        var pack = event.getGenerator().getPackOutput();

        generator.addProvider(true, new BridgeLanguage(pack));
    }
}
