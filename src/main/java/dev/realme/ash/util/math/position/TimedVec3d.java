package dev.realme.ash.util.math.position;

import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public record TimedVec3d(Vec3d pos, long time) implements Position {
   public TimedVec3d(Vec3d pos, long time) {
      this.pos = pos;
      this.time = time;
   }

   public double getX() {
      return this.pos.getX();
   }

   public double getY() {
      return this.pos.getY();
   }

   public double getZ() {
      return this.pos.getZ();
   }

   public Vec3d pos() {
      return this.pos;
   }

   public long time() {
      return this.time;
   }
}
