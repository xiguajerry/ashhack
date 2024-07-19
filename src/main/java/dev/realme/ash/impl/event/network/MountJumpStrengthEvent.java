package dev.realme.ash.impl.event.network;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class MountJumpStrengthEvent extends Event {
   private float jumpStrength;

   public float getJumpStrength() {
      return this.jumpStrength;
   }

   public void setJumpStrength(float jumpStrength) {
      this.jumpStrength = jumpStrength;
   }
}
