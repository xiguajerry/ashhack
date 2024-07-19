package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.entity.LookDirectionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;

public class YawModule
extends ToggleModule {
    Config<Float> pitch = new NumberConfig<Float>("Pitch", "", -90.0f, 0.0f, 90.0f);
    Config<Boolean> lockConfig = new BooleanConfig("Lock", "Locks the yaw in cardinal direction", false);

    public YawModule() {
        super("Yaw", "Locks player yaw to a cardinal axis", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            float yaw = (float)Math.round(YawModule.mc.player.getYaw() / 45.0f) * 45.0f;
            Entity vehicle = YawModule.mc.player.getVehicle();
            if (vehicle != null) {
                vehicle.setYaw(yaw);
                if (vehicle instanceof LlamaEntity) {
                    LlamaEntity llama = (LlamaEntity) vehicle;
                    llama.setHeadYaw(yaw);
                }
                return;
            }
            YawModule.mc.player.setPitch(this.pitch.getValue().floatValue());
            YawModule.mc.player.setYaw(yaw);
            YawModule.mc.player.setHeadYaw(yaw);
        }
    }

    @EventListener
    public void onLookDirection(LookDirectionEvent event) {
        if (this.lockConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }
}
