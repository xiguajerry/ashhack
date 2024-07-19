package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.entity.player.PlayerMoveEvent;
import dev.realme.ash.impl.event.network.TickMovementEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.MovementUtil;
import net.minecraft.util.math.Box;

public class FastFallModule
extends ToggleModule {
    final Config<Float> heightConfig = new NumberConfig<>("Height", "The maximum fall height", 1.0f, 3.0f, 10.0f);
    final Config<FallMode> fallModeConfig = new EnumConfig<>("Mode", "The mode for falling down blocks", FallMode.STEP, FallMode.values());
    final Config<Integer> shiftTicksConfig = new NumberConfig<>("ShiftTicks", "Number of ticks to shift ahead", 1, 3, 5, () -> this.fallModeConfig.getValue() == FallMode.SHIFT);
    private boolean prevOnGround;
    private boolean cancelFallMovement;
    private int fallTicks;
    private final Timer fallTimer = new CacheTimer();

    public FastFallModule() {
        super("FastFall", "Falls down blocks faster", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable() {
        this.cancelFallMovement = false;
        this.fallTicks = 0;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            this.prevOnGround = FastFallModule.mc.player.isOnGround();
            if (this.fallModeConfig.getValue() == FallMode.STEP) {
                if (FastFallModule.mc.player.isRiding() || FastFallModule.mc.player.isFallFlying() || FastFallModule.mc.player.isHoldingOntoLadder() || FastFallModule.mc.player.isInLava() || FastFallModule.mc.player.isTouchingWater() || FastFallModule.mc.player.input.jumping || FastFallModule.mc.player.input.sneaking) {
                    return;
                }
                if (Modules.SPEED.isEnabled() || Modules.LONG_JUMP.isEnabled() || Modules.FLIGHT.isEnabled() || Modules.PACKET_FLY.isEnabled()) {
                    return;
                }
                if (FastFallModule.mc.player.isOnGround() && this.isNearestBlockWithinHeight(this.heightConfig.getValue())) {
                    MovementUtil.setMotionY(-3.0);
                }
            }
        }
    }

    @EventListener
    public void onTickMovement(TickMovementEvent event) {
        if (this.fallModeConfig.getValue() == FallMode.SHIFT) {
            if (FastFallModule.mc.player.isRiding() || FastFallModule.mc.player.isFallFlying() || FastFallModule.mc.player.isHoldingOntoLadder() || FastFallModule.mc.player.isInLava() || FastFallModule.mc.player.isTouchingWater() || FastFallModule.mc.player.input.jumping || FastFallModule.mc.player.input.sneaking) {
                return;
            }
            if (!Managers.ANTICHEAT.hasPassed(1000L) || !this.fallTimer.passed(1000) || Modules.SPEED.isEnabled() || Modules.LONG_JUMP.isEnabled() || Modules.FLIGHT.isEnabled() || Modules.PACKET_FLY.isEnabled()) {
                return;
            }
            if (FastFallModule.mc.player.getVelocity().y < 0.0 && this.prevOnGround && !FastFallModule.mc.player.isOnGround() && this.isNearestBlockWithinHeight((double) this.heightConfig.getValue() + 0.01)) {
                this.fallTimer.reset();
                event.cancel();
                event.setIterations(this.shiftTicksConfig.getValue());
                this.cancelFallMovement = true;
                this.fallTicks = 0;
            }
        }
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Modules.FLIGHT.isEnabled() || Modules.PACKET_FLY.isEnabled()) {
            return;
        }
        if (this.cancelFallMovement && this.fallModeConfig.getValue() == FallMode.SHIFT) {
            event.setX(0.0);
            event.setZ(0.0);
            MovementUtil.setMotionXZ(0.0, 0.0);
            ++this.fallTicks;
            if (this.fallTicks > this.shiftTicksConfig.getValue()) {
                this.cancelFallMovement = false;
                this.fallTicks = 0;
            }
        }
    }

    private boolean isNearestBlockWithinHeight(double height) {
        Box bb = FastFallModule.mc.player.getBoundingBox();
        for (double i = 0.0; i < height + 0.5; i += 0.01) {
            if (FastFallModule.mc.world.isSpaceEmpty(FastFallModule.mc.player, bb.offset(0.0, -i, 0.0))) continue;
            return true;
        }
        return false;
    }

    public enum FallMode {
        STEP,
        SHIFT

    }
}
