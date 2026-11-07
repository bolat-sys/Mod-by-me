package com.bolat.casinomod.menu;

import com.bolat.casinomod.network.CasinoNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CasinoScreen extends AbstractContainerScreen<CasinoMenu> {
    private EditBox search;
    private int scroll = 0;
    private List<Item> filtered = new ArrayList<>();
    private static final int GRID_X = 104, GRID_Y = 64, COLS = 9, ROWS = 5;

    public CasinoScreen(CasinoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 340;
        this.imageHeight = 256;
        this.inventoryLabelY = 164;
    }

    @Override protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 104, topPos + 39, 162, 18, Component.literal("Search items"));
        search.setMaxLength(64);
        search.setHint(Component.literal("Search all mod items..."));
        search.setResponder(s -> { scroll = 0; updateFiltered(); });
        addRenderableWidget(search);
        updateFiltered();
    }

    private void updateFiltered() {
        String query = search == null ? "" : search.getValue().toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null || item == net.minecraft.world.item.Items.AIR) continue;
            String name = item.getDescription().getString().toLowerCase(Locale.ROOT);
            String key = id.toString().toLowerCase(Locale.ROOT);
            if (query.isBlank() || name.contains(query) || key.contains(query)) filtered.add(item);
        }
        filtered.sort((a,b) -> ForgeRegistries.ITEMS.getKey(a).toString().compareTo(ForgeRegistries.ITEMS.getKey(b).toString()));
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF100D18);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFF24182D);
        g.fill(x + 7, y + 7, x + imageWidth - 7, y + 30, 0xFF56351C);
        g.fill(x + 12, y + 34, x + 328, y + 158, 0xFF30223A);
        g.fill(x + 12, y + 166, x + 328, y + 251, 0xFF1A1422);
        g.fill(x + 25, y + 54, x + 73, y + 103, 0xFF120E18);
        g.fill(x + 279, y + 54, x + 327, y + 103, 0xFF120E18);
        g.fill(x + 25, y + 54, x + 73, y + 56, 0xFFFFC857);
        g.fill(x + 279, y + 54, x + 327, y + 56, 0xFF55E69A);
        g.drawString(font, "STAKE", x + 32, y + 42, 0xFFFFD36A, false);
        g.drawString(font, "SELECTED PRIZE", x + 273, y + 42, 0xFF70F0A6, false);
        g.drawString(font, "ITEM CATALOG  •  " + filtered.size() + " items", x + 104, y + 25, 0xFFFFD36A, false);
        g.drawString(font, "Search vanilla + installed mod items", x + 104, y + 59, 0xFFBDAFC9, false);

        int start = scroll;
        for (int i = 0; i < COLS * ROWS && start + i < filtered.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int sx = x + GRID_X + col * 18, sy = y + GRID_Y + row * 18;
            g.fill(sx, sy, sx + 18, sy + 18, 0xFF493653);
            Item item = filtered.get(start + i);
            g.renderItem(new net.minecraft.world.item.ItemStack(item), sx + 1, sy + 1);
            if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
                g.fill(sx, sy, sx + 18, sy + 18, 0x55FFFFFF);
                g.renderTooltip(font, new net.minecraft.world.item.ItemStack(item), mouseX, mouseY);
            }
        }
        g.drawString(font, "Select an item as your prize, then place a stake.", x + 14, y + 146, 0xFFE6D9F0, false);
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "YOUR INVENTORY", 92, 165, 0xFFE6D9F0, false);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int gx = leftPos + GRID_X, gy = topPos + GRID_Y;
            if (mouseX >= gx && mouseX < gx + COLS * 18 && mouseY >= gy && mouseY < gy + ROWS * 18) {
                int col = (int)(mouseX - gx) / 18, row = (int)(mouseY - gy) / 18;
                int index = scroll + row * COLS + col;
                if (index >= 0 && index < filtered.size()) {
                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(filtered.get(index));
                    if (id != null) CasinoNetwork.selectPrize(id.toString());
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll = Math.max(0, Math.min(Math.max(0, filtered.size() - COLS * ROWS), scroll - (int)Math.signum(delta) * COLS));
        return true;
    }
}
