package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.entity.player.PlayerMoveEvent;
import dev.realme.ash.util.player.PlayerUtil;

public class BurrowStrafeModule
extends ToggleModule {
    Config<Float> speed = new NumberConfig<Float>("Speed", "The speed for alternative modes", Float.valueOf(0.0f), Float.valueOf(10.0f), Float.valueOf(20.0f));

    public BurrowStrafeModule() {
        super("BurrowStrafe", "Move in block", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event) {
        event.cancel();
        if (!PlayerUtil.isInsideBlock()) {
            return;
        }
        if (PlayerUtil.isInWeb(BurrowStrafeModule.mc.player)) {
            return;
        }
        double speed = this.speed.getValue().floatValue();
        double moveSpeed = 0.002873 * speed;
        double n = BurrowStrafeModule.mc.player.input.movementForward;
        double n2 = BurrowStrafeModule.mc.player.input.movementSideways;
        double n3 = BurrowStrafeModule.mc.player.getYaw();
        if (n == 0.0 && n2 == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
            return;
        }
        if (n != 0.0 && n2 != 0.0) {
            n *= Math.sin(0.7853981633974483);
            n2 *= Math.cos(0.7853981633974483);
        }
        event.setX(n * moveSpeed * -Math.sin(Math.toRadians(n3)) + n2 * moveSpeed * Math.cos(Math.toRadians(n3)));
        event.setZ(n * moveSpeed * Math.cos(Math.toRadians(n3)) - n2 * moveSpeed * -Math.sin(Math.toRadians(n3)));
    }
}
