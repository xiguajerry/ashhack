package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.SprintCancelEvent;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.string.EnumFormatter;
import net.minecraft.entity.effect.StatusEffects;

public class SprintModule
extends ToggleModule {
    Config<SprintMode> modeConfig = new EnumConfig("Mode", "Sprinting mode. Rage allows for multi-directional sprinting.", (Enum)SprintMode.LEGIT, (Enum[])SprintMode.values());

    public SprintModule() {
        super("Sprint", "Automatically sprints", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(this.modeConfig.getValue());
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (!(!MovementUtil.isInputtingMovement() || SprintModule.mc.player.isInSneakingPose() || SprintModule.mc.player.isSneaking() || SprintModule.mc.player.isRiding() || SprintModule.mc.player.isTouchingWater() || SprintModule.mc.player.isInLava() || SprintModule.mc.player.isHoldingOntoLadder() || SprintModule.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || !((float)SprintModule.mc.player.getHungerManager().getFoodLevel() > 6.0f))) {
            switch (this.modeConfig.getValue()) {
                case LEGIT: {
                    if (!SprintModule.mc.player.input.hasForwardMovement() || SprintModule.mc.player.horizontalCollision && !SprintModule.mc.player.collidedSoftly) break;
                    SprintModule.mc.player.setSprinting(true);
                    break;
                }
                case RAGE: {
                    SprintModule.mc.player.setSprinting(true);
                }
            }
        }
    }

    @EventListener
    public void onSprintCancel(SprintCancelEvent event) {
        if (!(!MovementUtil.isInputtingMovement() || SprintModule.mc.player.isInSneakingPose() || SprintModule.mc.player.isSneaking() || SprintModule.mc.player.isRiding() || SprintModule.mc.player.isTouchingWater() || SprintModule.mc.player.isInLava() || SprintModule.mc.player.isHoldingOntoLadder() || SprintModule.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || !((float)SprintModule.mc.player.getHungerManager().getFoodLevel() > 6.0f) || this.modeConfig.getValue() != SprintMode.RAGE)) {
            event.cancel();
        }
    }

    public static enum SprintMode {
        LEGIT,
        RAGE;

    }
}
