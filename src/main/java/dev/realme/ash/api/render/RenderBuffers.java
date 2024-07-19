package dev.realme.ash.api.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.math.ColorHelper.Argb;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class RenderBuffers {
   public static final Buffer QUADS;
   public static final Buffer LINES;
   private static final List postRenderCallbacks;
   private static boolean isSetup;

   public static void preRender() {
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      RenderSystem.enableCull();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      isSetup = true;
   }

   public static void postRender() {
      QUADS.draw();
      LINES.draw();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      RenderSystem.disableCull();
      GL11.glDisable(2848);
      isSetup = false;

       for (Object postRenderCallback : postRenderCallbacks) {
           Runnable callback = (Runnable) postRenderCallback;
           callback.run();
       }

      postRenderCallbacks.clear();
   }

   public static void post(Runnable callback) {
      if (isSetup) {
         postRenderCallbacks.add(callback);
      } else {
         callback.run();
      }

   }

   static {
      QUADS = new Buffer(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      LINES = new Buffer(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      postRenderCallbacks = new ArrayList();
      isSetup = false;
   }

   public static class Buffer {
      public final BufferBuilder buffer = new BufferBuilder(2048);
      private final VertexFormat.DrawMode drawMode;
      private final VertexFormat vertexFormat;
      private Matrix4f positionMatrix;

      public Buffer(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat) {
         this.drawMode = drawMode;
         this.vertexFormat = vertexFormat;
      }

      public void begin(Matrix4f positionMatrix) {
         this.positionMatrix = positionMatrix;
         if (!this.buffer.isBuilding()) {
            this.buffer.begin(this.drawMode, this.vertexFormat);
         }

      }

      public void end() {
         if (!RenderBuffers.isSetup) {
            this.draw();
         }

      }

      public Buffer vertex(double x, double y, double z) {
         return this.vertex((float)x, (float)y, (float)z);
      }

      public Buffer vertex(float x, float y, float z) {
         this.buffer.vertex(this.positionMatrix, x, y, z).next();
         return this;
      }

      public void color(int color) {
         this.buffer.fixedColor(Argb.getRed(color), Argb.getGreen(color), Argb.getBlue(color), Argb.getAlpha(color));
      }

      public void draw() {
         if (this.buffer.isBuilding()) {
            if (this.buffer.isBatchEmpty()) {
               this.buffer.clear();
            } else {
               RenderSystem.setShader(GameRenderer::getPositionColorProgram);
               BufferRenderer.drawWithGlobalProgram(this.buffer.end());
            }
         }

      }
   }
}
