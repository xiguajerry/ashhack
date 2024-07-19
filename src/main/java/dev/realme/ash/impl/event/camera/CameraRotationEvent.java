package dev.realme.ash.impl.event.camera;

import dev.realme.ash.api.event.Event;
import net.minecraft.util.math.Vec2f;

public class CameraRotationEvent extends Event {
   private float yaw;
   private float pitch;
   private final float tickDelta;

   public CameraRotationEvent(float yaw, float pitch, float tickDelta) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.tickDelta = tickDelta;
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

   public void setRotation(Vec2f rotation) {
      this.yaw = rotation.x;
      this.pitch = rotation.y;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }
}
