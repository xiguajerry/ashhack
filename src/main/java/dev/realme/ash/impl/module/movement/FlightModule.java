package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.config.ConfigUpdateEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.string.EnumFormatter;

public class FlightModule
extends ToggleModule {
    Config<FlightMode> modeConfig = new EnumConfig("Mode", "The mode for vanilla flight", FlightMode.NORMAL, FlightMode.values());
    Config<Float> speedConfig = new NumberConfig<Float>("Speed", "The horizontal flight speed", 0.1f, 2.5f, 10.0f);
    Config<Float> vspeedConfig = new NumberConfig<Float>("VerticalSpeed", "The vertical flight speed", 0.1f, 1.0f, 5.0f);
    Config<Boolean> antiKickConfig = new BooleanConfig("AntiKick", "Prevents vanilla flight detection", true);
    Config<Boolean> accelerateConfig = new BooleanConfig("Accelerate", "Accelerate as you fly", false);
    Config<Float> accelerateSpeedConfig = new NumberConfig<Float>("AccelerateSpeed", "Speed to accelerate as", 0.01f, 0.2f, 1.0f, () -> this.accelerateConfig.getValue());
    Config<Float> maxSpeedConfig = new NumberConfig<Float>("MaxSpeed", "Max speed to acceleratee to", 1.0f, 5.0f, 10.0f, () -> this.accelerateConfig.getValue());
    private double speed;
    private final Timer antiKickTimer = new CacheTimer();
    private final Timer antiKick2Timer = new CacheTimer();

    public FlightModule() {
        super("Flight", "Allows the player to fly in survival", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(this.modeConfig.getValue());
    }

    @Override
    public void onEnable() {
        this.antiKickTimer.reset();
        this.antiKick2Timer.reset();
        if (this.modeConfig.getValue() == FlightMode.VANILLA) {
            this.enableVanillaFly();
        }
        this.speed = 0.0;
    }

    @Override
    public void onDisable() {
        if (this.modeConfig.getValue() == FlightMode.VANILLA) {
            this.disableVanillaFly();
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (this.accelerateConfig.getValue().booleanValue()) {
            if (!MovementUtil.isInputtingMovement() || FlightModule.mc.player.horizontalCollision) {
                this.speed = 0.0;
            }
            this.speed += this.accelerateSpeedConfig.getValue().floatValue();
            if (this.speed > (double)this.maxSpeedConfig.getValue().floatValue()) {
                this.speed = this.maxSpeedConfig.getValue().floatValue();
            }
        } else {
            this.speed = this.speedConfig.getValue().floatValue();
        }
        if (this.modeConfig.getValue().equals(FlightMode.VANILLA)) {
            FlightModule.mc.player.getAbilities().setFlySpeed((float)(this.speed * (double)0.05f));
        } else {
            FlightModule.mc.player.getAbilities().setFlySpeed(0.05f);
        }
        if (this.antiKickTimer.passed(3900) && this.antiKickConfig.getValue().booleanValue()) {
            MovementUtil.setMotionY(-0.04);
            this.antiKickTimer.reset();
        } else if (this.antiKick2Timer.passed(4000) && this.antiKickConfig.getValue().booleanValue()) {
            MovementUtil.setMotionY(0.04);
            this.antiKick2Timer.reset();
        } else if (this.modeConfig.getValue() == FlightMode.NORMAL) {
            MovementUtil.setMotionY(0.0);
            if (FlightModule.mc.options.jumpKey.isPressed()) {
                MovementUtil.setMotionY(this.vspeedConfig.getValue().floatValue());
            } else if (FlightModule.mc.options.sneakKey.isPressed()) {
                MovementUtil.setMotionY(-this.vspeedConfig.getValue().floatValue());
            }
        }
        if (this.modeConfig.getValue() == FlightMode.NORMAL) {
            this.speed = Math.max(this.speed, 0.2873f);
            float forward = FlightModule.mc.player.input.movementForward;
            float strafe = FlightModule.mc.player.input.movementSideways;
            float yaw = FlightModule.mc.player.getYaw();
            if (forward == 0.0f && strafe == 0.0f) {
                MovementUtil.setMotionXZ(0.0, 0.0);
                return;
            }
            double rx = Math.cos(Math.toRadians(yaw + 90.0f));
            double rz = Math.sin(Math.toRadians(yaw + 90.0f));
            MovementUtil.setMotionXZ((double)forward * this.speed * rx + (double)strafe * this.speed * rz, (double)forward * this.speed * rz - (double)strafe * this.speed * rx);
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event) {
        if (event.getConfig() == this.modeConfig && event.getStage() == EventStage.POST) {
            if (this.modeConfig.getValue() == FlightMode.VANILLA) {
                this.enableVanillaFly();
            } else {
                this.disableVanillaFly();
            }
        }
    }

    private void enableVanillaFly() {
        FlightModule.mc.player.getAbilities().allowFlying = true;
        FlightModule.mc.player.getAbilities().flying = true;
    }

    private void disableVanillaFly() {
        if (!FlightModule.mc.player.isCreative()) {
            FlightModule.mc.player.getAbilities().allowFlying = false;
        }
        FlightModule.mc.player.getAbilities().flying = false;
        FlightModule.mc.player.getAbilities().setFlySpeed(0.05f);
    }

    public enum FlightMode {
        NORMAL,
        VANILLA

    }
}
