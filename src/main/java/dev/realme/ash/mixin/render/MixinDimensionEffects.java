package dev.realme.ash.mixin.render;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.world.SkyboxEvent;
import java.awt.Color;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({DimensionEffects.class})
public class MixinDimensionEffects {
   @Inject(
      method = {"getFogColorOverride"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetFogColorOverride(float skyAngle, float tickDelta, CallbackInfoReturnable cir) {
      SkyboxEvent.Fog skyboxEvent = new SkyboxEvent.Fog(tickDelta);
      Ash.EVENT_HANDLER.dispatch(skyboxEvent);
      if (skyboxEvent.isCanceled()) {
         Color color = skyboxEvent.getColor();
         cir.cancel();
         cir.setReturnValue(new float[]{(float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F});
      }

   }
}
