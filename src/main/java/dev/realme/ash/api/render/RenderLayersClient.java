package dev.realme.ash.api.render;

import com.google.common.collect.ImmutableMap;
import dev.realme.ash.mixin.accessor.AccessorRenderPhase;
import dev.realme.ash.util.Globals;
import net.minecraft.client.render.*;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.item.ItemRenderer;
import org.lwjgl.opengl.GL11;

public class RenderLayersClient implements Globals {
   public static final VertexFormat POSITION_COLOR_TEXTURE_OVERLAY;
   public static final RenderLayer GLINT;
   public static final RenderLayer ITEM_ENTITY_TRANSLUCENT_CULL;

   static {
      POSITION_COLOR_TEXTURE_OVERLAY = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", VertexFormats.POSITION_ELEMENT).put("Color", VertexFormats.COLOR_ELEMENT).put("UV0", VertexFormats.TEXTURE_ELEMENT).put("Padding", VertexFormats.PADDING_ELEMENT).put("UV1", VertexFormats.OVERLAY_ELEMENT).put("UV2", VertexFormats.LIGHT_ELEMENT).build());
      GLINT = RenderLayer.of("glint", VertexFormats.POSITION_TEXTURE, DrawMode.QUADS, 256, MultiPhaseParameters.builder().program(RenderPhase.GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false)).writeMaskState(RenderPhase.COLOR_MASK).cull(RenderPhase.DISABLE_CULLING).depthTest(new DepthTest()).transparency(RenderPhase.GLINT_TRANSPARENCY).texturing(RenderPhase.GLINT_TEXTURING).build(false));
      ITEM_ENTITY_TRANSLUCENT_CULL = RenderLayer.of("item_entity_translucent_cull", POSITION_COLOR_TEXTURE_OVERLAY, DrawMode.QUADS, 1536, MultiPhaseParameters.builder().program(RenderPhase.ITEM_ENTITY_TRANSLUCENT_CULL_PROGRAM).texture(RenderPhase.BLOCK_ATLAS_TEXTURE).lightmap(new Lightmap()).target(RenderPhase.ITEM_ENTITY_TARGET).writeMaskState(RenderPhase.ALL_MASK).build(true));
   }

   protected static class DepthTest extends RenderPhase.DepthTest {
      public DepthTest() {
         super("depth_test", 519);
      }

      public void startDrawing() {
         GL11.glEnable(2929);
         GL11.glDepthFunc(514);
      }

      public void endDrawing() {
         GL11.glDisable(2929);
         GL11.glDepthFunc(515);
         GL11.glDepthFunc(519);
      }
   }

   protected static class Lightmap extends RenderPhase.Lightmap {
      public Lightmap() {
         super(false);
         ((AccessorRenderPhase)this).hookSetBeginAction(() -> {
            Globals.mc.gameRenderer.getLightmapTextureManager().enable();
         });
         ((AccessorRenderPhase)this).hookSetEndAction(() -> {
            Globals.mc.gameRenderer.getLightmapTextureManager().disable();
         });
      }
   }
}
