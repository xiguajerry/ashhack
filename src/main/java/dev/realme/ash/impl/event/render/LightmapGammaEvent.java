package dev.realme.ash.impl.event.render;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class LightmapGammaEvent extends Event {
   private int gamma;

   public LightmapGammaEvent(int gamma) {
      this.gamma = gamma;
   }

   public int getGamma() {
      return this.gamma;
   }

   public void setGamma(int gamma) {
      this.gamma = gamma;
   }
}
