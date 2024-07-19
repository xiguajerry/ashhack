package dev.realme.ash.impl.event.network;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class TickMovementEvent extends Event {
   private int iterations;

   public int getIterations() {
      return this.iterations;
   }

   public void setIterations(int iterations) {
      this.iterations = iterations;
   }
}
