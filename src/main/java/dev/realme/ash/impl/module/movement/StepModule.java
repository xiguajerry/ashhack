package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.string.EnumFormatter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class StepModule
extends ToggleModule {
    Config<StepMode> modeConfig = new EnumConfig("Mode", "Step mode", (Enum)StepMode.NORMAL, (Enum[])StepMode.values());
    Config<Float> heightConfig = new NumberConfig<Float>("Height", "The maximum height for stepping up blocks", Float.valueOf(1.0f), Float.valueOf(2.5f), Float.valueOf(10.0f));
    Config<Boolean> useTimerConfig = new BooleanConfig("UseTimer", "Slows down packets by applying timer when stepping", true);
    Config<Boolean> strictConfig = new BooleanConfig("Strict", "Confirms the step height for NCP servers", false, () -> this.heightConfig.getValue().floatValue() <= 2.5f);
    Config<Boolean> entityStepConfig = new BooleanConfig("EntityStep", "Allows entities to step up blocks", false);
    Config<Boolean> pauseInside = new BooleanConfig("PauseInside", "Allows entities to step up blocks", false);
    private boolean cancelTimer;
    private final Timer stepTimer = new CacheTimer();

    public StepModule() {
        super("Step", "Allows the player to step up blocks", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(this.modeConfig.getValue());
    }

    @Override
    public void onDisable() {
        if (StepModule.mc.player == null) {
            return;
        }
        this.setStepHeight(this.isAbstractHorse(StepModule.mc.player.getVehicle()) ? 1.0f : 0.6f);
        Managers.TICK.setClientTick(1.0f);
    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (PlayerUtil.isInsideBlock() && this.pauseInside.getValue().booleanValue()) {
            this.setStepHeight(0.6f);
            return;
        }
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (this.modeConfig.getValue() == StepMode.NORMAL) {
            double stepHeight = StepModule.mc.player.getY() - StepModule.mc.player.prevY;
            if (stepHeight <= 0.5 || stepHeight > (double)this.heightConfig.getValue().floatValue()) {
                return;
            }
            double[] offs = this.getStepOffsets(stepHeight);
            if (this.useTimerConfig.getValue().booleanValue()) {
                Managers.TICK.setClientTick(stepHeight > 1.0 ? 0.15f : 0.35f);
                this.cancelTimer = true;
            }
            for (double off : offs) {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(StepModule.mc.player.prevX, StepModule.mc.player.prevY + off, StepModule.mc.player.prevZ, false));
            }
            this.stepTimer.reset();
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (PlayerUtil.isInsideBlock() && this.pauseInside.getValue().booleanValue()) {
            this.setStepHeight(0.6f);
            return;
        }
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (StepModule.mc.player.isTouchingWater() || StepModule.mc.player.isInLava() || StepModule.mc.player.isFallFlying()) {
            Managers.TICK.setClientTick(1.0f);
            this.setStepHeight(this.isAbstractHorse(StepModule.mc.player.getVehicle()) ? 1.0f : 0.6f);
            return;
        }
        if (this.cancelTimer && StepModule.mc.player.isOnGround()) {
            Managers.TICK.setClientTick(1.0f);
            this.cancelTimer = false;
        }
        if (StepModule.mc.player.isOnGround() && this.stepTimer.passed(200)) {
            this.setStepHeight(this.heightConfig.getValue().floatValue());
        } else {
            this.setStepHeight(this.isAbstractHorse(StepModule.mc.player.getVehicle()) ? 1.0f : 0.6f);
        }
    }

    private void setStepHeight(float stepHeight) {
        if (this.entityStepConfig.getValue().booleanValue() && StepModule.mc.player.getVehicle() != null) {
            StepModule.mc.player.getVehicle().setStepHeight(stepHeight);
        } else {
            StepModule.mc.player.setStepHeight(stepHeight);
        }
    }

    private double[] getStepOffsets(double stepHeight) {
        double[] offsets = new double[]{};
        if (this.strictConfig.getValue().booleanValue()) {
            if (stepHeight > 1.1661) {
                offsets = new double[]{0.42, 0.7532, 1.001, 1.1661, stepHeight};
            } else if (stepHeight > 1.015) {
                offsets = new double[]{0.42, 0.7532, 1.001, stepHeight};
            } else if (stepHeight > 0.6) {
                offsets = new double[]{0.42 * stepHeight, 0.7532 * stepHeight, stepHeight};
            }
            return offsets;
        }
        if (stepHeight > 2.019) {
            offsets = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.919};
        } else if (stepHeight > 1.5) {
            offsets = new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
        } else if (stepHeight > 1.015) {
            offsets = new double[]{0.42, 0.7532, 1.01, 1.093, 1.015};
        } else if (stepHeight > 0.6) {
            offsets = new double[]{0.42 * stepHeight, 0.7532 * stepHeight};
        }
        return offsets;
    }

    private boolean isAbstractHorse(Entity e) {
        return e instanceof HorseEntity || e instanceof LlamaEntity || e instanceof MuleEntity;
    }

    public static enum StepMode {
        VANILLA,
        NORMAL,
        A_A_C;

    }
}
