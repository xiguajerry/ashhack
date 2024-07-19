package dev.realme.ash.mixin.biome;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.world.SkyboxEvent;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Biome.class})
public class MixinBiome {
   @Inject(
      method = {"getFogColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetFogColor(CallbackInfoReturnable cir) {
      SkyboxEvent.Fog skyboxEvent = new SkyboxEvent.Fog(0.0F);
      Ash.EVENT_HANDLER.dispatch(skyboxEvent);
      if (skyboxEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(skyboxEvent.getRGB());
      }

   }
}
