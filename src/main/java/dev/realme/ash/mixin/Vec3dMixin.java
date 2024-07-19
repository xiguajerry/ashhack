package dev.realme.ash.mixin;

import dev.realme.ash.impl.imixin.IVec3d;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({Vec3d.class})
public class Vec3dMixin implements IVec3d {
   @Shadow
   @Final
   @Mutable
   public double x;
   @Shadow
   @Final
   @Mutable
   public double y;
   @Shadow
   @Final
   @Mutable
   public double z;

   public void set(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void setXZ(double x, double z) {
      this.x = x;
      this.z = z;
   }

   public void setY(double y) {
      this.y = y;
   }
}
