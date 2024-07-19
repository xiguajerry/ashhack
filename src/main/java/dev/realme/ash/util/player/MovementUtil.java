package dev.realme.ash.util.player;

import dev.realme.ash.util.Globals;
import net.minecraft.client.input.Input;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class MovementUtil implements Globals {
   public static double getMotionX() {
      return mc.player.getVelocity().x;
   }

   public static double getMotionY() {
      return mc.player.getVelocity().y;
   }

   public static double getMotionZ() {
      return mc.player.getVelocity().z;
   }

   public static void setMotionXZ(double x, double z) {
      Vec3d motion = mc.player.getVelocity();
      mc.player.setVelocity(x, motion.y, z);
   }

   public static void setMotionX(double x) {
      Vec3d velocity = new Vec3d(x, mc.player.getVelocity().y, mc.player.getVelocity().z);
      mc.player.setVelocity(velocity);
   }

   public static void setMotionY(double y) {
      Vec3d velocity = new Vec3d(mc.player.getVelocity().x, y, mc.player.getVelocity().z);
      mc.player.setVelocity(velocity);
   }

   public static void setMotionZ(double z) {
      Vec3d velocity = new Vec3d(mc.player.getVelocity().x, mc.player.getVelocity().y, z);
      mc.player.setVelocity(velocity);
   }

   public static double getDistance2D() {
      double xDist = mc.player.getX() - mc.player.prevX;
      double zDist = mc.player.getZ() - mc.player.prevZ;
      return Math.sqrt(xDist * xDist + zDist * zDist);
   }

   public static double getSpeed(boolean slowness) {
      double defaultSpeed = 0.2873;
      return getSpeed(slowness, defaultSpeed);
   }

   public static double getSpeed(boolean slowness, double defaultSpeed) {
      int amplifier;
      if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
         amplifier = ((StatusEffectInstance)mc.player.getActiveStatusEffects().get(StatusEffects.SPEED)).getAmplifier();
         defaultSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
      }

      if (slowness && mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
         amplifier = ((StatusEffectInstance)mc.player.getActiveStatusEffects().get(StatusEffects.SLOWNESS)).getAmplifier();
         defaultSpeed /= 1.0 + 0.2 * (double)(amplifier + 1);
      }

      if (mc.player.isSneaking()) {
         defaultSpeed /= 5.0;
      }

      return defaultSpeed;
   }

   public static double getMoveForward() {
      return (double)mc.player.input.movementForward;
   }

   public static double getMoveStrafe() {
      return (double)mc.player.input.movementSideways;
   }

   public static double getJumpSpeed() {
      double defaultSpeed = 0.0;
      if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
         int amplifier = ((StatusEffectInstance)mc.player.getActiveStatusEffects().get(StatusEffects.JUMP_BOOST)).getAmplifier();
         defaultSpeed += (double)(amplifier + 1) * 0.1;
      }

      return defaultSpeed;
   }

   public static double[] directionSpeed(double speed) {
      float forward = mc.player.input.movementForward;
      float side = mc.player.input.movementSideways;
      float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getTickDelta();
      if (forward != 0.0F) {
         if (side > 0.0F) {
            yaw += (float)(forward > 0.0F ? -45 : 45);
         } else if (side < 0.0F) {
            yaw += (float)(forward > 0.0F ? 45 : -45);
         }

         side = 0.0F;
         if (forward > 0.0F) {
            forward = 1.0F;
         } else if (forward < 0.0F) {
            forward = -1.0F;
         }
      }

      double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double posX = (double)forward * speed * cos + (double)side * speed * sin;
      double posZ = (double)forward * speed * sin - (double)side * speed * cos;
      return new double[]{posX, posZ};
   }

   public static boolean isInputtingMovement() {
      return mc.player.input.pressingForward || mc.player.input.pressingBack || mc.player.input.pressingLeft || mc.player.input.pressingRight;
   }

   public static boolean isMovingInput() {
      return mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F;
   }

   public static boolean isMoving() {
      double d = mc.player.getX() - mc.player.lastX;
      double e = mc.player.getY() - mc.player.lastBaseY;
      double f = mc.player.getZ() - mc.player.lastZ;
      return MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4);
   }

   public static Vec2f applySafewalk(double motionX, double motionZ) {
      double offset = 0.05;
      double moveX = motionX;
      double moveZ = motionZ;
      float fallDist = -mc.player.getStepHeight();
      if (!mc.player.isOnGround()) {
         fallDist = -1.5F;
      }

      while(moveX != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(moveX, (double)fallDist, 0.0))) {
         if (moveX < 0.05 && moveX >= -0.05) {
            moveX = 0.0;
         } else if (moveX > 0.0) {
            moveX -= 0.05;
         } else {
            moveX += 0.05;
         }
      }

      while(moveZ != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0.0, (double)fallDist, moveZ))) {
         if (moveZ < 0.05 && moveZ >= -0.05) {
            moveZ = 0.0;
         } else if (moveZ > 0.0) {
            moveZ -= 0.05;
         } else {
            moveZ += 0.05;
         }
      }

      while(moveX != 0.0 && moveZ != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(moveX, (double)fallDist, moveZ))) {
         if (moveX < 0.05 && moveX >= -0.05) {
            moveX = 0.0;
         } else if (moveX > 0.0) {
            moveX -= 0.05;
         } else {
            moveX += 0.05;
         }

         if (moveZ < 0.05 && moveZ >= -0.05) {
            moveZ = 0.0;
         } else if (moveZ > 0.0) {
            moveZ -= 0.05;
         } else {
            moveZ += 0.05;
         }
      }

      return new Vec2f((float)moveX, (float)moveZ);
   }

   public static float getYawOffset(Input input, float rotationYaw) {
      if (input.movementForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (input.movementForward < 0.0F) {
         forward = -0.5F;
      } else if (input.movementForward > 0.0F) {
         forward = 0.5F;
      }

      float strafe = input.movementSideways;
      if (strafe > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (strafe < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return rotationYaw;
   }
}
