package dev.realme.ash.util.player;

import dev.realme.ash.util.Globals;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class RayCastUtil implements Globals {
   public static HitResult raycastEntity(double reach, Vec3d position, float[] angles) {
      if (mc.gameRenderer == null) {
         return null;
      } else if (position == null) {
         return null;
      } else {
         Camera view = mc.gameRenderer.getCamera();
         Vec3d vec3d2 = RotationUtil.getRotationVector(angles[1], angles[0]);
         Vec3d vec3d3 = position.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);
         Box box = (new Box(position, position)).stretch(vec3d2.multiply(reach)).expand(1.0, 1.0, 1.0);
         return ProjectileUtil.raycast(view.getFocusedEntity(), position, vec3d3, box, (entity) -> {
            return !entity.isSpectator() && entity.canHit();
         }, reach * reach);
      }
   }

   public static HitResult raycastEntity(double reach) {
      if (mc.gameRenderer == null) {
         return null;
      } else {
         Camera view = mc.gameRenderer.getCamera();
         Vec3d vec3d = view.getPos();
         Vec3d vec3d2 = RotationUtil.getRotationVector(view.getPitch(), view.getYaw());
         Vec3d vec3d3 = vec3d.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);
         if (view.getFocusedEntity() == null) {
            return null;
         } else {
            Box box = view.getFocusedEntity().getBoundingBox().stretch(vec3d2.multiply(reach)).expand(1.0, 1.0, 1.0);
            return box == null ? null : ProjectileUtil.raycast(view.getFocusedEntity(), vec3d, vec3d3, box, (entity) -> {
               return !entity.isSpectator() && entity.canHit();
            }, reach * reach);
         }
      }
   }

   public static HitResult rayCast(double reach, float[] angles) {
      double eyeHeight = (double)mc.player.getStandingEyeHeight();
      Vec3d eyes = new Vec3d(mc.player.getX(), mc.player.getY() + eyeHeight, mc.player.getZ());
      return rayCast(reach, eyes, angles);
   }

   public static HitResult rayCast(double reach, Vec3d position, float[] angles) {
      if (!Float.isNaN(angles[0]) && !Float.isNaN(angles[1])) {
         Vec3d rotationVector = RotationUtil.getRotationVector(angles[1], angles[0]);
         return mc.world.raycast(new RaycastContext(position, position.add(rotationVector.x * reach, rotationVector.y * reach, rotationVector.z * reach), ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
      } else {
         return null;
      }
   }
}
