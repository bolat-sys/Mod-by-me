package com.bolat.casinomod.menu;

import com.bolat.casinomod.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;
import net.minecraft.util.RandomSource;

public class CasinoMenu extends AbstractContainerMenu {
    private final net.minecraft.world.SimpleContainer casino = new net.minecraft.world.SimpleContainer(2);
    private final Player owner;
    private ItemStack selectedPrize = ItemStack.EMPTY;
    private boolean rolling = false;

    public CasinoMenu(int id, Inventory inventory, FriendlyByteBuf data) { this(id, inventory); }
    public CasinoMenu(int id, Inventory inventory) {
        super(ModMenus.CASINO_MENU.get(), id);
        this.owner = inventory.player;
        addSlot(new Slot(casino, 0, 30, 62) {
            @Override public void setChanged() { super.setChanged(); tryRoll(); }
            @Override public boolean mayPlace(ItemStack stack) { return !stack.isEmpty(); }
        });
        addSlot(new Slot(casino, 1, 290, 62) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 92 + col * 18, 178 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 92 + col * 18, 232));
    }

    public void selectPrize(ItemStack stack) {
        if (!(owner instanceof ServerPlayer) || !casino.getItem(0).isEmpty()) return;
        selectedPrize = stack.copy();
        selectedPrize.setCount(1);
        casino.setItem(1, selectedPrize.copy());
        broadcastChanges();
    }

    private void tryRoll() {
        if (rolling || casino.getItem(0).isEmpty() || !(owner instanceof ServerPlayer player)) return;
        rolling = true;
        ItemStack stake = casino.getItem(0).copy();
        if (selectedPrize.isEmpty()) {
            casino.setItem(0, ItemStack.EMPTY);
            if (!player.getInventory().add(stake)) player.drop(stake, false);
            player.sendSystemMessage(Component.literal("§cChoose your prize first! Your stake was returned."));
            rolling = false;
            return;
        }
        int chance = winChance(stake);
        casino.setItem(0, ItemStack.EMPTY);
        if (RandomSource.create().nextInt(100) < chance) {
            ItemStack reward = selectedPrize.copy();
            reward.setCount(1);
            casino.setItem(1, reward);
            player.sendSystemMessage(Component.literal("§6✦ YOU WON! §fPrize: " + reward.getHoverName().getString() + " §7| Chance: " + chance + "%"));
            selectedPrize = ItemStack.EMPTY;
        } else {
            casino.setItem(1, ItemStack.EMPTY);
            selectedPrize = ItemStack.EMPTY;
            player.sendSystemMessage(Component.literal("§c✖ YOU LOST! §7Chance was " + chance + "%. Choose a new prize."));
        }
        casino.setChanged();
        rolling = false;
    }

    private int winChance(ItemStack stack) {
        int base;
        if (stack.is(Items.NETHERITE_INGOT) || stack.is(Items.NETHERITE_BLOCK)) base = 80;
        else if (stack.is(Items.DIAMOND) || stack.is(Items.DIAMOND_BLOCK)) base = 60;
        else if (stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLD_BLOCK)) base = 40;
        else if (stack.is(Items.IRON_INGOT) || stack.is(Items.IRON_BLOCK)) base = 25;
        else if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.PLANKS)) base = 5;
        else if (stack.is(Items.COBBLESTONE)) base = 10;
        else if (stack.getRarity() == Rarity.EPIC) base = 70;
        else if (stack.getRarity() == Rarity.RARE) base = 45;
        else if (stack.getRarity() == Rarity.UNCOMMON) base = 25;
        else base = 8;
        return Math.min(90, Math.max(1, (int)Math.round(base * Math.sqrt(stack.getCount() / 64.0))));
    }

    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack source = slot.getItem(); result = source.copy();
            if (index < 2) { if (!moveItemStackTo(source, 2, slots.size(), true)) return ItemStack.EMPTY; }
            else { if (!moveItemStackTo(source, 0, 1, false)) return ItemStack.EMPTY; }
            if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return result;
    }
}
