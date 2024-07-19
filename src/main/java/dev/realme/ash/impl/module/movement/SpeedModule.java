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
import dev.realme.ash.impl.event.config.ConfigUpdateEvent;
import dev.realme.ash.impl.event.entity.player.PlayerMoveEvent;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.math.MathUtil;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.string.EnumFormatter;
import dev.realme.ash.util.world.FakePlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class SpeedModule
extends ToggleModule {
    Config<Speed> speedModeConfig = new EnumConfig<>("Mode", "Speed mode", Speed.STRAFE, Speed.values());
    Config<Boolean> vanillaStrafeConfig = new BooleanConfig("Strafe-Vanilla", "Applies strafe speeds to vanilla speed", false, () -> this.speedModeConfig.getValue() == Speed.VANILLA);
    Config<Float> speedConfig = new NumberConfig<>("Speed", "The speed for alternative modes", 0.1f, 4.0f, 10.0f);
    Config<Boolean> timerConfig = new BooleanConfig("UseTimer", "Uses timer to increase acceleration", false);
    Config<Boolean> strafeBoostConfig = new BooleanConfig("StrafeBoost", "Uses explosion velocity to boost Strafe", false);
    Config<Float> multiply = new NumberConfig<>("Multiply", "", 0.1f, 1.5f, 10.0f);
    Config<Integer> boostTicksConfig = new NumberConfig<>("BoostTicks", "The number of ticks to boost strafe", 10, 20, 40, () -> this.strafeBoostConfig.getValue());
    Config<Boolean> speedWaterConfig = new BooleanConfig("SpeedInWater", "Applies speed even in water and lava", false);
    private int strafe = 4;
    private boolean accel;
    private int strictTicks;
    private int boostTicks;
    private double speed;
    private double boostSpeed;
    private double getDistance;
    private boolean prevTimer;

    public SpeedModule() {
        super("Speed", "Move faster", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        if (this.speedModeConfig.getValue() == Speed.GRIM_COLLIDE) {
            return "Grim";
        }
        return EnumFormatter.formatEnum(this.speedModeConfig.getValue());
    }

    @Override
    public void onEnable() {
        this.prevTimer = Modules.TIMER.isEnabled();
        if (this.timerConfig.getValue().booleanValue() && !this.prevTimer && this.isStrafe()) {
            Modules.TIMER.enable();
        }
    }

    @Override
    public void onDisable() {
        this.resetStrafe();
        if (Modules.TIMER.isEnabled()) {
            Modules.TIMER.resetTimer();
            if (!this.prevTimer) {
                Modules.TIMER.disable();
            }
        }
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        this.disable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            ++this.boostTicks;
            if (this.boostTicks > this.boostTicksConfig.getValue()) {
                this.boostSpeed = 0.0;
            }
            double dx = SpeedModule.mc.player.getX() - SpeedModule.mc.player.prevX;
            double dz = SpeedModule.mc.player.getZ() - SpeedModule.mc.player.prevZ;
            this.getDistance = Math.sqrt(dx * dx + dz * dz);
            if (this.speedModeConfig.getValue() == Speed.GRIM_COLLIDE && MovementUtil.isInputtingMovement()) {
                int collisions = 0;
                for (Entity entity : SpeedModule.mc.world.getEntities()) {
                    if (!this.checkIsCollidingEntity(entity) || !((double)MathHelper.sqrt((float)SpeedModule.mc.player.squaredDistanceTo(entity)) <= 1.5)) continue;
                    ++collisions;
                }
                if (collisions > 0) {
                    Vec3d velocity = SpeedModule.mc.player.getVelocity();
                    double factor = 0.08 * (double)collisions;
                    Vec2f strafe = this.handleStrafeMotion((float)factor);
                    SpeedModule.mc.player.setVelocity(velocity.x + (double)strafe.x, velocity.y, velocity.z + (double)strafe.y);
                }
            }
        }
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event) {
        if (SpeedModule.mc.player != null && SpeedModule.mc.world != null) {
            double amplifier;
            if (!MovementUtil.isInputtingMovement() || Modules.FLIGHT.isEnabled() || Modules.LONG_JUMP.isEnabled() || SpeedModule.mc.player.isRiding() || SpeedModule.mc.player.isFallFlying() || SpeedModule.mc.player.isHoldingOntoLadder() || (SpeedModule.mc.player.isInLava() || SpeedModule.mc.player.isTouchingWater()) && !this.speedWaterConfig.getValue().booleanValue()) {
                this.resetStrafe();
                Modules.TIMER.setTimer(1.0f);
                return;
            }
            event.cancel();
            double n = SpeedModule.mc.player.input.movementForward;
            double n2 = SpeedModule.mc.player.input.movementSideways;
            if (n == 0.0 && n2 == 0.0) {
                event.setX(0.0);
                event.setZ(0.0);
                return;
            }
            double speedEffect = 1.0;
            double slowEffect = 1.0;
            if (SpeedModule.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                amplifier = SpeedModule.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                speedEffect = 1.0 + 0.2 * (amplifier + 1.0);
            }
            if (SpeedModule.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                amplifier = SpeedModule.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                slowEffect = 1.0 + 0.2 * (amplifier + 1.0);
            }
            double base = (double)0.2873f * speedEffect / slowEffect;
            float jumpEffect = 0.0f;
            if (SpeedModule.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                jumpEffect += (float)(SpeedModule.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
            }
            if (this.speedModeConfig.getValue() == Speed.STRAFE || this.speedModeConfig.getValue() == Speed.STRAFE_B_HOP) {
                if (!Managers.ANTICHEAT.hasPassed(100L)) {
                    return;
                }
                if (this.timerConfig.getValue().booleanValue()) {
                    Modules.TIMER.setTimer(1.0888f);
                }
                if (this.strafe == 1) {
                    this.speed = (double)1.35f * base - (double)0.01f;
                } else if (this.strafe == 2) {
                    if (SpeedModule.mc.player.input.jumping || !SpeedModule.mc.player.isOnGround()) {
                        return;
                    }
                    float jump = (this.speedModeConfig.getValue() == Speed.STRAFE_B_HOP ? 0.4f : 0.39999995f) + jumpEffect;
                    event.setY(jump);
                    MovementUtil.setMotionY(jump);
                    this.speed *= this.speedModeConfig.getValue() == Speed.STRAFE_B_HOP ? 1.535 : (this.accel ? 1.6835 : 1.395);
                } else if (this.strafe == 3) {
                    double moveSpeed = 0.66 * (this.getDistance - base);
                    this.speed = this.getDistance - moveSpeed;
                    this.accel = !this.accel;
                } else {
                    if ((!SpeedModule.mc.world.isSpaceEmpty(SpeedModule.mc.player, SpeedModule.mc.player.getBoundingBox().offset(0.0, SpeedModule.mc.player.getVelocity().getY(), 0.0)) || SpeedModule.mc.player.verticalCollision) && this.strafe > 0) {
                        this.strafe = MovementUtil.isInputtingMovement() ? 1 : 0;
                    }
                    this.speed = this.getDistance - this.getDistance / 159.0;
                }
                this.speed = Math.max(this.speed, base);
                if (this.strafeBoostConfig.getValue().booleanValue()) {
                    this.speed += this.boostSpeed * (double)this.multiply.getValue().floatValue();
                }
                Vec2f motion = this.handleStrafeMotion((float)this.speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                ++this.strafe;
            } else if (this.speedModeConfig.getValue() == Speed.STRAFE_STRICT) {
                if (!Managers.ANTICHEAT.hasPassed(100L)) {
                    return;
                }
                if (this.strafe == 1) {
                    this.speed = (double)1.35f * base - (double)0.01f;
                } else if (this.strafe == 2) {
                    if (SpeedModule.mc.player.input.jumping || !SpeedModule.mc.player.isOnGround()) {
                        return;
                    }
                    float jump = 0.39999995f + jumpEffect;
                    event.setY(jump);
                    MovementUtil.setMotionY(jump);
                    this.speed *= 2.149;
                } else if (this.strafe == 3) {
                    double moveSpeed = 0.66 * (this.getDistance - base);
                    this.speed = this.getDistance - moveSpeed;
                } else {
                    if ((!SpeedModule.mc.world.isSpaceEmpty(SpeedModule.mc.player, SpeedModule.mc.player.getBoundingBox().offset(0.0, SpeedModule.mc.player.getVelocity().getY(), 0.0)) || SpeedModule.mc.player.verticalCollision) && this.strafe > 0) {
                        this.strafe = MovementUtil.isInputtingMovement() ? 1 : 0;
                    }
                    this.speed = this.getDistance - this.getDistance / 159.0;
                }
                ++this.strictTicks;
                this.speed = Math.max(this.speed, base);
                if (this.timerConfig.getValue().booleanValue()) {
                    Modules.TIMER.setTimer(1.0888f);
                }
                double baseMax = 0.465 * speedEffect / slowEffect;
                double baseMin = 0.44 * speedEffect / slowEffect;
                this.speed = Math.min(this.speed, this.strictTicks > 25 ? baseMax : baseMin);
                if (this.strafeBoostConfig.getValue().booleanValue()) {
                    this.speed += this.boostSpeed * (double)this.multiply.getValue().floatValue();
                }
                if (this.strictTicks > 50) {
                    this.strictTicks = 0;
                }
                Vec2f motion = this.handleStrafeMotion((float)this.speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                ++this.strafe;
            } else if (this.speedModeConfig.getValue() == Speed.LOW_HOP) {
                if (!Managers.ANTICHEAT.hasPassed(100L)) {
                    return;
                }
                if (this.timerConfig.getValue().booleanValue()) {
                    Modules.TIMER.setTimer(1.0888f);
                }
                if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.4, 3)) {
                    MovementUtil.setMotionY(0.31 + (double)jumpEffect);
                    event.setY(0.31 + (double)jumpEffect);
                } else if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.71, 3)) {
                    MovementUtil.setMotionY(0.04 + (double)jumpEffect);
                    event.setY(0.04 + (double)jumpEffect);
                } else if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.75, 3)) {
                    MovementUtil.setMotionY(-0.2 - (double)jumpEffect);
                    event.setY(-0.2 - (double)jumpEffect);
                } else if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.55, 3)) {
                    MovementUtil.setMotionY(-0.14 + (double)jumpEffect);
                    event.setY(-0.14 + (double)jumpEffect);
                } else if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.41, 3)) {
                    MovementUtil.setMotionY(-0.2 + (double)jumpEffect);
                    event.setY(-0.2 + (double)jumpEffect);
                }
                if (this.strafe == 1) {
                    this.speed = (double)1.35f * base - (double)0.01f;
                } else if (this.strafe == 2) {
                    double jump = (this.isBoxColliding() ? 0.2 : 0.3999) + (double)jumpEffect;
                    MovementUtil.setMotionY(jump);
                    event.setY(jump);
                    this.speed *= this.accel ? 1.5685 : 1.3445;
                } else if (this.strafe == 3) {
                    double moveSpeed = 0.66 * (this.getDistance - base);
                    this.speed = this.getDistance - moveSpeed;
                    this.accel = !this.accel;
                } else {
                    if (SpeedModule.mc.player.isOnGround() && this.strafe > 0) {
                        this.strafe = MovementUtil.isInputtingMovement() ? 1 : 0;
                    }
                    this.speed = this.getDistance - this.getDistance / 159.0;
                }
                this.speed = Math.max(this.speed, base);
                Vec2f motion = this.handleVanillaMotion((float)this.speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                ++this.strafe;
            } else if (this.speedModeConfig.getValue() == Speed.GAY_HOP) {
                if (!Managers.ANTICHEAT.hasPassed(100L)) {
                    this.strafe = 1;
                    return;
                }
                if (this.strafe == 1 && SpeedModule.mc.player.verticalCollision && MovementUtil.isInputtingMovement()) {
                    this.speed = 1.25 * base - (double)0.01f;
                } else if (this.strafe == 2 && SpeedModule.mc.player.verticalCollision && MovementUtil.isInputtingMovement()) {
                    float jump = (this.isBoxColliding() ? 0.2f : 0.4f) + jumpEffect;
                    event.setY(jump);
                    MovementUtil.setMotionY(jump);
                    this.speed *= 2.149;
                } else if (this.strafe == 3) {
                    double moveSpeed = 0.66 * (this.getDistance - base);
                    this.speed = this.getDistance - moveSpeed;
                } else {
                    if (SpeedModule.mc.player.isOnGround() && this.strafe > 0) {
                        this.strafe = 1.35 * base - 0.01 > this.speed ? 0 : (MovementUtil.isInputtingMovement() ? 1 : 0);
                    }
                    this.speed = this.getDistance - this.getDistance / 159.0;
                }
                this.speed = Math.max(this.speed, base);
                if (this.strafe > 0) {
                    Vec2f motion = this.handleStrafeMotion((float)this.speed);
                    event.setX(motion.x);
                    event.setZ(motion.y);
                }
                ++this.strafe;
            } else if (this.speedModeConfig.getValue() == Speed.V_HOP) {
                if (!Managers.ANTICHEAT.hasPassed(100L)) {
                    this.strafe = 1;
                    return;
                }
                if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.4, 3)) {
                    MovementUtil.setMotionY(0.31 + (double)jumpEffect);
                    event.setY(0.31 + (double)jumpEffect);
                } else if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.71, 3)) {
                    MovementUtil.setMotionY(0.04 + (double)jumpEffect);
                    event.setY(0.04 + (double)jumpEffect);
                } else if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.75, 3)) {
                    MovementUtil.setMotionY(-0.2 - (double)jumpEffect);
                    event.setY(-0.2 - (double)jumpEffect);
                }
                if (!SpeedModule.mc.world.isSpaceEmpty(null, SpeedModule.mc.player.getBoundingBox().offset(0.0, -0.56, 0.0)) && MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.55, 3)) {
                    MovementUtil.setMotionY(-0.14 + (double)jumpEffect);
                    event.setY(-0.14 + (double)jumpEffect);
                }
                if (this.strafe != 1 || !SpeedModule.mc.player.verticalCollision || SpeedModule.mc.player.forwardSpeed == 0.0f && SpeedModule.mc.player.sidewaysSpeed == 0.0f) {
                    if (this.strafe != 2 || !SpeedModule.mc.player.verticalCollision || SpeedModule.mc.player.forwardSpeed == 0.0f && SpeedModule.mc.player.sidewaysSpeed == 0.0f) {
                        if (this.strafe == 3) {
                            double moveSpeed = 0.66 * (this.getDistance - base);
                            this.speed = this.getDistance - moveSpeed;
                        } else {
                            if (SpeedModule.mc.player.isOnGround() && this.strafe > 0) {
                                this.strafe = 1.35 * base - 0.01 > this.speed ? 0 : (MovementUtil.isInputtingMovement() ? 1 : 0);
                            }
                            this.speed = this.getDistance - this.getDistance / 159.0;
                        }
                    } else {
                        double jump = (this.isBoxColliding() ? 0.2 : 0.4) + (double)jumpEffect;
                        MovementUtil.setMotionY(jump);
                        event.setY(jump);
                        this.speed *= 2.149;
                    }
                } else {
                    this.speed = 2.0 * base - 0.01;
                }
                if (this.strafe > 8) {
                    this.speed = base;
                }
                this.speed = Math.max(this.speed, base);
                Vec2f motion = this.handleStrafeMotion((float)this.speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                ++this.strafe;
            } else if (this.speedModeConfig.getValue() == Speed.B_HOP) {
                if (!Managers.ANTICHEAT.hasPassed(100L)) {
                    this.strafe = 4;
                    return;
                }
                if (MathUtil.round(SpeedModule.mc.player.getY() - (double)((int)SpeedModule.mc.player.getY()), 3) == MathUtil.round(0.138, 3)) {
                    MovementUtil.setMotionY(SpeedModule.mc.player.getVelocity().y - (0.08 + (double)jumpEffect));
                    event.setY(event.getY() - (0.0931 + (double)jumpEffect));
                    Managers.POSITION.setPositionY(SpeedModule.mc.player.getY() - (0.0931 + (double)jumpEffect));
                }
                if (this.strafe != 2 || SpeedModule.mc.player.forwardSpeed == 0.0f && SpeedModule.mc.player.sidewaysSpeed == 0.0f) {
                    if (this.strafe == 3) {
                        double moveSpeed = 0.66 * (this.getDistance - base);
                        this.speed = this.getDistance - moveSpeed;
                    } else {
                        if (SpeedModule.mc.player.isOnGround()) {
                            this.strafe = 1;
                        }
                        this.speed = this.getDistance - this.getDistance / 159.0;
                    }
                } else {
                    double jump = (this.isBoxColliding() ? 0.2 : 0.4) + (double)jumpEffect;
                    MovementUtil.setMotionY(jump);
                    event.setY(jump);
                    this.speed *= 2.149;
                }
                this.speed = Math.max(this.speed, base);
                Vec2f motion = this.handleStrafeMotion((float)this.speed);
                event.setX(motion.x);
                event.setZ(motion.y);
                ++this.strafe;
            } else if (this.speedModeConfig.getValue() == Speed.VANILLA) {
                Vec2f motion = this.handleVanillaMotion(this.vanillaStrafeConfig.getValue() ? (float)base : this.speedConfig.getValue().floatValue() / 10.0f);
                event.setX(motion.x);
                event.setZ(motion.y);
            }
        }
    }

    public Vec2f handleStrafeMotion(float speed) {
        float forward = SpeedModule.mc.player.input.movementForward;
        float strafe = SpeedModule.mc.player.input.movementSideways;
        float yaw = SpeedModule.mc.player.prevYaw + (SpeedModule.mc.player.getYaw() - SpeedModule.mc.player.prevYaw) * mc.getTickDelta();
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

    public Vec2f handleVanillaMotion(float speed) {
        float forward = SpeedModule.mc.player.input.movementForward;
        float strafe = SpeedModule.mc.player.input.movementSideways;
        if (forward == 0.0f && strafe == 0.0f) {
            return Vec2f.ZERO;
        }
        if (forward != 0.0f && strafe != 0.0f) {
            forward *= (float)Math.sin(0.7853981633974483);
            strafe *= (float)Math.cos(0.7853981633974483);
        }
        return new Vec2f((float)((double)(forward * speed) * -Math.sin(Math.toRadians(SpeedModule.mc.player.getYaw())) + (double)(strafe * speed) * Math.cos(Math.toRadians(SpeedModule.mc.player.getYaw()))), (float)((double)(forward * speed) * Math.cos(Math.toRadians(SpeedModule.mc.player.getYaw())) - (double)(strafe * speed) * -Math.sin(Math.toRadians(SpeedModule.mc.player.getYaw()))));
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (SpeedModule.mc.player == null || SpeedModule.mc.world == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ExplosionS2CPacket packet2) {
            double x = packet2.getPlayerVelocityX();
            double z = packet2.getPlayerVelocityZ();
            this.boostSpeed = Math.sqrt(x * x + z * z) / 100000.0;
            this.boostTicks = 0;
        } else {
            EntityVelocityUpdateS2CPacket packet3;
            Packet<?> x = event.getPacket();
            if (x instanceof EntityVelocityUpdateS2CPacket && (packet3 = (EntityVelocityUpdateS2CPacket) x).getId() == SpeedModule.mc.player.getId()) {
                double x2 = packet3.getVelocityX();
                double z = packet3.getVelocityZ();
                this.boostSpeed = Math.sqrt(x2 * x2 + z * z) / 100000.0;
                this.boostTicks = 0;
            } else if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
                this.resetStrafe();
            }
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event) {
        if (event.getConfig() == this.timerConfig && event.getStage() == EventStage.POST && this.isStrafe()) {
            if (this.timerConfig.getValue().booleanValue()) {
                this.prevTimer = Modules.TIMER.isEnabled();
                if (!this.prevTimer) {
                    Modules.TIMER.enable();
                }
            } else if (Modules.TIMER.isEnabled()) {
                Modules.TIMER.resetTimer();
                if (!this.prevTimer) {
                    Modules.TIMER.disable();
                }
            }
        }
    }

    public boolean isBoxColliding() {
        return !SpeedModule.mc.world.isSpaceEmpty(SpeedModule.mc.player, SpeedModule.mc.player.getBoundingBox().offset(0.0, 0.21, 0.0));
    }

    public boolean checkIsCollidingEntity(Entity entity) {
        return entity != null && entity != SpeedModule.mc.player && entity instanceof LivingEntity && !(entity instanceof FakePlayerEntity) && !(entity instanceof ArmorStandEntity);
    }

    public void setPrevTimer() {
        this.prevTimer = !this.prevTimer;
    }

    public boolean isUsingTimer() {
        return this.isEnabled() && this.timerConfig.getValue();
    }

    public void resetStrafe() {
        this.strafe = 4;
        this.strictTicks = 0;
        this.speed = 0.0;
        this.getDistance = 0.0;
        this.accel = false;
    }

    public boolean isStrafe() {
        return this.speedModeConfig.getValue() != Speed.FIREWORK && this.speedModeConfig.getValue() != Speed.GRIM_COLLIDE && this.speedModeConfig.getValue() != Speed.VANILLA;
    }

    private enum Speed {
        STRAFE,
        STRAFE_STRICT,
        STRAFE_B_HOP,
        LOW_HOP,
        GAY_HOP,
        V_HOP,
        B_HOP,
        VANILLA,
        GRIM_COLLIDE,
        FIREWORK

    }
}
