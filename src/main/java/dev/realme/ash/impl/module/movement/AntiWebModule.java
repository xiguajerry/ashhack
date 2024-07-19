package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.player.PlayerUtil;

public class AntiWebModule
extends ToggleModule {
    Config<Float> ySpeed = new NumberConfig<Float>("DownSpeed", "", 0.0f, 1.5f, 5.0f);
    Config<Float> xzSpeed = new NumberConfig<Float>("XZSpeed", "", 0.0f, 2.0f, 10.0f);

    public AntiWebModule() {
        super("AntiWeb", "mio client", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (AntiWebModule.mc.player == null || AntiWebModule.mc.world == null) {
            return;
        }
        if (PlayerUtil.isInWeb(AntiWebModule.mc.player)) {
            double[] xz = MovementUtil.directionSpeed(this.xzSpeed.getValue().floatValue() / 10.0f);
            double y = AntiWebModule.mc.options.sneakKey.isPressed() && !AntiWebModule.mc.player.isOnGround() ? (double)(-this.ySpeed.getValue().floatValue()) : AntiWebModule.mc.player.getVelocity().y;
            AntiWebModule.mc.player.setVelocity(xz[0], y, xz[1]);
        }
    }
}
