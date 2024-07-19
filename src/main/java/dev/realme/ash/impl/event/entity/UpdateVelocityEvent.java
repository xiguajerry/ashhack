package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.StageEvent;
import net.minecraft.util.math.Vec3d;

@Cancelable
public class UpdateVelocityEvent extends StageEvent {
   private final Vec3d movementInput;
   private final float speed;
   private final float yaw;
   private Vec3d velocity;

   public UpdateVelocityEvent(Vec3d movementInput, float speed, float yaw, Vec3d velocity) {
      this.movementInput = movementInput;
      this.speed = speed;
      this.yaw = yaw;
      this.velocity = velocity;
   }

   public Vec3d getMovementInput() {
      return this.movementInput;
   }

   public float getSpeed() {
      return this.speed;
   }

   public Vec3d getVelocity() {
      return this.velocity;
   }

   public void setVelocity(Vec3d velocity) {
      this.velocity = velocity;
   }
}
