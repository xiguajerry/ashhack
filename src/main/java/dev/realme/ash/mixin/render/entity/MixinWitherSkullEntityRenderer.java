package dev.realme.ash.mixin.render.entity;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.entity.RenderWitherSkullEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.WitherSkullEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.WitherSkullEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WitherSkullEntityRenderer.class})
public class MixinWitherSkullEntityRenderer {
   @Inject(
      method = {"render(Lnet/minecraft/entity/projectile/WitherSkullEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRender(WitherSkullEntity witherSkullEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      RenderWitherSkullEvent renderWitherSkullEvent = new RenderWitherSkullEvent();
      Ash.EVENT_HANDLER.dispatch(renderWitherSkullEvent);
      if (renderWitherSkullEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
