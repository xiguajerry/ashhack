package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import java.text.DecimalFormat;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntitySpeedModule
extends ToggleModule {
    final Config<Float> speedConfig = new NumberConfig<>("Speed", "The speed of the entity while moving", 0.1f, 0.5f, 4.0f);
    final Config<Boolean> antiStuckConfig = new BooleanConfig("AntiStuck", "Prevents entities from getting stuck when moving up", false);
    final Config<Boolean> strictConfig = new BooleanConfig("Strict", "The NCP-Updated bypass for speeding up entity movement", false);
    private final Timer entityJumpTimer = new CacheTimer();

    public EntitySpeedModule() {
        super("EntitySpeed", "Increases riding entity speeds", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        DecimalFormat decimal = new DecimalFormat("0.0");
        return decimal.format(this.speedConfig.getValue());
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (EntitySpeedModule.mc.player.isRiding() && EntitySpeedModule.mc.player.getControllingVehicle() != null) {
            double d = Math.cos(Math.toRadians(EntitySpeedModule.mc.player.getYaw() + 90.0f));
            double d2 = Math.sin(Math.toRadians(EntitySpeedModule.mc.player.getYaw() + 90.0f));
            BlockPos pos1 = BlockPos.ofFloored(EntitySpeedModule.mc.player.getX() + 2.0 * d, EntitySpeedModule.mc.player.getY() - 1.0, EntitySpeedModule.mc.player.getZ() + 2.0 * d2);
            BlockPos pos2 = BlockPos.ofFloored(EntitySpeedModule.mc.player.getX() + 2.0 * d, EntitySpeedModule.mc.player.getY() - 2.0, EntitySpeedModule.mc.player.getZ() + 2.0 * d2);
            if (this.antiStuckConfig.getValue() && !EntitySpeedModule.mc.player.getControllingVehicle().isOnGround() && !EntitySpeedModule.mc.world.getBlockState(pos1).blocksMovement() && !EntitySpeedModule.mc.world.getBlockState(pos2).blocksMovement()) {
                this.entityJumpTimer.reset();
                return;
            }
            BlockPos pos3 = BlockPos.ofFloored(EntitySpeedModule.mc.player.getX() + 2.0 * d, EntitySpeedModule.mc.player.getY(), EntitySpeedModule.mc.player.getZ() + 2.0 * d2);
            if (this.antiStuckConfig.getValue() && EntitySpeedModule.mc.world.getBlockState(pos3).blocksMovement()) {
                this.entityJumpTimer.reset();
                return;
            }
            BlockPos pos4 = BlockPos.ofFloored(EntitySpeedModule.mc.player.getX() + d, EntitySpeedModule.mc.player.getY() + 1.0, EntitySpeedModule.mc.player.getZ() + d2);
            if (this.antiStuckConfig.getValue() && EntitySpeedModule.mc.world.getBlockState(pos4).blocksMovement()) {
                this.entityJumpTimer.reset();
                return;
            }
            if (EntitySpeedModule.mc.player.input.jumping) {
                this.entityJumpTimer.reset();
            }
            if (this.entityJumpTimer.passed(10000) || !this.antiStuckConfig.getValue()) {
                if (!EntitySpeedModule.mc.player.getControllingVehicle().isTouchingWater() || EntitySpeedModule.mc.player.input.jumping || !this.entityJumpTimer.passed(1000)) {
                    if (EntitySpeedModule.mc.player.getControllingVehicle().isOnGround()) {
                        EntitySpeedModule.mc.player.getControllingVehicle().setVelocity(EntitySpeedModule.mc.player.getVelocity().x, 0.4, EntitySpeedModule.mc.player.getVelocity().z);
                    }
                    EntitySpeedModule.mc.player.getControllingVehicle().setVelocity(EntitySpeedModule.mc.player.getVelocity().x, -0.4, EntitySpeedModule.mc.player.getVelocity().z);
                }
                if (this.strictConfig.getValue()) {
                    Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.interact(EntitySpeedModule.mc.player.getControllingVehicle(), false, Hand.MAIN_HAND));
                }
                this.handleEntityMotion(this.speedConfig.getValue(), d, d2);
                this.entityJumpTimer.reset();
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (EntitySpeedModule.mc.player == null || !EntitySpeedModule.mc.player.isRiding() || EntitySpeedModule.mc.options.sneakKey.isPressed() || EntitySpeedModule.mc.player.getControllingVehicle() == null) {
            return;
        }
        if (this.strictConfig.getValue()) {
            if (event.getPacket() instanceof EntityPassengersSetS2CPacket) {
                event.cancel();
            } else if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
                event.cancel();
            }
        }
    }

    private void handleEntityMotion(float entitySpeed, double d, double d2) {
        Vec3d motion = EntitySpeedModule.mc.player.getControllingVehicle().getVelocity();
        float forward = EntitySpeedModule.mc.player.input.movementForward;
        float strafe = EntitySpeedModule.mc.player.input.movementSideways;
        if (forward == 0.0f && strafe == 0.0f) {
            EntitySpeedModule.mc.player.getControllingVehicle().setVelocity(0.0, motion.y, 0.0);
            return;
        }
        EntitySpeedModule.mc.player.getControllingVehicle().setVelocity((double)(forward * entitySpeed) * d + (double)(strafe * entitySpeed) * d2, motion.y, (double)(forward * entitySpeed) * d2 - (double)(strafe * entitySpeed) * d);
    }
}
