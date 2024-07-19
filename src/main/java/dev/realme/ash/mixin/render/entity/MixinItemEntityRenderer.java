package dev.realme.ash.mixin.render.entity;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.entity.RenderItemEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntityRenderer.class})
public class MixinItemEntityRenderer {
   @Inject(
      method = {"render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRender(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      RenderItemEvent renderItemEvent = new RenderItemEvent(itemEntity);
      Ash.EVENT_HANDLER.dispatch(renderItemEvent);
      if (renderItemEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
