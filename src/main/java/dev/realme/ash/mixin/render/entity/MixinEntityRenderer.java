package dev.realme.ash.mixin.render.entity;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.entity.RenderLabelEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public abstract class MixinEntityRenderer {
   @Inject(
      method = {"renderLabelIfPresent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void hookRenderLabelIfPresent(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      RenderLabelEvent renderLabelEvent = new RenderLabelEvent(entity);
      Ash.EVENT_HANDLER.dispatch(renderLabelEvent);
      if (renderLabelEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
