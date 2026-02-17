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
 * - Top left: Pickaxe + Fuel with flame indicator
 * - Top right: 4x6 output grid
 * - Center: Progress arrow
 * - Bottom left: Filter mode + Chunk loader toggles
 * - Bottom center-right: 3x3 filter grid
 * - Bottom: Player inventory
 */
public class QuarryScreen extends HandledScreen<QuarryScreenHandler> {
    
    private static final Identifier FURNACE_TEXTURE = Identifier.of("minecraft", "textures/gui/container/furnace.png");

    // Colors
    private static final int BG_COLOR = 0xFFC6C6C6;
    private static final int BORDER_LIGHT = 0xFFFFFFFF;
    private static final int BORDER_DARK = 0xFF555555;
    private static final int BORDER_DARKER = 0xFF373737;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int SEPARATOR = 0xFFA0A0A0;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int TEXT_DARK = 4210752;

    private ButtonWidget filterButton;
    private ButtonWidget chunkLoaderButton;

    public QuarryScreen(QuarryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = 128;
    }

    @Override
    protected void init() {
        super.init();
        titleX = 8;

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Filter mode button - compact label
        filterButton = ButtonWidget.builder(
                getFilterButtonText(),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(handler.syncId, 0);
                    }
                }
        ).dimensions(x + 8, y + 82, 66, 18).build();
        this.addDrawableChild(filterButton);

        // Chunk loader toggle button
        chunkLoaderButton = ButtonWidget.builder(
                getChunkButtonText(),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(handler.syncId, 1);
                    }
                }
        ).dimensions(x + 8, y + 103, 66, 18).build();
        this.addDrawableChild(chunkLoaderButton);
    }

    private Text getFilterButtonText() {
        String mode = switch (handler.getFilterMode()) {
            case QuarryBlockEntity.FILTER_WHITELIST -> "§aWhitelist";
            case QuarryBlockEntity.FILTER_BLACKLIST -> "§cBlacklist";
            default -> "§7Disabled";
        };
        return Text.literal(mode);
    }

    private Text getChunkButtonText() {
        return Text.literal(handler.isChunkLoaderEnabled() ? "§aChunks §2ON" : "§7Chunks §8OFF");
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (filterButton != null) {
            filterButton.setMessage(getFilterButtonText());
        }
        if (chunkLoaderButton != null) {
            chunkLoaderButton.setMessage(getChunkButtonText());
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Draw main background with 3D border
        drawPanel(context, x, y, this.backgroundWidth, this.backgroundHeight);

        // ============ TOP SECTION: Mining Interface ============

        // Draw output grid slots (4x6)
        int outputStartX = x + 62;
        int outputStartY = y + 8;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 6; col++) {
                drawSlot(context, outputStartX + col * 18, outputStartY + row * 18);
            }
        }

        // Draw pickaxe slot
        int pickaxeSlotX = x + 7;
        int pickaxeSlotY = y + 16;
        drawSlot(context, pickaxeSlotX, pickaxeSlotY);
        
        // Draw pickaxe ghost icon if empty
        if (!handler.hasPickaxe()) {
            context.drawItem(Items.IRON_PICKAXE.getDefaultStack(), pickaxeSlotX + 1, pickaxeSlotY + 1);
            context.fill(pickaxeSlotX + 1, pickaxeSlotY + 1, pickaxeSlotX + 17, pickaxeSlotY + 17, 0x80C6C6C6);
        }

        // Draw fuel slot
        int fuelSlotX = x + 7;
        int fuelSlotY = y + 52;
        drawSlot(context, fuelSlotX, fuelSlotY);
        
        // Draw coal ghost icon if empty
        if (!handler.hasFuel()) {
            context.drawItem(Items.COAL.getDefaultStack(), fuelSlotX + 1, fuelSlotY + 1);
            context.fill(fuelSlotX + 1, fuelSlotY + 1, fuelSlotX + 17, fuelSlotY + 17, 0x80C6C6C6);
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

        // ============ SEPARATOR ============
        context.fill(x + 7, y + 78, x + 169, y + 79, SEPARATOR);
        context.fill(x + 7, y + 79, x + 169, y + 80, BORDER_LIGHT);

        // ============ FILTER SECTION ============

        // "Filter" label above the filter grid
        context.drawText(this.textRenderer, Text.literal("Filter"), x + 96, y + 83, TEXT_DARK, false);

        // Draw filter 3x3 grid with mode-colored accent
        int filterStartX = x + 79;
        int filterStartY = y + 93;
        int accentColor = getFilterAccentColor();
        
        // Draw a subtle colored background behind the filter grid
        if (handler.getFilterMode() != QuarryBlockEntity.FILTER_DISABLED) {
            context.fill(filterStartX - 1, filterStartY - 1, filterStartX + 55, filterStartY + 55, accentColor & 0x30FFFFFF);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                drawSlot(context, filterStartX + col * 18, filterStartY + row * 18);
            }
        }

        // Small colored indicator dot next to "Filter" label
        if (handler.getFilterMode() != QuarryBlockEntity.FILTER_DISABLED) {
            context.fill(x + 85, y + 84, x + 93, y + 92, accentColor);
            context.fill(x + 86, y + 85, x + 92, y + 91, accentColor);
        }

        // ============ SEPARATOR before player inv ============
        context.fill(x + 7, y + 125, x + 169, y + 126, SEPARATOR);
        context.fill(x + 7, y + 126, x + 169, y + 127, BORDER_LIGHT);

        // ============ PLAYER INVENTORY ============
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

    private int getFilterAccentColor() {
        return switch (handler.getFilterMode()) {
            case QuarryBlockEntity.FILTER_WHITELIST -> 0xFF44BB44;  // Green
            case QuarryBlockEntity.FILTER_BLACKLIST -> 0xFFBB4444;  // Red
            default -> 0xFF888888;
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
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, TEXT_DARK, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, TEXT_DARK, false);
    }

    /**
     * Draw a 3D panel background (like vanilla container GUIs)
     */
    private void drawPanel(DrawContext context, int x, int y, int w, int h) {
        // Fill background
        context.fill(x, y, x + w, y + h, BG_COLOR);
        // 3D border: light on top-left, dark on bottom-right
        context.fill(x, y, x + w, y + 2, BORDER_LIGHT);
        context.fill(x, y, x + 2, y + h, BORDER_LIGHT);
        context.fill(x + w - 2, y, x + w, y + h, BORDER_DARK);
        context.fill(x, y + h - 2, x + w, y + h, BORDER_DARK);
        // Inner edge
        context.fill(x + 2, y + 2, x + w - 2, y + 3, 0xFFDBDBDB);
        context.fill(x + 2, y + 2, x + 3, y + h - 2, 0xFFDBDBDB);
    }

    /**
     * Draw a standard Minecraft-style inventory slot
     */
    private void drawSlot(DrawContext context, int x, int y) {
        // Top-left shadow (dark)
        context.fill(x, y, x + 18, y + 1, BORDER_DARKER);
        context.fill(x, y, x + 1, y + 18, BORDER_DARKER);
        // Bottom-right highlight (light)  
        context.fill(x + 17, y + 1, x + 18, y + 18, BORDER_LIGHT);
        context.fill(x + 1, y + 17, x + 17, y + 18, BORDER_LIGHT);
        // Inner slot background
        context.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BG);
    }
}
