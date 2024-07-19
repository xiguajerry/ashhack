package dev.realme.ash.impl.event.render;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class TickCounterEvent extends Event {
   private float ticks;

   public float getTicks() {
      return this.ticks;
   }

   public void setTicks(float ticks) {
      this.ticks = ticks;
   }
}
