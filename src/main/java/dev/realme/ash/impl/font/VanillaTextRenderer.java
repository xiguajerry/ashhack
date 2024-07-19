package dev.realme.ash.impl.font;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.AccessorTextRenderer;
import dev.realme.ash.util.Globals;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.font.EmptyGlyphRenderer;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TextVisitFactory;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/** @deprecated */
@Deprecated
public class VanillaTextRenderer implements Globals {
   public void drawWithShadow(MatrixStack matrices, String text, float x, float y, int color) {
      if (Modules.CLIENT_SETTING.shadow.getValue()) {
         this.draw(matrices, text, x + 1.0F, y + 1.0F, color, true);
      }

      this.draw(matrices, text, x, y, color, false);
   }

   public void draw(MatrixStack matrices, String text, float x, float y, int color, boolean shadow) {
      this.draw(text, x, y, color, matrices.peek().getPositionMatrix(), shadow);
   }

   public void draw(MatrixStack matrices, String text, float x, float y, int color) {
      this.draw(text, x, y, color, matrices.peek().getPositionMatrix(), Modules.CLIENT_SETTING.shadow.getValue());
   }

   public void draw(String text, float x, float y, int color) {
      MatrixStack matrixStack = new MatrixStack();
      matrixStack.push();
      matrixStack.translate(0.0F, 0.0F, 200.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      GL11.glDepthFunc(519);
      VertexConsumerProvider.Immediate vertexConsumers = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
      this.draw(matrixStack, text, x, y, color);
      vertexConsumers.draw();
      RenderSystem.disableBlend();
      GL11.glDepthFunc(515);
      matrixStack.pop();
   }

   private void draw(String text, float x, float y, int color, Matrix4f matrix, boolean shadow) {
      if (text != null) {
         VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
         this.draw(text, x, y, color, shadow, matrix, immediate, TextLayerType.NORMAL, 15728880);
         immediate.draw();
      }
   }

   public void draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int light) {
      this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, light);
   }

   private void drawInternal(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int light) {
      Matrix4f matrix4f = new Matrix4f(matrix);
      this.drawLayer(text, x, y, color, shadow, matrix4f, vertexConsumers, layerType, light);
   }

   private void drawLayer(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int light) {
      Drawer drawer = new Drawer(vertexConsumerProvider, x, y, color, shadow, matrix, layerType, light);
      TextVisitFactory.visitFormatted(text, Style.EMPTY, drawer);
      drawer.drawLayer();
   }

   public static class Drawer implements CharacterVisitor {
      final VertexConsumerProvider vertexConsumers;
      private final float brightnessMultiplier;
      private final float red;
      private final float green;
      private final float blue;
      private final float alpha;
      private final Matrix4f matrix;
      private final TextRenderer.TextLayerType layerType;
      private final int light;
      float x;
      float y;
      private @Nullable List rectangles;

      public Drawer(VertexConsumerProvider vertexConsumers, float x, float y, int color, boolean shadow, Matrix4f matrix, TextRenderer.TextLayerType layerType, int light) {
         this.vertexConsumers = vertexConsumers;
         this.x = x;
         this.y = y;
         this.brightnessMultiplier = shadow ? 0.25F : 1.0F;
         this.red = (float)(color >> 16 & 255) / 255.0F * this.brightnessMultiplier;
         this.green = (float)(color >> 8 & 255) / 255.0F * this.brightnessMultiplier;
         this.blue = (float)(color & 255) / 255.0F * this.brightnessMultiplier;
         this.alpha = (float)(color >> 24 & 255) / 255.0F;
         this.matrix = matrix;
         this.layerType = layerType;
         this.light = light;
      }

      public boolean accept(int i, Style style, int j) {
         FontStorage fontStorage = ((AccessorTextRenderer)Globals.mc.textRenderer).hookGetFontStorage(style.getFont());
         Glyph glyph = fontStorage.getGlyph(j, ((AccessorTextRenderer)Globals.mc.textRenderer).hookGetValidateAdvance());
         GlyphRenderer glyphRenderer = style.isObfuscated() && j != 32 ? fontStorage.getObfuscatedGlyphRenderer(glyph) : fontStorage.getGlyphRenderer(j);
         boolean bl = style.isBold();
         float f = this.alpha;
         TextColor textColor = style.getColor();
         float l;
         float h;
         float g;
         if (textColor != null) {
            int k = textColor.getRgb();
            g = (float)(k >> 16 & 255) / 255.0F * this.brightnessMultiplier;
            h = (float)(k >> 8 & 255) / 255.0F * this.brightnessMultiplier;
            l = (float)(k & 255) / 255.0F * this.brightnessMultiplier;
         } else {
            g = this.red;
            h = this.green;
            l = this.blue;
         }

         float m;
         if (!(glyphRenderer instanceof EmptyGlyphRenderer)) {
            m = bl ? glyph.getBoldOffset() : 0.0F;
            VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType));
            ((AccessorTextRenderer)Globals.mc.textRenderer).hookDrawGlyph(glyphRenderer, bl, style.isItalic(), m, this.x, this.y, this.matrix, vertexConsumer, g, h, l, f, this.light);
         }

         m = glyph.getAdvance(bl);
         this.x += m;
         return true;
      }

      public void drawLayer() {
         if (this.rectangles != null) {
            GlyphRenderer glyphRenderer = ((AccessorTextRenderer)Globals.mc.textRenderer).hookGetFontStorage(Style.DEFAULT_FONT_ID).getRectangleRenderer();
            VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType));
            Iterator var3 = this.rectangles.iterator();

            while(var3.hasNext()) {
               GlyphRenderer.Rectangle rectangle = (GlyphRenderer.Rectangle)var3.next();
               glyphRenderer.drawRectangle(rectangle, this.matrix, vertexConsumer, this.light);
            }
         }

      }
   }
}
