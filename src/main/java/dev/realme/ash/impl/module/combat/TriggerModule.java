package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.imixin.IMinecraftClient;
import dev.realme.ash.init.Managers;
import dev.realme.ash.mixin.accessor.AccessorMinecraftClient;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TriggerModule
extends ToggleModule {
    Config<TriggerMode> modeConfig = new EnumConfig("Mode", "The mode for activating the trigger bot", TriggerMode.MOUSE_BUTTON, TriggerMode.values());
    Config<Float> attackSpeedConfig = new NumberConfig<Float>("AttackSpeed", "The speed to attack entities", 0.1f, 8.0f, 20.0f);
    Config<Float> randomSpeedConfig = new NumberConfig<Float>("RandomSpeed", "The speed randomizer for attacks", 0.1f, 2.0f, 10.0f);
    private final Timer triggerTimer = new CacheTimer();

    public TriggerModule() {
        super("Trigger", "Automatically attacks entities in the crosshair", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        boolean buttonDown = switch (this.modeConfig.getValue()) {
            case MOUSE_BUTTON -> TriggerModule.mc.mouse.wasLeftButtonClicked();
            case MOUSE_OVER -> {
                if (TriggerModule.mc.crosshairTarget == null || TriggerModule.mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
                    yield false;
                }
                EntityHitResult entityHit = (EntityHitResult)TriggerModule.mc.crosshairTarget;
                Entity crosshairEntity = entityHit.getEntity();
                yield !TriggerModule.mc.player.isTeammate(crosshairEntity) && !Managers.SOCIAL.isFriend(crosshairEntity.getName());
            }
            case MOUSE_CLICK -> true;
        };
        double d = Math.random() * (double)this.randomSpeedConfig.getValue().floatValue() * 2.0 - (double)this.randomSpeedConfig.getValue().floatValue();
        if (buttonDown && this.triggerTimer.passed(1000.0 - Math.max((double)this.attackSpeedConfig.getValue().floatValue() + d, 0.5) * 50.0)) {
            ((IMinecraftClient) mc).leftClick();
            ((AccessorMinecraftClient) mc).hookSetAttackCooldown(0);
            this.triggerTimer.reset();
        }
    }

    public enum TriggerMode {
        MOUSE_BUTTON,
        MOUSE_OVER,
        MOUSE_CLICK

    }
}
