package dev.realme.ash.impl.event.biome;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.world.biome.BiomeParticleConfig;

@Cancelable
public class BiomeEffectsEvent extends Event {
   private BiomeParticleConfig particleConfig;

   public BiomeParticleConfig getParticleConfig() {
      return this.particleConfig;
   }

   public void setParticleConfig(BiomeParticleConfig particleConfig) {
      this.particleConfig = particleConfig;
   }
}
