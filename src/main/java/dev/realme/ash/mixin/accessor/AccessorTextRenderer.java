package dev.realme.ash.mixin.accessor;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({TextRenderer.class})
public interface AccessorTextRenderer {
   @Accessor("validateAdvance")
   boolean hookGetValidateAdvance();

   @Invoker("getFontStorage")
   FontStorage hookGetFontStorage(Identifier var1);

   @Invoker("drawGlyph")
   void hookDrawGlyph(GlyphRenderer var1, boolean var2, boolean var3, float var4, float var5, float var6, Matrix4f var7, VertexConsumer var8, float var9, float var10, float var11, float var12, int var13);

   @Invoker("drawLayer")
   float hookDrawLayer(String var1, float var2, float var3, int var4, boolean var5, Matrix4f var6, VertexConsumerProvider var7, TextRenderer.TextLayerType var8, int var9, int var10);
}
