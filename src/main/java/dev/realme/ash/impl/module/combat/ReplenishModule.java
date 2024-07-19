package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.mixin.accessor.AccessorItemStack;
import dev.realme.ash.util.player.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ReplenishModule
extends ToggleModule {
    final Config<Integer> threshold = new NumberConfig<>("Threshold", "", 0, 32, 64);
    final Config<Integer> tickDelay = new NumberConfig<>("TickDelay", "Added delays", 0, 1, 10);
    final Config<Boolean> unstackable = new BooleanConfig("Unstackable", "", true);
    private int tickDelayLeft;
    private boolean prevHadOpenScreen;
    private final ItemStack[] items = new ItemStack[10];

    public ReplenishModule() {
        super("Replenish", "Automatically replaces items in your hotbar", ModuleCategory.COMBAT);
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = new ItemStack(Items.AIR);
        }
    }

    @Override
    public void onEnable() {
        this.fillItems();
        this.tickDelayLeft = this.tickDelay.getValue();
        this.prevHadOpenScreen = ReplenishModule.mc.currentScreen != null;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (ReplenishModule.mc.currentScreen == null && this.prevHadOpenScreen) {
            this.fillItems();
        }
        boolean bl = this.prevHadOpenScreen = ReplenishModule.mc.currentScreen != null;
        if (ReplenishModule.mc.player.currentScreenHandler.getStacks().size() != 46 || ReplenishModule.mc.currentScreen != null) {
            return;
        }
        if (this.tickDelayLeft <= 0) {
            this.tickDelayLeft = this.tickDelay.getValue();
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = ReplenishModule.mc.player.getInventory().getStack(i);
                this.checkSlot(i, stack);
            }
        } else {
            --this.tickDelayLeft;
        }
    }

    private void checkSlot(int slot, ItemStack stack) {
        ItemStack prevStack = this.getItem(slot);
        if (!stack.isEmpty() && stack.isStackable() && stack.getCount() <= this.threshold.getValue()) {
            this.addSlots(slot, this.findItem(stack, slot, this.threshold.getValue() - stack.getCount() + 1));
        }
        if (stack.isEmpty() && !prevStack.isEmpty()) {
            if (prevStack.isStackable()) {
                this.addSlots(slot, this.findItem(prevStack, slot, this.threshold.getValue() - stack.getCount() + 1));
            } else if (this.unstackable.getValue()) {
                this.addSlots(slot, this.findItem(prevStack, slot, 1));
            }
        }
        this.setItem(slot, stack);
    }

    private int findItem(ItemStack itemStack, int excludedSlot, int goodEnoughCount) {
        int slot = -1;
        int count = 0;
        for (int i = ReplenishModule.mc.player.getInventory().size() - 2; i >= 9; --i) {
            ItemStack stack = ReplenishModule.mc.player.getInventory().getStack(i);
            if (i == excludedSlot || stack.getItem() != itemStack.getItem() || !ItemStack.canCombine(itemStack, stack) || stack.getCount() <= count) continue;
            slot = i;
            count = stack.getCount();
            if (count >= goodEnoughCount) break;
        }
        return slot;
    }

    private ItemStack getItem(int slot) {
        if (slot == 45) {
            slot = 9;
        }
        return this.items[slot];
    }

    private void addSlots(int to, int from) {
        InventoryUtil.move().from(from).to(to);
    }

    private void fillItems() {
        for (int i = 0; i < 9; ++i) {
            this.setItem(i, ReplenishModule.mc.player.getInventory().getStack(i));
        }
        this.setItem(45, ReplenishModule.mc.player.getOffHandStack());
    }

    private void setItem(int slot, ItemStack stack) {
        if (slot == 45) {
            slot = 9;
        }
        ItemStack s = this.items[slot];
        ((AccessorItemStack)((Object)s)).setItem(stack.getItem());
        s.setCount(stack.getCount());
        s.setNbt(stack.getNbt());
        if (stack.isEmpty()) {
            ((AccessorItemStack)((Object)s)).setItem(Items.AIR);
        }
    }
}
