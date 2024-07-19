package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.entity.player.TravelEvent;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.MovementUtil;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraFlyModule
extends ToggleModule {
    final Config<Float> speed = new NumberConfig<>("Speed", "", 0.0f, 1.0f, 3.0f);
    final Config<Float> downSpeed = new NumberConfig<>("DownSpeed", "", 0.0f, 1.0f, 2.0f);
    final Config<Float> upPitch = new NumberConfig<>("UpPitch", "", 0.0f, 85.0f, 90.0f);
    final Config<Float> upFactor = new NumberConfig<>("UpFactor", "", 0.0f, 2.0f, 10.0f);
    final Config<Float> downFactor = new NumberConfig<>("DownFactor", "", 0.0f, 2.0f, 10.0f);
    final Config<Double> timeout = new NumberConfig<>("Timeout", "", 0.1, 0.5, 2.0);
    private boolean hasElytra = false;
    private final Timer instantFlyTimer = new CacheTimer();

    public ElytraFlyModule() {
        super("ElytraFly", "Allows you to fly freely using an elytra", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (ElytraFlyModule.mc.player != null) {
            if (!ElytraFlyModule.mc.player.isCreative()) {
                ElytraFlyModule.mc.player.getAbilities().allowFlying = false;
            }
            ElytraFlyModule.mc.player.getAbilities().flying = false;
        }
        this.hasElytra = false;
    }

    @Override
    public void onEnable() {
        if (ElytraFlyModule.mc.player != null) {
            if (!ElytraFlyModule.mc.player.isCreative()) {
                ElytraFlyModule.mc.player.getAbilities().allowFlying = false;
            }
            ElytraFlyModule.mc.player.getAbilities().flying = false;
        }
        this.hasElytra = false;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (ElytraFlyModule.nullCheck()) {
            return;
        }
        for (ItemStack is : ElytraFlyModule.mc.player.getArmorItems()) {
            if (is.getItem() instanceof ElytraItem) {
                this.hasElytra = true;
                break;
            }
            this.hasElytra = false;
        }
        if (!this.hasElytra) {
            return;
        }
        if (!ElytraFlyModule.mc.player.isFallFlying() && !ElytraFlyModule.mc.player.isOnGround() && ElytraFlyModule.mc.player.getVelocity().getY() < 0.0) {
            if (!this.instantFlyTimer.passed((long)(1000.0 * this.timeout.getValue()))) {
                return;
            }
            this.instantFlyTimer.reset();
            ElytraFlyModule.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(ElytraFlyModule.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public final Vec3d getRotationVec(float tickDelta) {
        return this.getRotationVector(-this.upPitch.getValue(), ElytraFlyModule.mc.player.getYaw(tickDelta));
    }

    @EventListener
    public void onMove(TravelEvent event) {
        if (ElytraFlyModule.nullCheck() || !this.hasElytra || !ElytraFlyModule.mc.player.isFallFlying()) {
            return;
        }
        event.cancel();
        Vec3d lookVec = this.getRotationVec(mc.getTickDelta());
        double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
        double motionDist = Math.sqrt(this.getX() * this.getX() + this.getZ() * this.getZ());
        if (ElytraFlyModule.mc.options.sneakKey.isPressed()) {
            this.setY(-this.downSpeed.getValue());
        } else if (!ElytraFlyModule.mc.player.input.jumping) {
            this.setY(-3.0E-14 * (double) this.downFactor.getValue());
        }
        if (ElytraFlyModule.mc.player.input.jumping) {
            if (motionDist > (double)(this.upFactor.getValue() / 10.0f)) {
                double rawUpSpeed = motionDist * 0.01325;
                this.setY(this.getY() + rawUpSpeed * 3.2);
                this.setX(this.getX() - lookVec.x * rawUpSpeed / lookDist);
                this.setZ(this.getZ() - lookVec.z * rawUpSpeed / lookDist);
            } else {
                double[] dir = MovementUtil.directionSpeed(this.speed.getValue());
                this.setX(dir[0]);
                this.setZ(dir[1]);
            }
        }
        if (lookDist > 0.0) {
            this.setX(this.getX() + (lookVec.x / lookDist * motionDist - this.getX()) * 0.1);
            this.setZ(this.getZ() + (lookVec.z / lookDist * motionDist - this.getZ()) * 0.1);
        }
        if (!ElytraFlyModule.mc.player.input.jumping) {
            double[] dir = MovementUtil.directionSpeed(this.speed.getValue());
            this.setX(dir[0]);
            this.setZ(dir[1]);
        }
        ElytraFlyModule.mc.player.move(MovementType.SELF, ElytraFlyModule.mc.player.getVelocity());
    }

    private void setX(double f) {
        MovementUtil.setMotionX(f);
    }

    private void setY(double f) {
        MovementUtil.setMotionY(f);
    }

    private void setZ(double f) {
        MovementUtil.setMotionZ(f);
    }

    private double getX() {
        return MovementUtil.getMotionX();
    }

    private double getY() {
        return MovementUtil.getMotionY();
    }

    private double getZ() {
        return MovementUtil.getMotionZ();
    }
}
