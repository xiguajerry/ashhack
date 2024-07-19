package dev.realme.ash.impl.event.particle;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

@Cancelable
public class ParticleEvent extends Event {
   private final ParticleEffect particle;

   public ParticleEvent(ParticleEffect particle) {
      this.particle = particle;
   }

   public ParticleEffect getParticle() {
      return this.particle;
   }

   public ParticleType getParticleType() {
      return this.particle.getType();
   }

   @Cancelable
   public static class Emitter extends ParticleEvent {
      public Emitter(ParticleEffect particle) {
         super(particle);
      }
   }
}
