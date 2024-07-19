package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.MacroConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.macro.Macro;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.MouseUpdateEvent;
import dev.realme.ash.impl.event.PerspectiveEvent;
import dev.realme.ash.impl.event.ScreenOpenEvent;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.camera.CameraPositionEvent;
import dev.realme.ash.impl.event.camera.CameraRotationEvent;
import dev.realme.ash.impl.event.camera.EntityCameraPositionEvent;
import dev.realme.ash.impl.event.entity.EntityRotationVectorEvent;
import dev.realme.ash.impl.event.keyboard.KeyboardInputEvent;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.impl.event.render.BobViewEvent;
import dev.realme.ash.util.player.RayCastUtil;
import dev.realme.ash.util.player.RotationUtil;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class FreecamModule
extends RotationModule {
    final Config<Float> speedConfig = new NumberConfig<>("Speed", "The move speed of the camera", 0.1f, 4.0f, 10.0f);
    final Config<Macro> controlConfig = new MacroConfig("ControlKey", "", new Macro(this.getId() + "-control", 342, () -> {}));
    final Config<Boolean> toggleControlConfig = new BooleanConfig("ToggleControl", "Allows toggling control key instead of holding", false);
    final Config<Interact> interactConfig = new EnumConfig<>("Interact", "The interaction type of the camera", Interact.CAMERA, Interact.values());
    final Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotate to the point of interaction", false);
    public Vec3d position;
    public Vec3d lastPosition;
    public float yaw;
    public float pitch;
    private boolean control = false;

    public FreecamModule() {
        super("Freecam", "Allows you to control the camera separately from the player", ModuleCategory.RENDER);
    }

    @Override
    protected void onEnable() {
        if (FreecamModule.mc.player == null) {
            return;
        }
        this.control = false;
        this.lastPosition = this.position = FreecamModule.mc.gameRenderer.getCamera().getPos();
        this.yaw = FreecamModule.mc.player.getYaw();
        this.pitch = FreecamModule.mc.player.getPitch();
        FreecamModule.mc.player.input = new FreecamKeyboardInput(FreecamModule.mc.options);
    }

    @Override
    protected void onDisable() {
        if (FreecamModule.mc.player == null) {
            return;
        }
        FreecamModule.mc.player.input = new KeyboardInput(FreecamModule.mc.options);
    }

    @EventListener
    public void onKey(KeyboardInputEvent event) {
        if (event.getAction() != 2 && event.getKeycode() == this.controlConfig.getValue().getKeycode()) {
            if (!this.toggleControlConfig.getValue()) {
                this.control = event.getAction() == 1;
            } else if (event.getAction() == 1) {
                this.control = !this.control;
            }
        }
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        this.disable();
    }

    @EventListener
    public void onCameraPosition(CameraPositionEvent event) {
        event.setPosition(this.control ? this.position : this.lastPosition.lerp(this.position, event.getTickDelta()));
    }

    @EventListener
    public void onCameraRotation(CameraRotationEvent event) {
        event.setRotation(new Vec2f(this.yaw, this.pitch));
    }

    @EventListener
    public void onMouseUpdate(MouseUpdateEvent event) {
        if (!this.control) {
            event.cancel();
            this.changeLookDirection(event.getCursorDeltaX(), event.getCursorDeltaY());
        }
    }

    @EventListener
    public void onEntityCameraPosition(EntityCameraPositionEvent event) {
        if (event.getEntity() != FreecamModule.mc.player) {
            return;
        }
        if (!this.control && this.interactConfig.getValue() == Interact.CAMERA) {
            event.setPosition(this.position);
        }
    }

    @EventListener
    public void onEntityRotation(EntityRotationVectorEvent event) {
        if (event.getEntity() != FreecamModule.mc.player) {
            return;
        }
        if (!this.control && this.interactConfig.getValue() == Interact.CAMERA) {
            event.setPosition(RotationUtil.getRotationVector(this.pitch, this.yaw));
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (!this.control && this.rotateConfig.getValue()) {
            float[] currentAngles = new float[]{this.yaw, this.pitch};
            Vec3d eyePos = this.position;
            HitResult result = RayCastUtil.rayCast(FreecamModule.mc.interactionManager.getReachDistance(), eyePos, currentAngles);
            if (result.getType() == HitResult.Type.BLOCK) {
                float[] newAngles = RotationUtil.getRotationsTo(FreecamModule.mc.player.getEyePos(), result.getPos());
                this.setRotation(newAngles[0], newAngles[1]);
            }
        }
    }

    @EventListener
    public void onScreenOpen(ScreenOpenEvent event) {
        if (event.getScreen() instanceof DeathScreen) {
            this.disable();
        }
    }

    @EventListener
    public void onPerspective(PerspectiveEvent event) {
        event.cancel();
    }

    @EventListener
    public void onBob(BobViewEvent event) {
        if (this.control) {
            event.cancel();
        }
    }

    private float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0f;
        }
        return positive ? 1.0f : -1.0f;
    }

    private Vec2f handleVanillaMotion(float speed, float forward, float strafe) {
        if (forward == 0.0f && strafe == 0.0f) {
            return Vec2f.ZERO;
        }
        if (forward != 0.0f && strafe != 0.0f) {
            forward *= (float)Math.sin(0.7853981633974483);
            strafe *= (float)Math.cos(0.7853981633974483);
        }
        return new Vec2f((float)((double)(forward * speed) * -Math.sin(Math.toRadians(this.yaw)) + (double)(strafe * speed) * Math.cos(Math.toRadians(this.yaw))), (float)((double)(forward * speed) * Math.cos(Math.toRadians(this.yaw)) - (double)(strafe * speed) * -Math.sin(Math.toRadians(this.yaw))));
    }

    private void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
        float f = (float)cursorDeltaY * 0.15f;
        float g = (float)cursorDeltaX * 0.15f;
        this.pitch += f;
        this.yaw += g;
        this.pitch = MathHelper.clamp(this.pitch, -90.0f, 90.0f);
    }

    public Vec3d getCameraPosition() {
        return this.position;
    }

    public float[] getCameraRotations() {
        return new float[]{this.yaw, this.pitch};
    }

    public enum Interact {
        PLAYER,
        CAMERA

    }

    public class FreecamKeyboardInput
    extends KeyboardInput {
        private final GameOptions options;

        public FreecamKeyboardInput(GameOptions options) {
            super(options);
            this.options = options;
        }

        public void tick(boolean slowDown, float slowDownFactor) {
            if (FreecamModule.this.control) {
                super.tick(slowDown, slowDownFactor);
            } else {
                this.unset();
                float speed = FreecamModule.this.speedConfig.getValue() / 10.0f;
                float fakeMovementForward = FreecamModule.this.getMovementMultiplier(this.options.forwardKey.isPressed(), this.options.backKey.isPressed());
                float fakeMovementSideways = FreecamModule.this.getMovementMultiplier(this.options.leftKey.isPressed(), this.options.rightKey.isPressed());
                Vec2f dir = FreecamModule.this.handleVanillaMotion(speed, fakeMovementForward, fakeMovementSideways);
                float y = 0.0f;
                if (this.options.jumpKey.isPressed()) {
                    y += speed;
                } else if (this.options.sneakKey.isPressed()) {
                    y -= speed;
                }
                FreecamModule.this.lastPosition = FreecamModule.this.position;
                FreecamModule.this.position = FreecamModule.this.position.add(dir.x, y, dir.y);
            }
        }

        private void unset() {
            this.pressingForward = false;
            this.pressingBack = false;
            this.pressingLeft = false;
            this.pressingRight = false;
            this.movementForward = 0.0f;
            this.movementSideways = 0.0f;
            this.jumping = false;
            this.sneaking = false;
        }
    }
}
