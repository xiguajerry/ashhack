package dev.realme.ash.impl.event;

import dev.realme.ash.api.event.StageEvent;

public class RotateEvent extends StageEvent {
   private float yaw;
   private float pitch;

   public RotateEvent(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public void setRotation(float yaw, float pitch) {
      this.setYaw(yaw);
      this.setPitch(pitch);
   }
}
