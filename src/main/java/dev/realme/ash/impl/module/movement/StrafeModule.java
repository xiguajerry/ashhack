package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.entity.player.PlayerMoveEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.player.PlayerUtil;
import net.minecraft.util.math.Vec2f;

public class StrafeModule
extends ToggleModule {
    Config<Float> speedConfig = new NumberConfig<Float>("Speed", "The speed for alternative modes", 0.0f, 5.5f, 10.0f);

    public StrafeModule() {
        super("Strafe", "Move ice", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event) {
        if (StrafeModule.mc.player.isRiding() || Modules.SPEED.isEnabled() || Modules.FLIGHT.isEnabled() || StrafeModule.mc.player.isFallFlying() || StrafeModule.mc.player.isHoldingOntoLadder() || StrafeModule.mc.player.isInLava() || StrafeModule.mc.player.isTouchingWater()) {
            return;
        }
        event.cancel();
        if (PlayerUtil.isInsideBlock()) {
            return;
        }
        if (PlayerUtil.isInWeb(StrafeModule.mc.player)) {
            return;
        }
        if (!MovementUtil.isMoving()) {
            event.setX(0.0);
            event.setY(StrafeModule.mc.player.getVelocity().y);
            event.setZ(0.0);
        }
        Vec2f motion = this.handleStrafeMotion(this.speedConfig.getValue().floatValue() / 10.0f);
        event.setX(motion.x);
        event.setZ(motion.y);
    }

    public Vec2f handleStrafeMotion(float speed) {
        float forward = StrafeModule.mc.player.input.movementForward;
        float strafe = StrafeModule.mc.player.input.movementSideways;
        float yaw = StrafeModule.mc.player.prevYaw + (StrafeModule.mc.player.getYaw() - StrafeModule.mc.player.prevYaw) * mc.getTickDelta();
        if (forward == 0.0f && strafe == 0.0f) {
            return Vec2f.ZERO;
        }
        if (forward != 0.0f) {
            if (strafe >= 1.0f) {
                yaw += forward > 0.0f ? -45.0f : 45.0f;
                strafe = 0.0f;
            } else if (strafe <= -1.0f) {
                yaw += forward > 0.0f ? 45.0f : -45.0f;
                strafe = 0.0f;
            }
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        float rx = (float)Math.cos(Math.toRadians(yaw));
        float rz = (float)(-Math.sin(Math.toRadians(yaw)));
        return new Vec2f(forward * speed * rz + strafe * speed * rx, forward * speed * rx - strafe * speed * rz);
    }
}
