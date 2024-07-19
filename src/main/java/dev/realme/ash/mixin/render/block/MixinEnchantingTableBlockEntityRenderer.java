package dev.realme.ash.mixin.render.block;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.block.RenderTileEntityEvent;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EnchantingTableBlockEntityRenderer.class})
public class MixinEnchantingTableBlockEntityRenderer {
   @Shadow
   @Final
   private BookModel book;

   @Inject(
      method = {"render(Lnet/minecraft/block/entity/EnchantingTableBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/entity/model/BookModel;renderBook(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V",
   shift = Shift.BEFORE
)},
      cancellable = true
   )
   private void hookRender(EnchantingTableBlockEntity enchantingTableBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci) {
      RenderTileEntityEvent.EnchantingTableBook renderTileEntityEvent = new RenderTileEntityEvent.EnchantingTableBook();
      Ash.EVENT_HANDLER.dispatch(renderTileEntityEvent);
      if (renderTileEntityEvent.isCanceled()) {
         ci.cancel();
         matrixStack.pop();
      }

   }
}
