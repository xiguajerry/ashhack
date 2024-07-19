package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.gui.click.ClickGuiScreen;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;

public class AutoPotModule
extends RotationModule {
    final Config<Mode> mode = new EnumConfig<>("Mode", "", Mode.Turtle, Mode.values());
    final Config<Boolean> onlyGround = new BooleanConfig("OnlyGround", "", true);
    final Config<Boolean> autoTurtle = new BooleanConfig("AutoTurtle", "", true);
    final Config<SwapMode> swapMode = new EnumConfig<>("SwapMode", "", SwapMode.SILENT, SwapMode.values());
    final Config<Float> delay = new NumberConfig<>("Delay", "", 0.0f, 1.0f, 1000.0f, NumberDisplay.DEFAULT);
    final Config<Boolean> usingPause = new BooleanConfig("UsingPause", "", true);
    private final Timer delayTimer = new CacheTimer();
    public int slot;

    public AutoPotModule() {
        super("AutoPot", "Automatically throws pot.", ModuleCategory.COMBAT, 850);
    }

    @Override
    public String getModuleData() {
        return "" + InventoryUtil.count(StatusEffects.RESISTANCE);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (AutoPotModule.nullCheck()) {
            return;
        }
        if (!(AutoPotModule.mc.currentScreen == null || AutoPotModule.mc.currentScreen instanceof ChatScreen || AutoPotModule.mc.currentScreen instanceof InventoryScreen || AutoPotModule.mc.currentScreen instanceof ClickGuiScreen)) {
            return;
        }
        if (!this.delayTimer.passed(this.delay.getValue())) {
            return;
        }
        this.slot = this.findItem();
        if (this.slot == -1) {
            return;
        }
        assert AutoPotModule.mc.player != null;
        for (StatusEffectInstance e : AutoPotModule.mc.player.getStatusEffects()) {
            int duration;
            if (e == null) {
                return;
            }
            if (!e.getEffectType().equals(StatusEffects.RESISTANCE) || e.getAmplifier() <= 2 || (duration = e.getDuration()) < 0 || !this.autoTurtle.getValue()) continue;
            return;
        }
        if (AutoPotModule.mc.player.fallDistance >= 2.0f) {
            return;
        }
        if (this.onlyGround.getValue() && !AutoPotModule.mc.player.isOnGround()) {
            return;
        }
        if (this.usingPause.getValue() && AutoPotModule.mc.player.isUsingItem()) {
            return;
        }
        if (Managers.MOVEMENT.getSpeed(AutoPotModule.mc.player) >= 15.0) {
            this.setRotation(AutoPotModule.mc.player.getYaw(), 50.0f);
        } else {
            this.setRotation(AutoPotModule.mc.player.getYaw(), 90.0f);
        }
        this.throwPot();
        this.delayTimer.reset();
    }

    private void throwPot() {
        assert AutoPotModule.mc.player != null;
        int prev = AutoPotModule.mc.player.getInventory().selectedSlot;
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
    }

    public boolean isStackPotion(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.getItem() == Items.SPLASH_POTION) {
            StatusEffect id = null;
            switch (this.mode.getValue()) {
                case Turtle: {
                    id = StatusEffects.RESISTANCE;
                    break;
                }
                case Healing: {
                    id = StatusEffects.INSTANT_HEALTH;
                }
            }
            for (StatusEffectInstance effect : PotionUtil.getPotion(stack).getEffects()) {
                if (effect.getEffectType() != id) continue;
                return true;
            }
        }
        return false;
    }

    public int findItem() {
        for (int i = this.swapMode.getValue().equals(SwapMode.Pick) ? 9 : 0; i < (this.swapMode.getValue().equals(SwapMode.SILENT) || this.swapMode.getValue().equals(SwapMode.NORMAL) ? 9 : 45); ++i) {
            ItemStack stack = InventoryUtil.getStackInSlot(i);
            assert stack != null;
            if (!(stack.getItem() instanceof SplashPotionItem) || !this.isStackPotion(stack)) continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public enum Mode {
        Turtle,
        Healing

    }

    public enum SwapMode {
        OFF,
        NORMAL,
        SILENT,
        Inventory,
        Pick

    }
}
