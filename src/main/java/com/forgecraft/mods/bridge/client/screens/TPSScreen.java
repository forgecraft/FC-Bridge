package com.forgecraft.mods.bridge.client.screens;

import com.forgecraft.mods.bridge.client.BridgeClientData;
import com.forgecraft.mods.bridge.structs.TickTimeHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.HashMap;

public class TPSScreen extends Screen {
    private static final DecimalFormat TIME_FORMATTER = new DecimalFormat("########0.000");
    private Instant lastRequest = Instant.now();

    public TPSScreen() {
        super(Component.literal("TPS Screen"));
        requestTPSData(); // Request data when the screen is created
    }

    private void requestTPSData() {
        BridgeClientData.INSTANCE.requestServerTpsUpdate();
        System.out.printf("Requested TPS data at %s%n", Instant.now().toString());
    }

    @Override
    public void onClose() {
        // Clean up the memory when the screen is closed
        BridgeClientData.INSTANCE.clearServerTps();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        // Every second, use the render method to request new data
        if (Instant.now().getEpochSecond() - lastRequest.getEpochSecond() >= 1) {
            requestTPSData();
            lastRequest = Instant.now();
        }

        HashMap<ResourceLocation, TickTimeHolder> serverTps = BridgeClientData.INSTANCE.getServerTps();
        if (serverTps.isEmpty()) {
            pGuiGraphics.drawString(this.font, "No data", 10, 10, 0xFFFFFF);
            return;
        }

        int y = 10;
        for (ResourceLocation dimension : serverTps.keySet()) {
            TickTimeHolder tps = serverTps.get(dimension);
            pGuiGraphics.drawString(this.font, dimension.toString(), 10, y, 0xFFFFFF);
            pGuiGraphics.drawString(this.font, "Mean tick time: " + TIME_FORMATTER.format(tps.meanTickTime()) + "ms", 10, y + 10, 0xFFFFFF);
            pGuiGraphics.drawString(this.font, "Mean TPS: " + TIME_FORMATTER.format(tps.meanTPS()), 10, y + 20, 0xFFFFFF);
            y += 30;
        }
    }
}
