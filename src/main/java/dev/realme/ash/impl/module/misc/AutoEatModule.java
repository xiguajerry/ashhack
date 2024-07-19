package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.mixin.accessor.AccessorKeyBinding;
import dev.realme.ash.util.player.InventoryUtil;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoEatModule
extends ToggleModule {
    Config<Float> hungerConfig = new NumberConfig<Float>("Hunger", "The minimum hunger level before eating", 1.0f, 19.0f, 20.0f);
    private int prevSlot;

    public AutoEatModule() {
        super("AutoEat", "Automatically eats when losing hunger", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable() {
        this.prevSlot = -1;
    }

    @Override
    public void onDisable() {
        KeyBinding.setKeyPressed(((AccessorKeyBinding) AutoEatModule.mc.options.useKey).getBoundKey(), false);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (!AutoEatModule.mc.player.isUsingItem()) {
            if (this.prevSlot != -1) {
                InventoryUtil.doSwap(this.prevSlot);
                this.prevSlot = -1;
            }
            KeyBinding.setKeyPressed(((AccessorKeyBinding) AutoEatModule.mc.options.useKey).getBoundKey(), false);
            return;
        }
        HungerManager hungerManager = AutoEatModule.mc.player.getHungerManager();
        if ((float)hungerManager.getFoodLevel() <= this.hungerConfig.getValue().floatValue()) {
            int slot = this.getFoodSlot();
            if (slot == -1) {
                return;
            }
            if (slot == 45) {
                AutoEatModule.mc.player.setCurrentHand(Hand.OFF_HAND);
            } else {
                this.prevSlot = AutoEatModule.mc.player.getInventory().selectedSlot;
                InventoryUtil.doSwap(slot);
            }
            KeyBinding.setKeyPressed(((AccessorKeyBinding) AutoEatModule.mc.options.useKey).getBoundKey(), true);
        }
    }

    public int getFoodSlot() {
        int foodLevel = -1;
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            int hunger;
            ItemStack stack = AutoEatModule.mc.player.getInventory().getStack(i);
            if (!stack.getItem().isFood() || stack.getItem() == Items.PUFFERFISH || stack.getItem() == Items.CHORUS_FRUIT || (hunger = stack.getItem().getFoodComponent().getHunger()) <= foodLevel) continue;
            slot = i;
            foodLevel = hunger;
        }
        ItemStack offhand = AutoEatModule.mc.player.getOffHandStack();
        if (offhand.getItem().isFood()) {
            if (offhand.getItem() == Items.PUFFERFISH || offhand.getItem() == Items.CHORUS_FRUIT) {
                return slot;
            }
            int hunger = offhand.getItem().getFoodComponent().getHunger();
            if (hunger > foodLevel) {
                slot = 45;
            }
        }
        return slot;
    }
}
