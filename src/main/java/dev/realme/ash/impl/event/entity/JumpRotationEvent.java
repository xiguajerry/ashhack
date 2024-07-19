package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Event;

public final class JumpRotationEvent extends Event {
   private float yaw;

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
