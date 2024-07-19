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
    final Config<YawMode> yawModeConfig = new EnumConfig<>("Yaw", "The mode for the rotation yaw spin ", YawMode.SPIN, YawMode.values());
    final Config<PitchMode> pitchModeConfig = new EnumConfig<>("Pitch", "The mode for the rotation pitch spin", PitchMode.DOWN, PitchMode.values());
    final Config<Float> yawAddConfig = new NumberConfig<>("YawAdd", "The yaw to add during each rotation", -180.0f, 20.0f, 180.0f);
    final Config<Float> pitchAddConfig = new NumberConfig<>("CustomPitch", "The pitch to add during each rotation", -90.0f, 20.0f, 90.0f);
    final Config<Float> spinSpeedConfig = new NumberConfig<>("SpinSpeed", "The yaw speed to rotate", 1.0f, 16.0f, 40.0f);
    final Config<Integer> flipTicksConfig = new NumberConfig<>("FlipTicks", "The number of ticks to wait between jitter", 2, 2, 20);
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
            case OFF -> {
                assert AntiAimModule.mc.player != null;
                yield AntiAimModule.mc.player.getYaw();
            }
            case STATIC -> {
                assert AntiAimModule.mc.player != null;
                yield AntiAimModule.mc.player.getYaw() + this.yawAddConfig.getValue();
            }
            case ZERO -> this.prevYaw;
            case SPIN -> {
                float spin = this.yaw + this.spinSpeedConfig.getValue();
                if (spin > 360.0f) {
                    yield spin - 360.0f;
                }
                yield spin;
            }
            case JITTER -> {
                assert AntiAimModule.mc.player != null;
                yield AntiAimModule.mc.player.getYaw() + (AntiAimModule.mc.player.age % this.flipTicksConfig.getValue() == 0 ? this.yawAddConfig.getValue() : -this.yawAddConfig.getValue());
            }
        };
        this.pitch = switch (this.pitchModeConfig.getValue()) {
            default -> throw new IncompatibleClassChangeError();
            case OFF -> {
                assert AntiAimModule.mc.player != null;
                yield AntiAimModule.mc.player.getPitch();
            }
            case STATIC -> this.pitchAddConfig.getValue();
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
