package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.item.TridentPullbackEvent;
import dev.realme.ash.impl.event.item.TridentWaterEvent;
import dev.realme.ash.init.Managers;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TridentFlyModule
extends ToggleModule {
    Config<Boolean> allowNoWaterConfig = new BooleanConfig("AllowNoWater", "Allows you to fly using tridents even without water", true);
    Config<Boolean> instantConfig = new BooleanConfig("Instant", "Removes the pullback of the trident", true);
    Config<Boolean> flyConfig = new BooleanConfig("Spam", "Automatically uses riptide", false);
    Config<Integer> ticksConfig = new NumberConfig<Integer>("Ticks", "The ticks between riptide boost", Integer.valueOf(0), Integer.valueOf(3), Integer.valueOf(20), () -> this.flyConfig.getValue());

    public TridentFlyModule() {
        super("TridentFly", "Allows you to fly using tridents", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE || !this.flyConfig.getValue().booleanValue()) {
            return;
        }
        if (TridentFlyModule.mc.player.getMainHandStack().getItem() == Items.TRIDENT && TridentFlyModule.mc.player.getItemUseTime() >= this.ticksConfig.getValue()) {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            TridentFlyModule.mc.player.stopUsingItem();
        }
    }

    @EventListener
    public void onTridentPullback(TridentPullbackEvent event) {
        if (this.instantConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onTridentWaterCheck(TridentWaterEvent event) {
        if (this.allowNoWaterConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }
}