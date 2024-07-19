package dev.realme.ash.mixin.particle;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.particle.ParticleEvent;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ParticleManager.class})
public class MixinParticleManager {
   @Inject(
      method = {"addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookAddParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable cir) {
      ParticleEvent particleEvent = new ParticleEvent(parameters);
      Ash.EVENT_HANDLER.dispatch(particleEvent);
      if (particleEvent.isCanceled()) {
         cir.setReturnValue(null);
         cir.cancel();
      }

   }

   @Inject(
      method = {"addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookAddEmitter(Entity entity, ParticleEffect parameters, int maxAge, CallbackInfo ci) {
      ParticleEvent.Emitter particleEvent = new ParticleEvent.Emitter(parameters);
      Ash.EVENT_HANDLER.dispatch(particleEvent);
      if (particleEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
