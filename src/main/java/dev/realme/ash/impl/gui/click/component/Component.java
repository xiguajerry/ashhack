package dev.realme.ash.impl.gui.click.component;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.gui.click.ClickGuiScreen;
import dev.realme.ash.util.Globals;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.ColorHelper.Argb;
import org.joml.Matrix4f;

public abstract class Component implements Drawable, Globals {
   protected float x;
   protected float y;
   protected float width;
   protected float height;

   public abstract void render(DrawContext var1, float var2, float var3, float var4);

   protected void rect(DrawContext context, int color) {
      this.fill(context, this.x, this.y, this.width, this.height, color);
   }

   protected void rectGradient(DrawContext context, int color1, int color2) {
      this.fillGradient(context, this.x, this.y, this.x + this.width, this.y + this.height, color1, color2);
   }

   protected void scale(DrawContext context, float scale) {
   }

   protected void drawRoundedRect(DrawContext context, double x1, double y1, double x2, double y2, int color) {
      this.drawRoundedRect(context, x1, y1, x2, y2, 0.0, color);
   }

   protected void drawRoundedRect(DrawContext context, double x1, double y1, double x2, double y2, double z, int color) {
      this.fill(context, x1, y1, x2, y2, z, color);
   }

   protected void drawCircle(DrawContext context, double x, double y, double radius, int color) {
      this.drawCircle(context, x, y, 0.0, radius, color);
   }

   protected void drawCircle(DrawContext context, double x, double y, double z, double radius, int color) {
   }

   protected void drawHorizontalLine(DrawContext context, double x1, double x2, double y, int color) {
      if (x2 < x1) {
         double i = x1;
         x1 = x2;
         x2 = i;
      }

      this.fill(context, x1, y, x1 + x2 + 1.0, y + 1.0, color);
   }

   protected void drawVerticalLine(DrawContext context, double x, double y1, double y2, int color) {
      if (y2 < y1) {
         double i = y1;
         y1 = y2;
         y2 = i;
      }

      this.fill(context, x, y1 + 1.0, x + 1.0, y1 + y2, color);
   }

   public void fill(DrawContext context, double x1, double y1, double x2, double y2, int color) {
      this.fill(context, x1, y1, x2, y2, 0.0, color);
   }

   public void fill(DrawContext context, double x1, double y1, double x2, double y2, double z, int color) {
      x2 += x1;
      y2 += y1;
      Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
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
      BufferBuilder buffer = RenderManager.BUFFER;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix4f, (float)x1, (float)y1, (float)z).color(g, h, j, f).next();
      buffer.vertex(matrix4f, (float)x1, (float)y2, (float)z).color(g, h, j, f).next();
      buffer.vertex(matrix4f, (float)x2, (float)y2, (float)z).color(g, h, j, f).next();
      buffer.vertex(matrix4f, (float)x2, (float)y1, (float)z).color(g, h, j, f).next();
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.disableBlend();
   }

   protected void fillGradient(DrawContext context, double startX, double startY, double endX, double endY, int colorStart, int colorEnd) {
      this.fillGradient(context, startX, startY, endX, endY, colorStart, colorEnd, 0);
   }

   protected void fillGradient(DrawContext context, double startX, double startY, double endX, double endY, int colorStart, int colorEnd, int z) {
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      this.fillGradient(context.getMatrices().peek().getPositionMatrix(), buffer, startX, startY, endX, endY, z, colorStart, colorEnd);
      tessellator.draw();
      RenderSystem.disableBlend();
   }

   protected void fillGradient(Matrix4f matrix, BufferBuilder builder, double startX, double startY, double endX, double endY, double z, int colorStart, int colorEnd) {
      float f = (float)Argb.getAlpha(colorStart) / 255.0F;
      float g = (float)Argb.getRed(colorStart) / 255.0F;
      float h = (float)Argb.getGreen(colorStart) / 255.0F;
      float i = (float)Argb.getBlue(colorStart) / 255.0F;
      float j = (float)Argb.getAlpha(colorEnd) / 255.0F;
      float k = (float)Argb.getRed(colorEnd) / 255.0F;
      float l = (float)Argb.getGreen(colorEnd) / 255.0F;
      float m = (float)Argb.getBlue(colorEnd) / 255.0F;
      builder.vertex(matrix, (float)startX, (float)startY, (float)z).color(k, l, m, j).next();
      builder.vertex(matrix, (float)startX, (float)endY, (float)z).color(k, l, m, j).next();
      builder.vertex(matrix, (float)endX, (float)endY, (float)z).color(g, h, i, f).next();
      builder.vertex(matrix, (float)endX, (float)startY, (float)z).color(g, h, i, f).next();
   }

   protected void fillGradientQuad(DrawContext context, float x1, float y1, float x2, float y2, int startColor, int endColor, boolean sideways) {
      float f = (float)(startColor >> 24 & 255) / 255.0F;
      float f1 = (float)(startColor >> 16 & 255) / 255.0F;
      float f2 = (float)(startColor >> 8 & 255) / 255.0F;
      float f3 = (float)(startColor & 255) / 255.0F;
      float f4 = (float)(endColor >> 24 & 255) / 255.0F;
      float f5 = (float)(endColor >> 16 & 255) / 255.0F;
      float f6 = (float)(endColor >> 8 & 255) / 255.0F;
      float f7 = (float)(endColor & 255) / 255.0F;
      BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
      Matrix4f posMatrix = context.getMatrices().peek().getPositionMatrix();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      if (sideways) {
         bufferBuilder.vertex(posMatrix, x1, y1, 0.0F).color(f1, f2, f3, f).next();
         bufferBuilder.vertex(posMatrix, x1, y2, 0.0F).color(f1, f2, f3, f).next();
         bufferBuilder.vertex(posMatrix, x2, y2, 0.0F).color(f5, f6, f7, f4).next();
         bufferBuilder.vertex(posMatrix, x2, y1, 0.0F).color(f5, f6, f7, f4).next();
      } else {
         bufferBuilder.vertex(posMatrix, x2, y1, 0.0F).color(f1, f2, f3, f).next();
         bufferBuilder.vertex(posMatrix, x1, y1, 0.0F).color(f1, f2, f3, f).next();
         bufferBuilder.vertex(posMatrix, x1, y2, 0.0F).color(f5, f6, f7, f4).next();
         bufferBuilder.vertex(posMatrix, x2, y2, 0.0F).color(f5, f6, f7, f4).next();
      }

      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      RenderSystem.disableBlend();
   }

   public void drawWithOutline(int x, int y, BiConsumer renderAction) {
      RenderSystem.blendFuncSeparate(SrcFactor.ZERO, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      renderAction.accept(x + 1, y);
      renderAction.accept(x - 1, y);
      renderAction.accept(x, y + 1);
      renderAction.accept(x, y - 1);
      RenderSystem.defaultBlendFunc();
      renderAction.accept(x, y);
   }

   public void drawSprite(DrawContext context, int x, int y, int z, int width, int height, Sprite sprite) {
      this.drawTexturedQuad(context.getMatrices().peek().getPositionMatrix(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
   }

   public void drawSprite(DrawContext context, int x, int y, int z, int width, int height, Sprite sprite, float red, float green, float blue, float alpha) {
      this.drawTexturedQuad(context.getMatrices().peek().getPositionMatrix(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), red, green, blue, alpha);
   }

   public void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
      this.fill(context, x, y, x + width, y + 1, color);
      this.fill(context, x, y + height - 1, x + width, y + height, color);
      this.fill(context, x, y + 1, x + 1, y + height - 1, color);
      this.fill(context, x + width - 1, y + 1, x + width, y + height - 1, color);
   }

   public void drawTexture(DrawContext context, int x, int y, int u, int v, int width, int height) {
      this.drawTexture(context, x, y, 0, (float)u, (float)v, width, height, 256, 256);
   }

   public void drawTexture(DrawContext context, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      this.drawTexture(context, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
   }

   public void drawTexture(DrawContext context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
      this.drawTexture(context, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
   }

   public void drawTexture(DrawContext context, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      this.drawTexture(context, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
   }

   private void drawTexture(DrawContext context, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
      this.drawTexturedQuad(context.getMatrices().peek().getPositionMatrix(), x0, x1, y0, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
   }

   private void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      BufferBuilder buffer = Tessellator.getInstance().getBuffer();
      buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      buffer.vertex(matrix, (float)x0, (float)y0, (float)z).texture(u0, v0).next();
      buffer.vertex(matrix, (float)x0, (float)y1, (float)z).texture(u0, v1).next();
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
      buffer.vertex(matrix, (float)x1, (float)y0, (float)z).texture(u1, v0).next();
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }

   private void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, float red, float green, float blue, float alpha) {
      RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
      RenderSystem.enableBlend();
      BufferBuilder buffer = Tessellator.getInstance().getBuffer();
      buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
      buffer.vertex(matrix, (float)x0, (float)y0, (float)z).color(red, green, blue, alpha).texture(u0, v0).next();
      buffer.vertex(matrix, (float)x0, (float)y1, (float)z).color(red, green, blue, alpha).texture(u0, v1).next();
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z).color(red, green, blue, alpha).texture(u1, v1).next();
      buffer.vertex(matrix, (float)x1, (float)y0, (float)z).color(red, green, blue, alpha).texture(u1, v0).next();
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.disableBlend();
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getHeight() {
      return this.height;
   }

   public void setHeight(float height) {
      this.height = height;
   }

   public float getWidth() {
      return this.width;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public boolean isWithin(double xval, double yval) {
      return this.isWithin((float)xval, (float)yval);
   }

   public boolean isWithin(float xval, float yval) {
      return this.isMouseOver(xval, yval, this.x, this.y, this.width, this.height);
   }

   public boolean isMouseOver(double mx, double my, double x1, double y1, double x2, double y2) {
      return mx >= x1 && mx <= x1 + x2 && my >= y1 && my <= y1 + y2;
   }

   public void setPos(float x, float y) {
      this.x = x;
      this.y = y;
   }

   public void setDimensions(float width, float height) {
      this.setWidth(width);
      this.setHeight(height);
   }

   public void enableScissor(int x1, int y1, int x2, int y2) {
      this.setScissor(ClickGuiScreen.SCISSOR_STACK.push(new ScreenRect(x1, y1, x2 - x1, y2 - y1)));
   }

   public void disableScissor() {
      this.setScissor(ClickGuiScreen.SCISSOR_STACK.pop());
   }

   private void setScissor(ScreenRect rect) {
      if (rect != null) {
         Window window = mc.getWindow();
         int i = window.getFramebufferHeight();
         double d = window.getScaleFactor();
         double e = (double)rect.getLeft() * d;
         double f = (double)i - (double)rect.getBottom() * d;
         double g = (double)rect.width() * d;
         double h = (double)rect.height() * d;
         RenderSystem.enableScissor((int)e, (int)f, Math.max(0, (int)g), Math.max(0, (int)h));
      } else {
         RenderSystem.disableScissor();
      }

   }
}
