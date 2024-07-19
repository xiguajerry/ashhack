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
import dev.realme.ash.impl.event.entity.player.PlayerMoveEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.string.EnumFormatter;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec2f;

public class LongJumpModule
extends ToggleModule {
    final Config<JumpMode> modeConfig = new EnumConfig<>("Mode", "The mode for long jump", JumpMode.NORMAL, JumpMode.values());
    final Config<Float> boostConfig = new NumberConfig<>("Boost", "The jump boost speed", 0.1f, 4.5f, 10.0f, () -> this.modeConfig.getValue() == JumpMode.NORMAL);
    final Config<Boolean> autoDisableConfig = new BooleanConfig("AutoDisable", "Automatically disables when rubberband is detected", true);
    private int stage;
    private double getDistance;
    private double speed;
    private int airTicks;
    private int groundTicks;

    public LongJumpModule() {
        super("LongJump", "Allows the player to jump farther", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(this.modeConfig.getValue());
    }

    @Override
    public void onEnable() {
        this.groundTicks = 0;
    }

    @Override
    public void onDisable() {
        this.stage = 0;
        this.getDistance = 0.0;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        double dx = LongJumpModule.mc.player.getX() - LongJumpModule.mc.player.prevX;
        double dz = LongJumpModule.mc.player.getZ() - LongJumpModule.mc.player.prevZ;
        this.getDistance = Math.sqrt(dx * dx + dz * dz);
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.modeConfig.getValue() == JumpMode.NORMAL) {
            double amplifier;
            if (LongJumpModule.mc.player == null || LongJumpModule.mc.world == null || Modules.FLIGHT.isEnabled() || Modules.PACKET_FLY.isEnabled() || !MovementUtil.isInputtingMovement()) {
                return;
            }
            double speedEffect = 1.0;
            double slowEffect = 1.0;
            if (LongJumpModule.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                amplifier = LongJumpModule.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                speedEffect = 1.0 + 0.2 * (amplifier + 1.0);
            }
            if (LongJumpModule.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                amplifier = LongJumpModule.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                slowEffect = 1.0 + 0.2 * (amplifier + 1.0);
            }
            double base = (double)0.2873f * speedEffect / slowEffect;
            if (this.stage == 0) {
                this.stage = 1;
                this.speed = (double) this.boostConfig.getValue() * base - 0.01;
            } else if (this.stage == 1) {
                this.stage = 2;
                MovementUtil.setMotionY(0.42);
                event.setY(0.42);
                this.speed *= 2.149;
            } else if (this.stage == 2) {
                this.stage = 3;
                double moveSpeed = 0.66 * (this.getDistance - base);
                this.speed = this.getDistance - moveSpeed;
            } else {
                if (!LongJumpModule.mc.world.isSpaceEmpty(LongJumpModule.mc.player, LongJumpModule.mc.player.getBoundingBox().offset(0.0, LongJumpModule.mc.player.getVelocity().getY(), 0.0)) || LongJumpModule.mc.player.verticalCollision) {
                    this.stage = 0;
                }
                this.speed = this.getDistance - this.getDistance / 159.0;
            }
            this.speed = Math.max(this.speed, base);
            event.cancel();
            Vec2f motion = this.handleStrafeMotion((float)this.speed);
            event.setX(motion.x);
            event.setZ(motion.y);
        }
    }

    public Vec2f handleStrafeMotion(float speed) {
        float forward = LongJumpModule.mc.player.input.movementForward;
        float strafe = LongJumpModule.mc.player.input.movementSideways;
        float yaw = LongJumpModule.mc.player.prevYaw + (LongJumpModule.mc.player.getYaw() - LongJumpModule.mc.player.prevYaw) * mc.getTickDelta();
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

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (event.getStage() == EventStage.PRE && this.modeConfig.getValue() == JumpMode.GLIDE) {
            if (Modules.FLIGHT.isEnabled() || LongJumpModule.mc.player.isFallFlying() || LongJumpModule.mc.player.isHoldingOntoLadder() || LongJumpModule.mc.player.isTouchingWater()) {
                return;
            }
            if (LongJumpModule.mc.player.isOnGround()) {
                this.getDistance = 0.0;
            }
            float direction = LongJumpModule.mc.player.getYaw() + (float)(LongJumpModule.mc.player.forwardSpeed < 0.0f ? 180 : 0) + (LongJumpModule.mc.player.sidewaysSpeed > 0.0f ? -90.0f * (LongJumpModule.mc.player.forwardSpeed < 0.0f ? -0.5f : (LongJumpModule.mc.player.forwardSpeed > 0.0f ? 0.5f : 1.0f)) : 0.0f) - (LongJumpModule.mc.player.sidewaysSpeed < 0.0f ? -90.0f * (LongJumpModule.mc.player.forwardSpeed < 0.0f ? -0.5f : (LongJumpModule.mc.player.forwardSpeed > 0.0f ? 0.5f : 1.0f)) : 0.0f);
            float dx = (float)Math.cos((double)(direction + 90.0f) * Math.PI / 180.0);
            float dz = (float)Math.sin((double)(direction + 90.0f) * Math.PI / 180.0);
            if (!LongJumpModule.mc.player.verticalCollision) {
                ++this.airTicks;
                if (LongJumpModule.mc.player.input.sneaking) {
                    LongJumpModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0.0, 2.147483647E9, 0.0, false));
                }
                this.groundTicks = 0;
                if (!LongJumpModule.mc.player.verticalCollision) {
                    if (LongJumpModule.mc.player.getVelocity().y == -0.07190068807140403) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.35f);
                    }
                    if (LongJumpModule.mc.player.getVelocity().y == -0.10306193759436909) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.55f);
                    }
                    if (LongJumpModule.mc.player.getVelocity().y == -0.13395038817442878) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.67f);
                    }
                    if (LongJumpModule.mc.player.getVelocity().y == -0.16635183030382) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.69f);
                    }
                    if (LongJumpModule.mc.player.getVelocity().y == -0.19088711097794803) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.71f);
                    }
                    if (LongJumpModule.mc.player.getVelocity().y == -0.21121925191528862) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.2f);
                    }
                    if (LongJumpModule.mc.player.getVelocity().y == -0.11979897632390576) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.93f);
                    }
                    if (LongJumpModule.mc.player.getVelocity().y == -0.18758479151225355) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.72f);
                    }
                    if (LongJumpModule.mc.player.getVelocity().y == -0.21075983825251726) {
                        MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.76f);
                    }
                    if (this.getJumpCollisions(LongJumpModule.mc.player, 70.0) < 0.5) {
                        if (LongJumpModule.mc.player.getVelocity().y == -0.23537393014173347) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.03f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.08531999505205401) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * -0.5);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.03659320313669756) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)-0.1f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.07481386749524899) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)-0.07f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.0732677700939672) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)-0.05f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.07480988066790395) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)-0.04f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.0784000015258789) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.1f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.08608320193943977) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.1f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.08683615560584318) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.05f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.08265497329678266) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.05f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.08245009535659828) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * (double)0.05f);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.08244005633718426) {
                            MovementUtil.setMotionY(-0.08243956442521608);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y == -0.08243956442521608) {
                            MovementUtil.setMotionY(-0.08244005590677261);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y > -0.1 && LongJumpModule.mc.player.getVelocity().y < -0.08 && !LongJumpModule.mc.player.isOnGround() && LongJumpModule.mc.player.input.pressingForward) {
                            MovementUtil.setMotionY(-1.0E-4f);
                        }
                    } else {
                        if (LongJumpModule.mc.player.getVelocity().y < -0.2 && LongJumpModule.mc.player.getVelocity().y > -0.24) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * 0.7);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y < -0.25 && LongJumpModule.mc.player.getVelocity().y > -0.32) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * 0.8);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y < -0.35 && LongJumpModule.mc.player.getVelocity().y > -0.8) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * 0.98);
                        }
                        if (LongJumpModule.mc.player.getVelocity().y < -0.8 && LongJumpModule.mc.player.getVelocity().y > -1.6) {
                            MovementUtil.setMotionY(LongJumpModule.mc.player.getVelocity().y * 0.99);
                        }
                    }
                }
                Managers.TICK.setClientTick(0.85f);
                double[] jumpFactor = new double[]{0.420606, 0.417924, 0.415258, 0.412609, 0.409977, 0.407361, 0.404761, 0.402178, 0.399611, 0.39706, 0.394525, 0.392, 0.3894, 0.38644, 0.383655, 0.381105, 0.37867, 0.37625, 0.37384, 0.37145, 0.369, 0.3666, 0.3642, 0.3618, 0.35945, 0.357, 0.354, 0.351, 0.348, 0.345, 0.342, 0.339, 0.336, 0.333, 0.33, 0.327, 0.324, 0.321, 0.318, 0.315, 0.312, 0.309, 0.307, 0.305, 0.303, 0.3, 0.297, 0.295, 0.293, 0.291, 0.289, 0.287, 0.285, 0.283, 0.281, 0.279, 0.277, 0.275, 0.273, 0.271, 0.269, 0.267, 0.265, 0.263, 0.261, 0.259, 0.257, 0.255, 0.253, 0.251, 0.249, 0.247, 0.245, 0.243, 0.241, 0.239, 0.237};
                if (LongJumpModule.mc.player.input.pressingForward) {
                    try {
                        MovementUtil.setMotionXZ((double)dx * jumpFactor[this.airTicks - 1] * 3.0, (double)dz * jumpFactor[this.airTicks - 1] * 3.0);
                    }
                    catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                        // empty catch block
                    }
                    return;
                }
                MovementUtil.setMotionXZ(0.0, 0.0);
                return;
            }
            Managers.TICK.setClientTick(1.0f);
            this.airTicks = 0;
            ++this.groundTicks;
            MovementUtil.setMotionXZ(LongJumpModule.mc.player.getVelocity().x / 13.0, LongJumpModule.mc.player.getVelocity().z / 13.0);
            if (this.groundTicks == 1) {
                LongJumpModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(LongJumpModule.mc.player.getX(), LongJumpModule.mc.player.getY(), LongJumpModule.mc.player.getZ(), LongJumpModule.mc.player.isOnGround()));
                LongJumpModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(LongJumpModule.mc.player.getX() + 0.0624, LongJumpModule.mc.player.getY(), LongJumpModule.mc.player.getZ(), LongJumpModule.mc.player.isOnGround()));
                LongJumpModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(LongJumpModule.mc.player.getX(), LongJumpModule.mc.player.getY() + 0.419, LongJumpModule.mc.player.getZ(), LongJumpModule.mc.player.isOnGround()));
                LongJumpModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(LongJumpModule.mc.player.getX() + 0.0624, LongJumpModule.mc.player.getY(), LongJumpModule.mc.player.getZ(), LongJumpModule.mc.player.isOnGround()));
                LongJumpModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(LongJumpModule.mc.player.getX(), LongJumpModule.mc.player.getY() + 0.419, LongJumpModule.mc.player.getZ(), LongJumpModule.mc.player.isOnGround()));
            }
            if (this.groundTicks > 2) {
                this.groundTicks = 0;
                MovementUtil.setMotionXZ((double)dx * 0.3, (double)dz * 0.3);
                MovementUtil.setMotionY(0.424f);
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (LongJumpModule.mc.player == null || LongJumpModule.mc.world == null || LongJumpModule.mc.currentScreen instanceof DownloadingTerrainScreen) {
            return;
        }
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && this.autoDisableConfig.getValue()) {
            this.disable();
        }
    }

    private double getJumpCollisions(PlayerEntity player, double d) {
        return 1.0;
    }

    public enum JumpMode {
        NORMAL,
        GLIDE

    }
}
