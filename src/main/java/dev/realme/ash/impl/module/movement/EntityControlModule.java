package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.entity.passive.EntitySteerEvent;
import dev.realme.ash.impl.event.network.MountJumpStrengthEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;

public class EntityControlModule
extends ToggleModule {
    Config<Float> jumpStrengthConfig = new NumberConfig<Float>("JumpStrength", "The fixed jump strength of the mounted entity", 0.1f, 0.7f, 2.0f);
    Config<Boolean> noPigMoveConfig = new BooleanConfig("NoPigAI", "Prevents the pig movement when controlling pigs", false);

    public EntityControlModule() {
        super("EntityControl", "Allows you to steer entities without a saddle", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        Entity vehicle = EntityControlModule.mc.player.getVehicle();
        if (vehicle == null) {
            return;
        }
        vehicle.setYaw(EntityControlModule.mc.player.getYaw());
        if (vehicle instanceof LlamaEntity) {
            LlamaEntity llama = (LlamaEntity) vehicle;
            llama.headYaw = EntityControlModule.mc.player.getYaw();
        }
    }

    @EventListener
    public void onEntitySteer(EntitySteerEvent event) {
        event.cancel();
    }

    @EventListener
    public void onMountJumpStrength(MountJumpStrengthEvent event) {
        event.cancel();
        event.setJumpStrength(this.jumpStrengthConfig.getValue().floatValue());
    }
}
