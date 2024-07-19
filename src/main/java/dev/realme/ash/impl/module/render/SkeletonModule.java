package dev.realme.ash.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.Interpolation;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.init.Modules;
import java.awt.Color;
import java.lang.invoke.LambdaMetafactory;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SkeletonModule
extends ToggleModule {
    public SkeletonModule() {
        super("Skeleton", "Renders a skeleton to show player limbs", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Game event) {
        MatrixStack matrixStack = event.getMatrices();
        float g = event.getTickDelta();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFancyGraphicsOrBetter());
        RenderSystem.enableCull();
        for (Entity entity : SkeletonModule.mc.world.getEntities()) {
            if (entity == null || !entity.isAlive() || !(entity instanceof PlayerEntity playerEntity)) continue;
            if (SkeletonModule.mc.options.getPerspective().isFirstPerson() && playerEntity == SkeletonModule.mc.player) continue;
            Vec3d skeletonPos = Interpolation.getInterpolatedPosition(entity, g);
            PlayerEntityRenderer livingEntityRenderer = (PlayerEntityRenderer)((Object) mc.getEntityRenderDispatcher().getRenderer(playerEntity));
            PlayerEntityModel playerEntityModel = livingEntityRenderer.getModel();
            float h = MathHelper.lerpAngleDegrees(g, playerEntity.prevBodyYaw, playerEntity.bodyYaw);
            float j = MathHelper.lerpAngleDegrees(g, playerEntity.prevHeadYaw, playerEntity.headYaw);
            float q = playerEntity.limbAnimator.getPos() - playerEntity.limbAnimator.getSpeed() * (1.0f - g);
            float p = playerEntity.limbAnimator.getSpeed(g);
            float o = (float)playerEntity.age + g;
            float k = j - h;
            float m = playerEntity.getPitch(g);
            playerEntityModel.animateModel(playerEntity, q, p, g);
            playerEntityModel.setAngles(playerEntity, q, p, o, k, m);
            boolean swimming = playerEntity.isInSwimmingPose();
            boolean sneaking = playerEntity.isSneaking();
            boolean flying = playerEntity.isFallFlying();
            ModelPart head = playerEntityModel.head;
            ModelPart leftArm = playerEntityModel.leftArm;
            ModelPart rightArm = playerEntityModel.rightArm;
            ModelPart leftLeg = playerEntityModel.leftLeg;
            ModelPart rightLeg = playerEntityModel.rightLeg;
            matrixStack.push();
            matrixStack.translate(skeletonPos.x, skeletonPos.y, skeletonPos.z);
            if (swimming) {
                matrixStack.translate(0.0f, 0.35f, 0.0f);
            }
            matrixStack.multiply(new Quaternionf().setAngleAxis((double)(h + 180.0f) * Math.PI / 180.0, 0.0, -1.0, 0.0));
            if (swimming || flying) {
                matrixStack.multiply(new Quaternionf().setAngleAxis((double)(90.0f + m) * Math.PI / 180.0, -1.0, 0.0, 0.0));
            }
            if (swimming) {
                matrixStack.translate(0.0f, -0.95f, 0.0f);
            }
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            Color skeletonColor = Modules.CLIENT_SETTING.getColor();
            bufferBuilder.vertex(matrix4f, 0.0f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, 0.0f, sneaking ? 1.05f : 1.4f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, -0.37f, sneaking ? 1.05f : 1.35f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, 0.37f, sneaking ? 1.05f : 1.35f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, -0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, 0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            matrixStack.push();
            matrixStack.translate(0.0f, sneaking ? 1.05f : 1.4f, 0.0f);
            this.rotateSkeleton(matrixStack, head);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.25f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            matrixStack.pop();
            matrixStack.push();
            matrixStack.translate(0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0.0f);
            this.rotateSkeleton(matrixStack, rightLeg);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, 0.0f, -0.6f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            matrixStack.pop();
            matrixStack.push();
            matrixStack.translate(-0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0.0f);
            this.rotateSkeleton(matrixStack, leftLeg);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, 0.0f, -0.6f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            matrixStack.pop();
            matrixStack.push();
            matrixStack.translate(0.37f, sneaking ? 1.05f : 1.35f, 0.0f);
            this.rotateSkeleton(matrixStack, rightArm);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, 0.0f, -0.55f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            matrixStack.pop();
            matrixStack.push();
            matrixStack.translate(-0.37f, sneaking ? 1.05f : 1.35f, 0.0f);
            this.rotateSkeleton(matrixStack, leftArm);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            bufferBuilder.vertex(matrix4f, 0.0f, -0.55f, 0.0f).color((float)skeletonColor.getRed() / 255.0f, (float)skeletonColor.getGreen() / 255.0f, (float)skeletonColor.getBlue() / 255.0f, 1.0f).next();
            matrixStack.pop();
            tessellator.draw();
            if (swimming) {
                matrixStack.translate(0.0f, 0.95f, 0.0f);
            }
            if (swimming || flying) {
                matrixStack.multiply(new Quaternionf().setAngleAxis((double)(90.0f + m) * Math.PI / 180.0, 1.0, 0.0, 0.0));
            }
            if (swimming) {
                matrixStack.translate(0.0f, -0.35f, 0.0f);
            }
            matrixStack.multiply(new Quaternionf().setAngleAxis((double)(h + 180.0f) * Math.PI / 180.0, 0.0, 1.0, 0.0));
            matrixStack.translate(-skeletonPos.x, -skeletonPos.y, -skeletonPos.z);
            matrixStack.pop();
        }
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    }

    private void rotateSkeleton(MatrixStack matrix, ModelPart modelPart) {
        if (modelPart.roll != 0.0f) {
            matrix.multiply(RotationAxis.POSITIVE_Z.rotation(modelPart.roll));
        }
        if (modelPart.yaw != 0.0f) {
            matrix.multiply(RotationAxis.NEGATIVE_Y.rotation(modelPart.yaw));
        }
        if (modelPart.pitch != 0.0f) {
            matrix.multiply(RotationAxis.NEGATIVE_X.rotation(modelPart.pitch));
        }
    }
}
