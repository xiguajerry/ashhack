package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.network.SetCurrentHandEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.string.EnumFormatter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class FastEatModule
extends ToggleModule {
    Config<Mode> modeConfig = new EnumConfig("Mode", "The bypass mode", (Enum)Mode.VANILLA, (Enum[])Mode.values());
    Config<Integer> ticksConfig = new NumberConfig<Integer>("Ticks", "The amount of ticks to have 'consumed' an item before fast eating", 0, 10, 30);
    private int packets;

    public FastEatModule() {
        super("FastEat", "Allows you to consume items faster", ModuleCategory.WORLD);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(this.modeConfig.getValue());
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (MovementUtil.isMoving() || !FastEatModule.mc.player.isOnGround()) {
            --this.packets;
            if (this.packets <= 0) {
                this.packets = 0;
            }
        } else {
            ++this.packets;
            if (this.packets > 100) {
                this.packets = 100;
            }
        }
        if (!FastEatModule.mc.player.isUsingItem()) {
            return;
        }
        ItemStack stack = FastEatModule.mc.player.getStackInHand(FastEatModule.mc.player.getActiveHand());
        if (stack.isEmpty() || !stack.getItem().isFood()) {
            return;
        }
        int timeUsed = FastEatModule.mc.player.getItemUseTime();
        if (timeUsed >= this.ticksConfig.getValue()) {
            int usePackets = 32 - timeUsed;
            for (int i = 0; i < usePackets; ++i) {
                switch (this.modeConfig.getValue()) {
                    // Empty switch
                }
            }
        }
    }

    @EventListener
    public void onSetCurrentHand(SetCurrentHandEvent event) {
        if (this.modeConfig.getValue() == Mode.SHIFT) {
            ItemStack stack = event.getStackInHand();
            if (!stack.getItem().isFood() && !(stack.getItem() instanceof PotionItem)) {
                return;
            }
            int maxUseTime = stack.getMaxUseTime();
            if (this.packets < maxUseTime) {
                return;
            }
            for (int i = 0; i < maxUseTime; ++i) {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(FastEatModule.mc.player.getX(), FastEatModule.mc.player.getY(), FastEatModule.mc.player.getZ(), FastEatModule.mc.player.isOnGround()));
                this.packets -= maxUseTime;
            }
            event.cancel();
            stack.getItem().finishUsing(stack, FastEatModule.mc.world, FastEatModule.mc.player);
        }
    }

    private static enum Mode {
        VANILLA,
        SHIFT,
        GRIM;

    }
}
