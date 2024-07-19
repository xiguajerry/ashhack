package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoBowReleaseModule
extends ToggleModule {
    final Config<Boolean> crossbowConfig = new BooleanConfig("Crossbow", "Automatically releases crossbow when fully charged", false);
    final Config<Integer> ticksConfig = new NumberConfig<>("Ticks", "Ticks before releasing the bow charge", 3, 5, 20);
    final Config<Boolean> tpsSyncConfig = new BooleanConfig("TPS-Sync", "Sync bow release to server ticks", false);

    public AutoBowReleaseModule() {
        super("AutoBowRelease", "Automatically releases a charged bow", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (Modules.SELF_BOW.isEnabled()) {
            return;
        }
        if (event.getStage() == EventStage.POST) {
            assert AutoBowReleaseModule.mc.player != null;
            ItemStack mainhand = AutoBowReleaseModule.mc.player.getMainHandStack();
            if (mainhand.getItem() == Items.BOW) {
                float off;
                float f = off = this.tpsSyncConfig.getValue() ? 20.0f - Managers.TICK.getTpsAverage() : 0.0f;
                if ((float)AutoBowReleaseModule.mc.player.getItemUseTime() + off >= (float) this.ticksConfig.getValue()) {
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                    AutoBowReleaseModule.mc.player.stopUsingItem();
                }
            } else if (this.crossbowConfig.getValue() && mainhand.getItem() == Items.CROSSBOW && (float)AutoBowReleaseModule.mc.player.getItemUseTime() / (float)CrossbowItem.getPullTime(AutoBowReleaseModule.mc.player.getMainHandStack()) > 1.0f) {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                AutoBowReleaseModule.mc.player.stopUsingItem();
                assert AutoBowReleaseModule.mc.interactionManager != null;
                AutoBowReleaseModule.mc.interactionManager.interactItem(AutoBowReleaseModule.mc.player, Hand.MAIN_HAND);
            }
        }
    }
}
