package com.simplequarries.screen;

import com.simplequarries.block.entity.QuarryBlockEntity;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Client-side screen for the Quarry GUI
 * 
 * Layout:
 * - Left: Pickaxe slot (top), Fuel slot (bottom) with flame
 * - Center: Progress arrow
 * - Right: 4x6 output grid (24 slots)
 * - Below output: Filter mode button + chunk loader button + 3x3 filter grid
 * - Bottom: Player inventory
 */
public class QuarryScreen extends HandledScreen<QuarryScreenHandler> {
    
    private static final Identifier FURNACE_TEXTURE = Identifier.of("minecraft", "textures/gui/container/furnace.png");

    private ButtonWidget filterButton;
    private ButtonWidget chunkLoaderButton;

    public QuarryScreen(QuarryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 222;  // Taller to fit filter section + player inv
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleX = 8;

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Filter mode button (left of filter grid)
        filterButton = ButtonWidget.builder(
                Text.literal("Filter: " + handler.getFilterModeText()),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(handler.syncId, 0);
                    }
                }
        ).dimensions(x + 8, y + 82, 68, 16).build();
        this.addDrawableChild(filterButton);

        // Chunk loader toggle button
        chunkLoaderButton = ButtonWidget.builder(
                Text.literal(handler.isChunkLoaderEnabled() ? "Chunkload: ON" : "Chunkload: OFF"),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(handler.syncId, 1);
                    }
                }
        ).dimensions(x + 8, y + 100, 68, 16).build();
        this.addDrawableChild(chunkLoaderButton);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        // Update button labels to reflect current state
        if (filterButton != null) {
            filterButton.setMessage(Text.literal("Filter: " + handler.getFilterModeText()));
        }
        if (chunkLoaderButton != null) {
            chunkLoaderButton.setMessage(Text.literal(handler.isChunkLoaderEnabled() ? "Chunkload: ON" : "Chunkload: OFF"));
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Draw background - fill entire area with standard GUI gray
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFFC6C6C6);
        
        // Draw border (3D effect like vanilla GUIs)
        context.fill(x, y, x + this.backgroundWidth, y + 1, 0xFFFFFFFF);  // Top white
        context.fill(x, y, x + 1, y + this.backgroundHeight, 0xFFFFFFFF);  // Left white
        context.fill(x + this.backgroundWidth - 1, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF555555);  // Right dark
        context.fill(x, y + this.backgroundHeight - 1, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF555555);  // Bottom dark

        // Draw output grid slots (4x6)
        int outputStartX = x + 61;
        int outputStartY = y + 7;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 6; col++) {
                int slotX = outputStartX + col * 18;
                int slotY = outputStartY + row * 18;
                drawSlot(context, slotX, slotY);
            }
        }

        // Draw pickaxe slot with highlight
        int pickaxeSlotX = x + 7;
        int pickaxeSlotY = y + 16;
        drawSlotHighlight(context, pickaxeSlotX, pickaxeSlotY, 0xFFAA7744);
        
        if (!handler.hasPickaxe()) {
            context.drawItem(Items.IRON_PICKAXE.getDefaultStack(), pickaxeSlotX + 1, pickaxeSlotY + 1);
            context.fill(pickaxeSlotX + 1, pickaxeSlotY + 1, pickaxeSlotX + 17, pickaxeSlotY + 17, 0x80000000);
        }

        // Draw fuel slot with highlight
        int fuelSlotX = x + 7;
        int fuelSlotY = y + 52;
        drawSlotHighlight(context, fuelSlotX, fuelSlotY, 0xFFDD6600);
        
        if (!handler.hasFuel()) {
            context.drawItem(Items.COAL.getDefaultStack(), fuelSlotX + 1, fuelSlotY + 1);
            context.fill(fuelSlotX + 1, fuelSlotY + 1, fuelSlotX + 17, fuelSlotY + 17, 0x80000000);
        }

        // Draw fuel flame indicator
        int flameX = x + 9;
        int flameY = y + 36;
        if (handler.isBurning()) {
            int flame = handler.getScaledFuelProgress();
            context.drawTexture(RenderPipelines.GUI_TEXTURED, FURNACE_TEXTURE, flameX, flameY + 12 - flame, 176.0f, (float)(12 - flame), 14, flame + 1, 256, 256);
        }

        // Draw progress arrow
        int arrowX = x + 34;
        int arrowY = y + 34;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, FURNACE_TEXTURE, arrowX, arrowY, 79.0f, 35.0f, 24, 16, 256, 256);
        int arrow = handler.getScaledMiningProgress();
        if (arrow > 0) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, FURNACE_TEXTURE, arrowX, arrowY, 176.0f, 14.0f, arrow + 1, 16, 256, 256);
        }

        // Draw filter slots (3x3 grid)
        int filterStartX = x + 79;
        int filterStartY = y + 81;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = filterStartX + col * 18;
                int slotY = filterStartY + row * 18;
                drawSlotHighlight(context, slotX, slotY, getFilterSlotColor());
            }
        }

        // Draw "Filter" label above filter grid
        context.drawText(this.textRenderer, Text.literal("Filter"), x + 80, y + 73, 4210752, false);

        // Draw redstone indicator
        boolean powered = false; // We can't easily check this client-side, but the quarry just pauses
        // The paused state is visible from the arrow not progressing

        // Draw player inventory background slots
        int playerInvY = y + 139;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(context, x + 7 + col * 18, playerInvY + row * 18);
            }
        }
        int hotbarY = y + 197;
        for (int col = 0; col < 9; col++) {
            drawSlot(context, x + 7 + col * 18, hotbarY);
        }
    }

    private int getFilterSlotColor() {
        return switch (handler.getFilterMode()) {
            case QuarryBlockEntity.FILTER_WHITELIST -> 0xFF44AA44;  // Green
            case QuarryBlockEntity.FILTER_BLACKLIST -> 0xFFAA4444;  // Red
            default -> 0xFF888888;  // Gray (disabled)
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);
    }

    private void drawSlot(DrawContext context, int x, int y) {
        context.fill(x, y, x + 18, y + 1, 0xFF373737);
        context.fill(x, y, x + 1, y + 18, 0xFF373737);
        context.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
        context.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        context.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }

    private void drawSlotHighlight(DrawContext context, int x, int y, int borderColor) {
        context.fill(x, y, x + 18, y + 2, borderColor);
        context.fill(x, y, x + 2, y + 18, borderColor);
        context.fill(x + 16, y, x + 18, y + 18, darkenColor(borderColor));
        context.fill(x, y + 16, x + 18, y + 18, darkenColor(borderColor));
        context.fill(x + 2, y + 2, x + 16, y + 16, 0xFF8B8B8B);
    }

    private int darkenColor(int color) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, ((color >> 16) & 0xFF) - 60);
        int g = Math.max(0, ((color >> 8) & 0xFF) - 60);
        int b = Math.max(0, (color & 0xFF) - 60);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
