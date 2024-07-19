package dev.realme.ash.mixin.biome;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.biome.BiomeEffectsEvent;
import dev.realme.ash.impl.event.world.SkyboxEvent;
import java.util.Optional;
import net.minecraft.world.biome.BiomeEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BiomeEffects.class})
public class MixinBiomeEffects {
   @Inject(
      method = {"getSkyColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetSkyColor(CallbackInfoReturnable cir) {
      SkyboxEvent.Sky skyboxEvent = new SkyboxEvent.Sky();
      Ash.EVENT_HANDLER.dispatch(skyboxEvent);
      if (skyboxEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(skyboxEvent.getRGB());
      }

   }

   @Inject(
      method = {"getParticleConfig"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetParticleConfig(CallbackInfoReturnable cir) {
      BiomeEffectsEvent biomeEffectsEvent = new BiomeEffectsEvent();
      Ash.EVENT_HANDLER.dispatch(biomeEffectsEvent);
      if (biomeEffectsEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(Optional.ofNullable(biomeEffectsEvent.getParticleConfig()));
      }

   }
}
