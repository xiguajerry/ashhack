package dev.realme.ash.mixin.accessor;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({ItemRenderer.class})
public interface AccessorItemRenderer {
   @Accessor("builtinModelItemRenderer")
   BuiltinModelItemRenderer hookGetBuiltinModelItemRenderer();

   @Invoker("renderBakedItemModel")
   void hookRenderBakedItemModel(BakedModel var1, ItemStack var2, int var3, int var4, MatrixStack var5, VertexConsumer var6);
}
