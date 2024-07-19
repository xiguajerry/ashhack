package dev.realme.ash.api.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.realme.ash.init.Fonts;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.AccessorWorldRenderer;
import dev.realme.ash.util.Globals;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.ColorHelper.Argb;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class RenderManager implements Globals {
   public static final Tessellator TESSELLATOR = RenderSystem.renderThreadTesselator();
   public static final BufferBuilder BUFFER;

   public static void post(Runnable callback) {
      RenderBuffers.post(callback);
   }

   public static void renderBox(MatrixStack matrices, BlockPos p, int color) {
      renderBox(matrices, new Box(p), color);
   }

   public static void renderBox(MatrixStack matrices, Box box, int color) {
      if (isFrustumVisible(box)) {
         matrices.push();
         drawBox(matrices, box, color);
         matrices.pop();
      }
   }

   public static void drawBox(MatrixStack matrices, Box box, int color) {
      drawBox(matrices, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
   }

   public static void drawBox(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      RenderBuffers.QUADS.begin(matrix4f);
      RenderBuffers.QUADS.color(color);
      RenderBuffers.QUADS.vertex(x1, y1, z1).vertex(x2, y1, z1).vertex(x2, y1, z2).vertex(x1, y1, z2);
      RenderBuffers.QUADS.vertex(x1, y2, z1).vertex(x1, y2, z2).vertex(x2, y2, z2).vertex(x2, y2, z1);
      RenderBuffers.QUADS.vertex(x1, y1, z1).vertex(x1, y2, z1).vertex(x2, y2, z1).vertex(x2, y1, z1);
      RenderBuffers.QUADS.vertex(x2, y1, z1).vertex(x2, y2, z1).vertex(x2, y2, z2).vertex(x2, y1, z2);
      RenderBuffers.QUADS.vertex(x1, y1, z2).vertex(x2, y1, z2).vertex(x2, y2, z2).vertex(x1, y2, z2);
      RenderBuffers.QUADS.vertex(x1, y1, z1).vertex(x1, y1, z2).vertex(x1, y2, z2).vertex(x1, y2, z1);
      RenderBuffers.QUADS.end();
   }

   public static void renderBoundingBox(MatrixStack matrices, BlockPos p, float width, int color) {
      renderBoundingBox(matrices, new Box(p), width, color);
   }

   public static void renderBoundingBox(MatrixStack matrices, Box box, float width, int color) {
      if (isFrustumVisible(box)) {
         matrices.push();
         RenderSystem.lineWidth(width);
         drawBoundingBox(matrices, box, color);
         matrices.pop();
      }
   }

   public static void drawBoundingBox(MatrixStack matrices, Box box, int color) {
      drawBoundingBox(matrices, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
   }

   public static void drawBoundingBox(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      RenderBuffers.LINES.begin(matrix4f);
      RenderBuffers.LINES.color(color);
      RenderBuffers.LINES.vertex(x1, y1, z1).vertex(x2, y1, z1);
      RenderBuffers.LINES.vertex(x2, y1, z1).vertex(x2, y1, z2);
      RenderBuffers.LINES.vertex(x2, y1, z2).vertex(x1, y1, z2);
      RenderBuffers.LINES.vertex(x1, y1, z2).vertex(x1, y1, z1);
      RenderBuffers.LINES.vertex(x1, y1, z1).vertex(x1, y2, z1);
      RenderBuffers.LINES.vertex(x2, y1, z1).vertex(x2, y2, z1);
      RenderBuffers.LINES.vertex(x2, y1, z2).vertex(x2, y2, z2);
      RenderBuffers.LINES.vertex(x1, y1, z2).vertex(x1, y2, z2);
      RenderBuffers.LINES.vertex(x1, y2, z1).vertex(x2, y2, z1);
      RenderBuffers.LINES.vertex(x2, y2, z1).vertex(x2, y2, z2);
      RenderBuffers.LINES.vertex(x2, y2, z2).vertex(x1, y2, z2);
      RenderBuffers.LINES.vertex(x1, y2, z2).vertex(x1, y2, z1);
      RenderBuffers.LINES.end();
   }

   public static void renderLine(MatrixStack matrices, Vec3d s, Vec3d d, float width, int color) {
      renderLine(matrices, s.x, s.y, s.z, d.x, d.y, d.z, width, color);
   }

   public static void renderLine(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, float width, int color) {
      matrices.push();
      RenderSystem.lineWidth(width);
      drawLine(matrices, x1, y1, z1, x2, y2, z2, color);
      matrices.pop();
   }

   public static void drawLine(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      RenderBuffers.LINES.begin(matrix4f);
      RenderBuffers.LINES.color(color);
      RenderBuffers.LINES.vertex(x1, y1, z1);
      RenderBuffers.LINES.vertex(x2, y2, z2);
      RenderBuffers.LINES.end();
   }

   public static void renderSign(String text, Vec3d pos) {
      renderSign(text, pos.getX(), pos.getY(), pos.getZ());
   }

   public static void renderSign(String text, double x1, double x2, double x3) {
      double dist = Math.sqrt(mc.player.squaredDistanceTo(x1, x2, x3));
      float scaling = 0.0018F + Modules.NAMETAGS.getScaling() * (float)dist;
      if (dist <= 8.0) {
         scaling = 0.0245F;
      }

      Camera camera = mc.gameRenderer.getCamera();
      Vec3d pos = camera.getPos();
      MatrixStack matrixStack = new MatrixStack();
      matrixStack.push();
      matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
      matrixStack.translate(x1 - pos.getX(), x2 - pos.getY(), x3 - pos.getZ());
      matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      matrixStack.scale(-scaling, -scaling, -1.0F);
      GL11.glDepthFunc(519);
      VertexConsumerProvider.Immediate vertexConsumers = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
      float hwidth = (float)mc.textRenderer.getWidth(text) / 2.0F;
      Fonts.VANILLA.drawWithShadow(matrixStack, text, -hwidth, 0.0F, -1);
      vertexConsumers.draw();
      RenderSystem.disableBlend();
      GL11.glDepthFunc(515);
      matrixStack.pop();
   }

   public static boolean isFrustumVisible(Box box) {
      return ((AccessorWorldRenderer)mc.worldRenderer).getFrustum().isVisible(box);
   }

   public static void rect(MatrixStack matrices, double x1, double y1, double x2, double y2, int color) {
      rect(matrices, x1, y1, x2, y2, 0.0, color);
   }

   public static void rect(MatrixStack matrices, double x1, double y1, double x2, double y2, double z, int color) {
      x2 += x1;
      y2 += y1;
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      double i;
      if (x1 < x2) {
         i = x1;
         x1 = x2;
         x2 = i;
      }

      if (y1 < y2) {
         i = y1;
         y1 = y2;
         y2 = i;
      }

      float f = (float)Argb.getAlpha(color) / 255.0F;
      float g = (float)Argb.getRed(color) / 255.0F;
      float h = (float)Argb.getGreen(color) / 255.0F;
      float j = (float)Argb.getBlue(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BUFFER.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      BUFFER.vertex(matrix4f, (float)x1, (float)y1, (float)z).color(g, h, j, f).next();
      BUFFER.vertex(matrix4f, (float)x1, (float)y2, (float)z).color(g, h, j, f).next();
      BUFFER.vertex(matrix4f, (float)x2, (float)y2, (float)z).color(g, h, j, f).next();
      BUFFER.vertex(matrix4f, (float)x2, (float)y1, (float)z).color(g, h, j, f).next();
      BufferRenderer.drawWithGlobalProgram(BUFFER.end());
      RenderSystem.disableBlend();
   }

   public static void renderText(DrawContext context, String text, float x, float y, int color) {
      context.drawText(mc.textRenderer, text, (int)x, (int)y, color, Modules.CLIENT_SETTING.shadow.getValue());
   }

   public static int textWidth(String text) {
      return mc.textRenderer.getWidth(text);
   }

   static {
      BUFFER = TESSELLATOR.getBuffer();
   }
}
