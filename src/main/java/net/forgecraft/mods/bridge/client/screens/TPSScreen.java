package net.forgecraft.mods.bridge.client.screens;

import net.forgecraft.mods.bridge.client.BridgeClientData;
import net.forgecraft.mods.bridge.client.screens.TPSScreen.TPSInformationList.Entry;
import net.forgecraft.mods.bridge.network.TPSPacket;
import net.forgecraft.mods.bridge.structs.TickTimeHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.LinearLayout.Orientation;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;

public class TPSScreen extends Screen {
    private static final DecimalFormat TIME_FORMATTER = new DecimalFormat("########0.000");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    @Nullable
    private TPSInformationList tpsList;
    @Nullable
    private EditBox search;
    private Instant lastRequest = Instant.now();
    private int lastKnownHash;

    public TPSScreen() {
        super(Component.literal("TPS Information"));
        requestTPSData(); // Request data when the screen is created
    }

    private void requestTPSData() {
        BridgeClientData.INSTANCE.requestServerTpsUpdate();
    }

    private static final int DIMENSION_NAME_WIDTH = 200;
    private static final int MEAN_TICK_TIME_WIDTH = 100;
    private static final int MEAN_TPS_WIDTH = 100;
    private static final int COLUMN_GAP = 4;

    @Override
    protected void init() {
        // Screen title
        this.layout.addTitleHeader(Component.literal("TPS Information"), this.font);

        final LinearLayout contentContainer = this.layout.addToContents(new LinearLayout(this.width, this.layout.getContentHeight(), Orientation.VERTICAL));
        contentContainer.defaultCellSetting().alignHorizontallyCenter().alignVerticallyMiddle();
        int leftPadding = TPSInformationList.ROW_PADDING + (2 * 2); // 2px * 2 sides (left and right)

        // Header information
        final LinearLayout header = contentContainer.addChild(new LinearLayout(this.width, 0, Orientation.HORIZONTAL));
        header.addChild(SpacerElement.width(leftPadding));
        header.addChild(new StringWidget(DIMENSION_NAME_WIDTH, font.lineHeight, Component.literal("Dimension name").withStyle(ChatFormatting.UNDERLINE), this.font).alignLeft());
        header.addChild(SpacerElement.width(COLUMN_GAP));
        header.addChild(new StringWidget(MEAN_TICK_TIME_WIDTH, font.lineHeight, Component.literal("Mean tick time").withStyle(ChatFormatting.UNDERLINE), this.font).alignLeft());
        header.addChild(SpacerElement.width(COLUMN_GAP));
        header.addChild(new StringWidget(MEAN_TPS_WIDTH, font.lineHeight, Component.literal("Mean TPS").withStyle(ChatFormatting.UNDERLINE), this.font).alignLeft());

        // The actual list
        contentContainer.addChild(SpacerElement.height(4));
        this.tpsList = contentContainer.addChild(new TPSInformationList(this.layout.getContentHeight() - font.lineHeight - 4 - 6 - (font.lineHeight + 5)));
        contentContainer.addChild(SpacerElement.height(6));

        // Search bar
        this.search = contentContainer.addChild(new EditBox(this.font, DIMENSION_NAME_WIDTH, this.font.lineHeight + 5, Component.translatable("fml.menu.mods.search")));
        this.search.setMaxLength(64);
        this.search.setHint(Component.translatable("fml.menu.mods.search").withStyle(ChatFormatting.GRAY));
        this.search.setFocused(false);
        this.search.setCanLoseFocus(true);
        this.search.setResponder(this::onSearchBoxEdited);

        // Back button
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    private void onSearchBoxEdited(String newValue) {
        if (tpsList != null) {
            tpsList.updateVisibleEntries();
        }
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
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

        TPSPacket serverTps = BridgeClientData.INSTANCE.getServerTps();
        if (tpsList != null) {
            tpsList.updateAllEntries(serverTps);
        }
    }

    private int calculateTPSColor(double tps) {
        assert this.minecraft != null && this.minecraft.level != null;
        float maxTPS = TimeUtil.MILLISECONDS_PER_SECOND / this.minecraft.level.tickRateManager().millisecondsPerTick();

        // 0 degrees (0F) is red, 120 degrees (0.33F) is green
        return Mth.hsvToRgb((float) (Mth.inverseLerp(tps, 0, maxTPS) * 0.33F), 1F, 1F);
    }

    class TPSInformationList extends ObjectSelectionList<Entry> {
        private final List<FilledEntry> allEntries = new ArrayList<>();
        static final int ROW_PADDING = 32;

        public TPSInformationList(int height) {
            //noinspection DataFlowIssue (minecraft is known to be non-null here)
            super(TPSScreen.this.minecraft, DIMENSION_NAME_WIDTH + COLUMN_GAP + MEAN_TICK_TIME_WIDTH + COLUMN_GAP + MEAN_TPS_WIDTH, height, 0, 13);
            // Item height is 13: 2 (padding) + 9 (line) + 2 (padding)
            this.addEntry(new EmptyEntry());
        }

        private void updateAllEntries(@Nullable TPSPacket tps) {
            // Intelligently update only when the object has changed
            // Using the hashCode to avoid holding the (previous) object in memory for comparison
            final int hash = Objects.hashCode(tps);
            if (hash == TPSScreen.this.lastKnownHash) return; // Do nothing, still up to date
            TPSScreen.this.lastKnownHash = hash;

            this.allEntries.clear();
            if (tps != null) {
                final TickTimeHolder overallHolder = tps.overall();
                //noinspection NoTranslation
                final FilledEntry overall = new FilledEntry("overall",
                        Component.literal("Overall").withStyle(ChatFormatting.BOLD),
                        Component.literal(TIME_FORMATTER.format(overallHolder.meanTickTime()) + "ms").withStyle(ChatFormatting.BOLD),
                        Component.literal(TIME_FORMATTER.format(overallHolder.meanTPS())).withStyle(ChatFormatting.BOLD).withColor(calculateTPSColor(overallHolder.meanTPS())),
                        Component.translatable("Overall mean tick time of %s milliseconds and mean TPS of %s",
                                TIME_FORMATTER.format(overallHolder.meanTickTime()), TIME_FORMATTER.format(overallHolder.meanTPS())));
                this.allEntries.add(overall);

                var sortedDimensionEntrySet = tps.dimensionMap().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .toList();

                for (Map.Entry<ResourceLocation, TickTimeHolder> entry : sortedDimensionEntrySet) {
                    final ResourceLocation location = entry.getKey();
                    final String locationStr = location.toString();
                    final Component locationComponent = Component.translatableWithFallback(location.toLanguageKey("dimension"), locationStr)
                            .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(locationStr))));
                    final FilledEntry filledEntry = new FilledEntry(locationStr, locationComponent, entry.getValue());
                    this.allEntries.add(filledEntry);
                }
            }
            updateVisibleEntries();
        }

        private void updateVisibleEntries() {
            // Store last selected, to reselect later on
            String lastSelected = null;
            if (this.getSelected() instanceof FilledEntry entry) {
                lastSelected = entry.location;
            }

            this.clearEntries();
            if (allEntries.isEmpty()) {
                // No data -- no entries
                this.addEntry(new EmptyEntry());
                return;
            }

            for (FilledEntry entry : allEntries) {
                if (search != null && !search.getValue().isEmpty()) {
                    // Filter based on search value; case-insensitive
                    final String searchValue = search.getValue().toLowerCase(Locale.ROOT);
                    final String locationName = entry.locationComponent.getString();
                    if (!locationName.toLowerCase(Locale.ROOT).contains(searchValue)) {
                        // Does not contain search value -- discard
                        continue;
                    }
                }
                this.addEntry(entry);

                // Reselect if possible
                if (entry.location.equalsIgnoreCase(lastSelected)) {
                    this.setSelected(entry);
                }
            }
        }

        @Override
        public int getRowWidth() {
            return this.width - ROW_PADDING;
        }

        @Override
        protected void renderHeader(@NotNull GuiGraphics guiGraphics, int x, int y) {
            guiGraphics.drawString(TPSScreen.this.font, Component.literal("Dimension name").withStyle(ChatFormatting.GRAY), x, y, 0xFFFFFF);
            x += DIMENSION_NAME_WIDTH + COLUMN_GAP;
            guiGraphics.drawString(TPSScreen.this.font, Component.literal("Mean tick time").withStyle(ChatFormatting.GRAY), x, y, 0xFFFFFF);
            x += MEAN_TICK_TIME_WIDTH + COLUMN_GAP;
            guiGraphics.drawString(TPSScreen.this.font, Component.literal("Mean TPS").withStyle(ChatFormatting.GRAY), x, y, 0xFFFFFF);
        }

        @Override
        protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (mouseY < this.getY() || mouseY > this.getBottom()) return;

            final Entry hovered = this.getHovered();
            if (!(hovered instanceof FilledEntry entry)) return;

            final int index = this.children().indexOf(entry);

            int top = this.getRowTop(index) + 1; // Match padding
            int left = this.getRowLeft();

            final ScreenRectangle locationRect = ScreenRectangle.of(ScreenAxis.HORIZONTAL,
                    left, top,
                    TPSScreen.this.font.width(entry.locationComponent), TPSScreen.this.font.lineHeight);
            if (locationRect.containsPoint(mouseX, mouseY)) {
                guiGraphics.renderComponentHoverEffect(TPSScreen.this.font, entry.locationComponent.getStyle(), mouseX, mouseY);
            }
        }

        abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
        }

        class EmptyEntry extends Entry {
            @Override
            public @NotNull Component getNarration() {
                return Component.literal("No data");
            }

            @Override
            public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int height, int width,
                               int mouseX, int mouseY, boolean focused, float partialTick) {
                top += 1; // Add a bit more padding
                guiGraphics.drawCenteredString(TPSScreen.this.font, Component.literal("No data"), TPSScreen.this.width / 2, top, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                Entry current = TPSInformationList.this.getFocused();
                if (current != this && current instanceof ContainerEventHandler handler) {
                    handler.setFocused(null);
                }

                TPSInformationList.this.setFocused(this);
                TPSInformationList.this.setDragging(true);
                return false;
            }
        }

        class FilledEntry extends Entry {
            final String location;
            final Component locationComponent;
            final Component meanTickTimeComponent;
            final Component meanTPSComponent;
            final Component narration;

            // For dimensions
            FilledEntry(String location, Component locationComponent, TickTimeHolder tickTimeHolder) {
                this.location = location;
                this.locationComponent = locationComponent;
                this.meanTickTimeComponent = Component.literal(TIME_FORMATTER.format(tickTimeHolder.meanTickTime()) + "ms");
                this.meanTPSComponent = Component.literal(TIME_FORMATTER.format(tickTimeHolder.meanTPS())).withColor(calculateTPSColor(tickTimeHolder.meanTPS()));
                //noinspection NoTranslation
                this.narration = Component.translatable("Dimension %s with mean tick time of %s milliseconds and mean TPS of %s",
                        location, TIME_FORMATTER.format(tickTimeHolder.meanTickTime()), TIME_FORMATTER.format(tickTimeHolder.meanTPS()));
            }

            // Specifically for the overall entry
            FilledEntry(String location, Component locationComponent, Component meanTickTimeComponent, Component meanTPSComponent, Component narration) {
                this.location = location;
                this.locationComponent = locationComponent;
                this.meanTickTimeComponent = meanTickTimeComponent;
                this.meanTPSComponent = meanTPSComponent;
                this.narration = narration;
            }

            @Override
            public @NotNull Component getNarration() {
                return narration;
            }

            @Override
            public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int height, int width,
                               int mouseX, int mouseY, boolean focused, float partialTick) {
                top += 1; // Add a bit more padding
                final FormattedText text = TPSScreen.this.font.ellipsize(locationComponent, DIMENSION_NAME_WIDTH);
                guiGraphics.drawString(TPSScreen.this.font, Language.getInstance().getVisualOrder(text), left, top, 0xFFFFFF);
                int locationLeft = left, locationTop = top;
                left += DIMENSION_NAME_WIDTH + COLUMN_GAP;
                guiGraphics.drawString(TPSScreen.this.font, meanTickTimeComponent, left, top, 0xFFFFFF);
                left += MEAN_TICK_TIME_WIDTH + COLUMN_GAP;
                guiGraphics.drawString(TPSScreen.this.font, meanTPSComponent, left, top, 0xFFFFFF);
            }
        }
    }
}
