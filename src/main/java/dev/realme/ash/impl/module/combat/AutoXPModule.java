package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.util.Iterator;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;

public class AutoXPModule
extends RotationModule {
    Config<InventoryUtil.SwapMode> swapMode = new EnumConfig("SwapMode", "", (Enum)InventoryUtil.SwapMode.SILENT, (Enum[])InventoryUtil.SwapMode.values());
    Config<Boolean> rotate = new BooleanConfig("Rotate", "Rotates the player while throwing xp.", true);
    Config<Float> delay = new NumberConfig<Float>("Delay", "Delay to throw xp in ticks.", Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(10.0f), NumberDisplay.DEFAULT);
    Config<Boolean> durabilityCheck = new BooleanConfig("DurabilityCheck", "Check if your armor and held item durability is full then disables if it is.", true);
    Config<Boolean> usingPause = new BooleanConfig("UsingPause", "", true);
    private final Timer delayTimer = new CacheTimer();
    int slot;

    public AutoXPModule() {
        super("AutoXP", "Automatically throws xp silently.", ModuleCategory.COMBAT, 850);
    }

    @Override
    public String getModuleData() {
        return "" + InventoryUtil.count(Items.EXPERIENCE_BOTTLE);
    }

    @EventListener
    public void onPlayerTick(UpdateWalkingEvent event) {
        if (AutoXPModule.nullCheck()) {
            return;
        }
        if (!this.checkThrow()) {
            this.disable();
            return;
        }
        if (AutoXPModule.mc.player.isUsingItem() && this.usingPause.getValue().booleanValue()) {
            return;
        }
        if (!this.delayTimer.passed(this.delay.getValue())) {
            return;
        }
        int prev = AutoXPModule.mc.player.getInventory().selectedSlot;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                this.slot = InventoryUtil.findClass(ExperienceBottleItem.class);
                break;
            }
            case Inventory: {
                this.slot = InventoryUtil.findClassInventorySlot(ExperienceBottleItem.class, false);
                break;
            }
            case Pick: {
                this.slot = InventoryUtil.findClassInventorySlot(ExperienceBottleItem.class, true);
            }
        }
        if (this.slot == -1) {
            this.disable();
            return;
        }
        if (this.rotate.getValue().booleanValue()) {
            this.setRotation(AutoXPModule.mc.player.getYaw(), 90.0f);
        }
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                InventoryUtil.doSwap(this.slot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.slot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.slot);
            }
        }
        Managers.INTERACT.useItem(Hand.MAIN_HAND);
        switch (this.swapMode.getValue()) {
            case SILENT: {
                InventoryUtil.doSwap(prev);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.slot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.slot);
            }
        }
        this.delayTimer.reset();
    }

    public boolean checkThrow() {
        if (!this.durabilityCheck.getValue().booleanValue()) {
            return true;
        }
        DefaultedList armors = AutoXPModule.mc.player.getInventory().armor;
        Iterator iterator = armors.iterator();
        while (iterator.hasNext()) {
            ItemStack armor = (ItemStack)iterator.next();
            if (armor.isEmpty() || EntityUtil.getDamagePercent(armor) >= 100) continue;
            return true;
        }
        return false;
    }
}
