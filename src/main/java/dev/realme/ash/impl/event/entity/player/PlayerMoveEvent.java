package dev.realme.ash.impl.event.entity.player;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

@Cancelable
public class PlayerMoveEvent extends Event {
   private final MovementType type;
   private double x;
   private double y;
   private double z;

   public PlayerMoveEvent(MovementType type, Vec3d movement) {
      this.type = type;
      this.x = movement.getX();
      this.y = movement.getY();
      this.z = movement.getZ();
   }

   public MovementType getType() {
      return this.type;
   }

   public Vec3d getMovement() {
      return new Vec3d(this.x, this.y, this.z);
   }

   public double getX() {
      return this.x;
   }

   public void setX(double x) {
      this.x = x;
   }

   public double getY() {
      return this.y;
   }

   public void setY(double y) {
      this.y = y;
   }

   public double getZ() {
      return this.z;
   }

   public void setZ(double z) {
      this.z = z;
   }
}
