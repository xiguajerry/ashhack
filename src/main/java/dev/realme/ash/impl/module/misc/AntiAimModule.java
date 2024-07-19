package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.PlayerTickEvent;

public class AntiAimModule
extends RotationModule {
    Config<YawMode> yawModeConfig = new EnumConfig("Yaw", "The mode for the rotation yaw spin ", YawMode.SPIN, YawMode.values());
    Config<PitchMode> pitchModeConfig = new EnumConfig("Pitch", "The mode for the rotation pitch spin", PitchMode.DOWN, PitchMode.values());
    Config<Float> yawAddConfig = new NumberConfig<Float>("YawAdd", "The yaw to add during each rotation", -180.0f, 20.0f, 180.0f);
    Config<Float> pitchAddConfig = new NumberConfig<Float>("CustomPitch", "The pitch to add during each rotation", -90.0f, 20.0f, 90.0f);
    Config<Float> spinSpeedConfig = new NumberConfig<Float>("SpinSpeed", "The yaw speed to rotate", 1.0f, 16.0f, 40.0f);
    Config<Integer> flipTicksConfig = new NumberConfig<Integer>("FlipTicks", "The number of ticks to wait between jitter", 2, 2, 20);
    private float yaw;
    private float pitch;
    private float prevYaw;
    private float prevPitch;

    public AntiAimModule() {
        super("AntiAim", "Makes it harder to accurately aim at the player", ModuleCategory.MISCELLANEOUS, 50);
    }

    @Override
    public void onEnable() {
        if (AntiAimModule.mc.player == null) {
            return;
        }
        this.prevYaw = AntiAimModule.mc.player.getYaw();
        this.prevPitch = AntiAimModule.mc.player.getPitch();
    }

    @EventListener
    public void onPlayerUpdate(PlayerTickEvent event) {
        if (AntiAimModule.mc.options.attackKey.isPressed() || AntiAimModule.mc.options.useKey.isPressed()) {
            return;
        }
        this.yaw = switch (this.yawModeConfig.getValue()) {
            case OFF -> AntiAimModule.mc.player.getYaw();
            case STATIC -> AntiAimModule.mc.player.getYaw() + this.yawAddConfig.getValue().floatValue();
            case ZERO -> this.prevYaw;
            case SPIN -> {
                float spin = this.yaw + this.spinSpeedConfig.getValue().floatValue();
                if (spin > 360.0f) {
                    yield spin - 360.0f;
                }
                yield spin;
            }
            case JITTER -> AntiAimModule.mc.player.getYaw() + (AntiAimModule.mc.player.age % this.flipTicksConfig.getValue() == 0 ? this.yawAddConfig.getValue().floatValue() : -this.yawAddConfig.getValue().floatValue());
        };
        this.pitch = switch (this.pitchModeConfig.getValue()) {
            default -> throw new IncompatibleClassChangeError();
            case OFF -> AntiAimModule.mc.player.getPitch();
            case STATIC -> this.pitchAddConfig.getValue().floatValue();
            case ZERO -> this.prevPitch;
            case UP -> -90.0f;
            case DOWN -> 90.0f;
            case JITTER -> {
                float jitter = this.pitch + 30.0f;
                if (jitter > 90.0f) {
                    yield -90.0f;
                }
                if (jitter < -90.0f) {
                    yield 90.0f;
                }
                yield jitter;
            }
        };
        this.setRotation(this.yaw, this.pitch);
    }

    public enum YawMode {
        OFF,
        STATIC,
        ZERO,
        SPIN,
        JITTER

    }

    public enum PitchMode {
        OFF,
        STATIC,
        ZERO,
        UP,
        DOWN,
        JITTER

    }
}
