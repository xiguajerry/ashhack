package dev.realme.ash.impl.imixin;

import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IVec3d {
   void set(double var1, double var3, double var5);

   default void set(Vec3i vec) {
      this.set(vec.getX(), vec.getY(), vec.getZ());
   }

   default void set(Vector3d vec) {
      this.set(vec.x, vec.y, vec.z);
   }

   void setXZ(double var1, double var3);

   void setY(double var1);
}
