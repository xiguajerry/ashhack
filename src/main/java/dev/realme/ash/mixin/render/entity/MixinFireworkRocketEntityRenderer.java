package dev.realme.ash.mixin.render.entity;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.entity.RenderFireworkRocketEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FireworkRocketEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FireworkRocketEntityRenderer.class})
public class MixinFireworkRocketEntityRenderer {
   @Inject(
      method = {"render(Lnet/minecraft/entity/projectile/FireworkRocketEntity;FFLnet/minecraft/client/util/math/ MatrixStack;Lnet/minecraft/client/render/ VertexConsumerProvider;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRender(FireworkRocketEntity fireworkRocketEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      RenderFireworkRocketEvent renderFireworkRocketEvent = new RenderFireworkRocketEvent();
      Ash.EVENT_HANDLER.dispatch(renderFireworkRocketEvent);
      if (renderFireworkRocketEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
