package dev.realme.ash.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.render.entity.RenderCrystalEvent;
import dev.realme.ash.impl.event.render.entity.RenderEntityEvent;
import dev.realme.ash.impl.event.render.item.RenderArmEvent;
import dev.realme.ash.util.world.EntityUtil;
import java.awt.Color;
import java.lang.invoke.LambdaMetafactory;
import java.util.function.Supplier;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

public class ChamsModule
extends ToggleModule {
    Config<ChamsMode> modeConfig = new EnumConfig("Mode", "The rendering mode for the chams", ChamsMode.NORMAL, ChamsMode.values());
    Config<Boolean> handsConfig = new BooleanConfig("Hands", "Render chams on first-person hands", true);
    Config<Boolean> selfConfig = new BooleanConfig("Self", "Render chams on the player", true);
    Config<Boolean> playersConfig = new BooleanConfig("Players", "Render chams on other players", true);
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Render chams on monsters", true);
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Render chams on animals", true);
    Config<Boolean> otherConfig = new BooleanConfig("Others", "Render chams on crystals", true);
    Config<Boolean> invisiblesConfig = new BooleanConfig("Invisibles", "Render chams on invisible entities", true);
    Config<Color> colorConfig = new ColorConfig("Color", "The color of the chams", new Color(255, 0, 0, 60));
    private static final float SINE_45_DEGREES = (float)Math.sin(0.7853981633974483);

    public ChamsModule() {
        super("Chams", "Renders entity models through walls", ModuleCategory.RENDER);
    }

    private static float getYaw(Direction direction) {
        switch (direction) {
            case SOUTH: {
                return 90.0f;
            }
            case WEST: {
                return 0.0f;
            }
            case NORTH: {
                return 270.0f;
            }
            case EAST: {
                return 180.0f;
            }
        }
        return 0.0f;
    }

    @EventListener
    public void onRenderEntity(RenderEntityEvent event) {
        float n;
        Direction direction;
        float l;
        if (!this.checkChams(event.entity)) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.lineWidth(2.0f);
        vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        Color color = this.colorConfig.getValue();
        event.matrixStack.push();
        RenderSystem.setShaderColor((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        event.model.handSwingProgress = event.entity.getHandSwingProgress(event.g);
        event.model.riding = event.entity.hasVehicle();
        event.model.child = event.entity.isBaby();
        float h = MathHelper.lerpAngleDegrees(event.g, event.entity.prevBodyYaw, event.entity.bodyYaw);
        float j = MathHelper.lerpAngleDegrees(event.g, event.entity.prevHeadYaw, event.entity.headYaw);
        float k = j - h;
        if (event.entity.hasVehicle() && event.entity.getVehicle() instanceof LivingEntity livingEntity2) {
            h = MathHelper.lerpAngleDegrees(event.g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
            k = j - h;
            l = MathHelper.wrapDegrees(k);
            if (l < -85.0f) {
                l = -85.0f;
            }
            if (l >= 85.0f) {
                l = 85.0f;
            }
            h = j - l;
            if (l * l > 2500.0f) {
                h += l * 0.2f;
            }
            k = j - h;
        }
        float m = MathHelper.lerp(event.g, event.entity.prevPitch, event.entity.getPitch());
        if (LivingEntityRenderer.shouldFlipUpsideDown(event.entity)) {
            m *= -1.0f;
            k *= -1.0f;
        }
        if (event.entity.isInPose(EntityPose.SLEEPING) && (direction = event.entity.getSleepingDirection()) != null) {
            n = event.entity.getEyeHeight(EntityPose.STANDING) - 0.1f;
            event.matrixStack.translate((float)(-direction.getOffsetX()) * n, 0.0f, (float)(-direction.getOffsetZ()) * n);
        }
        l = (float)event.entity.age + event.g;
        this.setupTransforms(event.entity, event.matrixStack, h, event.g);
        event.matrixStack.scale(-1.0f, -1.0f, 1.0f);
        event.matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        event.matrixStack.translate(0.0f, -1.501f, 0.0f);
        n = 0.0f;
        float o = 0.0f;
        if (!event.entity.hasVehicle() && event.entity.isAlive()) {
            n = event.entity.limbAnimator.getSpeed(event.g);
            o = event.entity.limbAnimator.getPos(event.g);
            if (event.entity.isBaby()) {
                o *= 3.0f;
            }
            if (n > 1.0f) {
                n = 1.0f;
            }
        }
        event.model.animateModel(event.entity, o, n, event.g);
        event.model.setAngles(event.entity, o, n, l, k, m);
        boolean bl = !event.entity.isInvisible();
        boolean bl2 = !bl && !event.entity.isInvisibleTo(ChamsModule.mc.player);
        int p = LivingEntityRenderer.getOverlay(event.entity, 0.0f);
        event.model.render(event.matrixStack, vertexConsumer, event.i, p, 1.0f, 1.0f, 1.0f, 1.0f);
        tessellator.draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        if (!event.entity.isSpectator()) {
            for (FeatureRenderer featureRenderer : event.features) {
                featureRenderer.render(event.matrixStack, event.vertexConsumerProvider,
                        event.i, event.entity, o, n, event.g, l, k, m);
            }
        }
        event.matrixStack.pop();
        event.cancel();
    }

    protected void setupTransforms(LivingEntity entity, MatrixStack matrices, float bodyYaw, float tickDelta) {
        if (entity.isFrozen()) {
            bodyYaw += (float)(Math.cos((double)entity.age * 3.25) * Math.PI * (double)0.4f);
        }
        if (!entity.isInPose(EntityPose.SLEEPING)) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
        }
        if (entity.deathTime > 0) {
            float f = 0f;
            float f2 = ((float)entity.deathTime + tickDelta - 1.0f) / 20.0f * 1.6f;
            f2 = MathHelper.sqrt(f2);
            if (f > 1.0f) {
                f2 = 1.0f;
            }
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f2 * 90.0f));
        } else if (entity.isUsingRiptide()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f - entity.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float)entity.age + tickDelta) * -75.0f));
        } else if (entity.isInPose(EntityPose.SLEEPING)) {
            Direction direction = entity.getSleepingDirection();
            float g = direction != null ? ChamsModule.getYaw(direction) : bodyYaw;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0f));
        } else if (LivingEntityRenderer.shouldFlipUpsideDown(entity)) {
            matrices.translate(0.0f, entity.getHeight() + 0.1f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        }
    }

    @EventListener
    public void onRenderCrystal(RenderCrystalEvent event) {
        if (!this.otherConfig.getValue().booleanValue()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        event.matrixStack.push();
        float h = EndCrystalEntityRenderer.getYOffset(event.endCrystalEntity, event.g);
        float j = ((float)event.endCrystalEntity.endCrystalAge + event.g) * 3.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.lineWidth(2.0f);
        vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        event.matrixStack.push();
        Color color = this.colorConfig.getValue();
        RenderSystem.setShaderColor((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        event.matrixStack.scale(2.0f, 2.0f, 2.0f);
        event.matrixStack.translate(0.0f, -0.5f, 0.0f);
        int k = OverlayTexture.DEFAULT_UV;
        event.matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        event.matrixStack.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
        event.matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        event.frame.render(event.matrixStack, vertexConsumer, event.i, k);
        float l = 0.875f;
        event.matrixStack.scale(0.875f, 0.875f, 0.875f);
        event.matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        event.matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        event.frame.render(event.matrixStack, vertexConsumer, event.i, k);
        event.matrixStack.scale(0.875f, 0.875f, 0.875f);
        event.matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        event.matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        event.core.render(event.matrixStack, vertexConsumer, event.i, k);
        event.matrixStack.pop();
        event.matrixStack.pop();
        tessellator.draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        event.cancel();
    }

    @EventListener
    public void onRenderArm(RenderArmEvent event) {
        if (this.handsConfig.getValue().booleanValue()) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexConsumer = tessellator.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            RenderSystem.lineWidth(2.0f);
            vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            event.matrices.push();
            Color color = this.colorConfig.getValue();
            RenderSystem.setShaderColor((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, MathHelper.clamp(((float)color.getAlpha() + 40.0f) / 255.0f, 0.0f, 1.0f));
            boolean bl = event.arm != Arm.LEFT;
            float f = bl ? 1.0f : -1.0f;
            float g = MathHelper.sqrt(event.swingProgress);
            float h = -0.3f * MathHelper.sin(g * (float)Math.PI);
            float i = 0.4f * MathHelper.sin(g * ((float)Math.PI * 2));
            float j = -0.4f * MathHelper.sin(event.swingProgress * (float)Math.PI);
            event.matrices.translate(f * (h + 0.64000005f), i + -0.6f + event.equipProgress * -0.6f, j + -0.71999997f);
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 45.0f));
            float k = MathHelper.sin(event.swingProgress * event.swingProgress * (float)Math.PI);
            float l = MathHelper.sin(g * (float)Math.PI);
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * l * 70.0f));
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * k * -20.0f));
            event.matrices.translate(f * -1.0f, 3.6f, 3.5f);
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 120.0f));
            event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0f));
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -135.0f));
            event.matrices.translate(f * 5.6f, 0.0f, 0.0f);
            event.playerEntityRenderer.setModelPose(ChamsModule.mc.player);
            event.playerEntityRenderer.getModel().handSwingProgress = 0.0f;
            event.playerEntityRenderer.getModel().sneaking = false;
            event.playerEntityRenderer.getModel().leaningPitch = 0.0f;
            event.playerEntityRenderer.getModel().setAngles(ChamsModule.mc.player, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
            if (event.arm == Arm.RIGHT) {
                event.playerEntityRenderer.getModel().rightArm.pitch = 0.0f;
                event.playerEntityRenderer.getModel().rightArm.render(event.matrices, vertexConsumer, event.light, OverlayTexture.DEFAULT_UV);
                event.playerEntityRenderer.getModel().rightSleeve.pitch = 0.0f;
                event.playerEntityRenderer.getModel().rightSleeve.render(event.matrices, vertexConsumer, event.light, OverlayTexture.DEFAULT_UV);
            } else {
                event.playerEntityRenderer.getModel().leftArm.pitch = 0.0f;
                event.playerEntityRenderer.getModel().leftArm.render(event.matrices, vertexConsumer, event.light, OverlayTexture.DEFAULT_UV);
                event.playerEntityRenderer.getModel().leftSleeve.pitch = 0.0f;
                event.playerEntityRenderer.getModel().leftSleeve.render(event.matrices, vertexConsumer, event.light, OverlayTexture.DEFAULT_UV);
            }
            tessellator.draw();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            event.matrices.pop();
            event.cancel();
        }
    }

    private boolean checkChams(LivingEntity entity) {
        if (entity instanceof PlayerEntity && this.playersConfig.getValue().booleanValue()) {
            return this.selfConfig.getValue() || entity != ChamsModule.mc.player;
        }
        return (!entity.isInvisible() || this.invisiblesConfig.getValue()) && (EntityUtil.isMonster(entity) && this.monstersConfig.getValue() || (EntityUtil.isNeutral(entity) || EntityUtil.isPassive(entity)) && this.animalsConfig.getValue());
    }

    public enum ChamsMode {
        NORMAL

    }
}
