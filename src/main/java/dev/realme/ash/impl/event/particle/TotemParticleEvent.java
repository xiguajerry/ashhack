package dev.realme.ash.impl.event.particle;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import java.awt.Color;

@Cancelable
public class TotemParticleEvent extends Event {
   private Color color;

   public Color getColor() {
      return this.color;
   }

   public void setColor(Color color) {
      this.color = color;
   }
}
