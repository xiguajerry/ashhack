package dev.realme.ash.impl.event.network;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class ReachEvent extends Event {
   private float reach;

   public float getReach() {
      return this.reach;
   }

   public void setReach(float reach) {
      this.reach = reach;
   }
}
