package dev.realme.ash.api.render;

import dev.realme.ash.util.Globals;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Interpolation implements Globals {
   public static Vec3d getRenderPosition(Entity entity, float tickDelta) {
      return new Vec3d(entity.getX() - MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()), entity.getY() - MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()), entity.getZ() - MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()));
   }

   public static Vec3d getInterpolatedPosition(Entity entity, float tickDelta) {
      return new Vec3d(entity.prevX + (entity.getX() - entity.prevX) * (double)tickDelta, entity.prevY + (entity.getY() - entity.prevY) * (double)tickDelta, entity.prevZ + (entity.getZ() - entity.prevZ) * (double)tickDelta);
   }

   public static float interpolateFloat(float prev, float value, float factor) {
      return prev + (value - prev) * factor;
   }

   public static double interpolateDouble(double prev, double value, double factor) {
      return prev + (value - prev) * factor;
   }

   public static Box getInterpolatedBox(Box prevBox, Box box) {
      double delta = mc.isPaused() ? 1.0 : (double)mc.getTickDelta();
      return new Box(interpolateDouble(prevBox.minX, box.minX, delta), interpolateDouble(prevBox.minY, box.minY, delta), interpolateDouble(prevBox.minZ, box.minZ, delta), interpolateDouble(prevBox.maxX, box.maxX, delta), interpolateDouble(prevBox.maxY, box.maxY, delta), interpolateDouble(prevBox.maxZ, box.maxZ, delta));
   }

   public static Box getInterpolatedEntityBox(Entity entity) {
      Box box = entity.getBoundingBox();
      Box prevBox = entity.getBoundingBox().offset(entity.prevX - entity.getX(), entity.prevY - entity.getY(), entity.prevZ - entity.getZ());
      return getInterpolatedBox(prevBox, box);
   }
}
