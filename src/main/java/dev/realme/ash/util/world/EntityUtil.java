package dev.realme.ash.util.world;

import dev.realme.ash.impl.module.client.CombatSettingModule;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.math.MathUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.player.RotationUtil;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class EntityUtil {
   public static PlayerEntity getTarget(double targetRange) {
      double min = Double.MAX_VALUE;
      Entity attackTarget = null;
      Iterator var5 = Globals.mc.world.getEntities().iterator();

      while(true) {
         Entity entity;
         do {
            do {
               do {
                  do {
                     do {
                        if (!var5.hasNext()) {
                           return (PlayerEntity)attackTarget;
                        }

                        entity = (Entity)var5.next();
                     } while(entity == null);
                  } while(entity == Globals.mc.player);
               } while(!entity.isAlive());
            } while(!(entity instanceof PlayerEntity));
         } while(entity.getDisplayName() != null && Managers.SOCIAL.isFriend(entity.getName()));

         double dist = Globals.mc.player.squaredDistanceTo(entity);
         if (!(dist > targetRange * targetRange)) {
            LivingEntity e;
            float armor;
            switch (Modules.COMBAT_SETTING.priority.getValue()) {
               case FOV:
                  Vec3d entityPos = entity.getEyePos();
                  float[] rots = RotationUtil.getRotationsTo(getEyesPos(), entityPos);
                  float diff = MathHelper.wrapDegrees(Globals.mc.player.getYaw()) - rots[0];
                  float magnitude = Math.abs(diff);
                  if ((double)magnitude < min) {
                     min = magnitude;
                     attackTarget = entity;
                  }
                  break;
               case DISTANCE:
                  if (dist < min) {
                     min = dist;
                     attackTarget = entity;
                  }
                  break;
               case HEALTH:
                  e = (LivingEntity)entity;
                  armor = e.getHealth() + e.getAbsorptionAmount();
                  if ((double)armor < min) {
                     min = armor;
                     attackTarget = entity;
                  }
                  break;
               case ARMOR:
                  e = (LivingEntity)entity;
                  armor = getArmorDurability(e);
                  if ((double)armor < min) {
                     min = armor;
                     attackTarget = entity;
                  }
            }
         }
      }
   }

   public static float getArmorDurability(LivingEntity e) {
      float edmg = 0.0F;
      float emax = 0.0F;

       for (ItemStack armor : e.getArmorItems()) {
           if (armor != null && !armor.isEmpty()) {
               edmg += (float) armor.getDamage();
               emax += (float) armor.getMaxDamage();
           }
       }

      return 100.0F - edmg / emax;
   }

   public static Vec3d getEntityPosVec(PlayerEntity entity, int ticks) {
      return entity.getPos().add(getMotionVec(entity, ticks));
   }

   public static Vec3d getMotionVec(Entity entity, int ticks) {
      double dX = entity.getX() - entity.prevX;
      double dY = entity.getY() - entity.prevY;
      double dZ = entity.getZ() - entity.prevZ;
      double entityMotionPosX = 0.0;
      double entityMotionPosY = 0.0;
      double entityMotionPosZ = 0.0;

      for(double i = 1.0; i <= (double)ticks && !Globals.mc.world.canCollide(entity, entity.getBoundingBox().offset(new Vec3d(dX * i, dY * i, dZ * i))); i += 0.5) {
         entityMotionPosX = dX * i;
         entityMotionPosY = dY * i;
         entityMotionPosZ = dZ * i;
      }

      return new Vec3d(entityMotionPosX, entityMotionPosY, entityMotionPosZ);
   }

   public static float[] getLegitRotations(Entity entity) {
      Vec3d vec = entity.getEyePos();
      return getLegitRotations(vec);
   }

   public static float[] getLegitRotations(Vec3d vec) {
      Vec3d eyesPos = getEyesPos();
      double diffX = vec.x - eyesPos.x;
      double diffY = vec.y - eyesPos.y;
      double diffZ = vec.z - eyesPos.z;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new float[]{Globals.mc.player.getYaw() + MathHelper.wrapDegrees(yaw - Globals.mc.player.getYaw()), Globals.mc.player.getPitch() + MathHelper.wrapDegrees(pitch - Globals.mc.player.getPitch())};
   }

   public static int getDamagePercent(ItemStack stack) {
      return (int)((double)(stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0);
   }

   public static float getHealth(Entity entity) {
      if (entity instanceof LivingEntity e) {
         return e.getHealth() + e.getAbsorptionAmount();
      } else {
         return 0.0F;
      }
   }

   public static boolean canSee(BlockPos pos, Direction side) {
      Vec3d testVec = pos.toCenterPos().add((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5);
      HitResult result = Globals.mc.world.raycast(new RaycastContext(getEyesPos(), testVec, ShapeType.OUTLINE, FluidHandling.NONE, Globals.mc.player));
      return result == null || result.getType() == Type.MISS;
   }

   public static Vec3d getEyesPos() {
      return Globals.mc.player.getEyePos();
   }

   public static boolean isValid(Entity entity, double range) {
      boolean invalid = entity == null || !entity.isAlive() || entity.equals(Globals.mc.player) || entity instanceof PlayerEntity && Managers.SOCIAL.isFriend(entity.getName().getString()) || Globals.mc.player.squaredDistanceTo(entity) > MathUtil.square(range);
      return !invalid;
   }

   public static boolean isMonster(Entity e) {
      return e instanceof Monster;
   }

   public static boolean isNeutral(Entity e) {
      return e instanceof Angerable && !((Angerable)e).hasAngerTime();
   }

   public static boolean isPassive(Entity e) {
      return e instanceof PassiveEntity || e instanceof AmbientEntity || e instanceof SquidEntity;
   }

   public static boolean isVehicle(Entity e) {
      return e instanceof BoatEntity || e instanceof MinecartEntity || e instanceof FurnaceMinecartEntity || e instanceof ChestMinecartEntity;
   }

   public static void attackCrystal(BlockPos pos) {
      if (Modules.COMBAT_SETTING.attackTimer.passed(Modules.COMBAT_SETTING.attackDelay.getValue())) {
         List entities = Globals.mc.world.getOtherEntities(null, new Box(pos)).stream().filter((e) -> e instanceof EndCrystalEntity).toList();

          for (Object o : entities) {
              Entity entity = (Entity) o;
              Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, Globals.mc.player.isSneaking()));
              PlayerUtil.doSwing();
              Modules.COMBAT_SETTING.attackTimer.reset();
          }

      }
   }

   public static void attackCrystal(BlockPos pos, double delay) {
      if (Modules.COMBAT_SETTING.attackTimer.passed(delay)) {
         List entities = Globals.mc.world.getOtherEntities(null, new Box(pos)).stream().filter((e) -> e instanceof EndCrystalEntity).toList();

          for (Object o : entities) {
              Entity entity = (Entity) o;
              Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, Globals.mc.player.isSneaking()));
              PlayerUtil.doSwing();
              Modules.COMBAT_SETTING.attackTimer.reset();
          }

      }
   }

   public static void attackCrystal(Box box) {
      Iterator var1 = Globals.mc.world.getNonSpectatingEntities(EndCrystalEntity.class, box).iterator();
      if (var1.hasNext()) {
         EndCrystalEntity entity = (EndCrystalEntity)var1.next();
         attackCrystal(entity);
      }

   }

   public static void attackCrystal(Entity crystal) {
      if (Modules.COMBAT_SETTING.attackTimer.passed(Modules.COMBAT_SETTING.attackDelay.getValue())) {
         if (crystal != null) {
            Globals.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, Globals.mc.player.isSneaking()));
            PlayerUtil.doSwing();
            Modules.COMBAT_SETTING.attackTimer.reset();
         }

      }
   }

   public static void attackCrystal(double range) {
      if (Modules.COMBAT_SETTING.attackTimer.passed(Modules.COMBAT_SETTING.attackDelay.getValue())) {

          for (Entity entity : Globals.mc.world.getEntities()) {
              if (entity instanceof EndCrystalEntity && !((double) Globals.mc.player.distanceTo(entity) > range)) {
                  Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, Globals.mc.player.isSneaking()));
                  PlayerUtil.doSwing();
                  Modules.COMBAT_SETTING.attackTimer.reset();
              }
          }

      }
   }

   public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
      Iterator var2 = Globals.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)).iterator();

      Entity entity;
      do {
         do {
            do {
               do {
                  do {
                     do {
                        do {
                           if (!var2.hasNext()) {
                              return false;
                           }

                           entity = (Entity)var2.next();
                        } while(!entity.isAlive());
                     } while(entity instanceof ItemEntity);
                  } while(entity instanceof ExperienceOrbEntity);
               } while(entity instanceof ExperienceBottleEntity);
            } while(entity instanceof ArrowEntity);
         } while(ignoreCrystal && entity instanceof EndCrystalEntity);
      } while(entity instanceof ArmorStandEntity);

      return true;
   }

   public static boolean hasEntity(BlockPos pos) {
      Iterator var1 = Globals.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)).iterator();

      Entity entity;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         entity = (Entity)var1.next();
      } while(!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity);

      return true;
   }

   public static boolean hasCrystal(BlockPos pos) {
      Iterator var1 = Globals.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)).iterator();

      Entity entity;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         entity = (Entity)var1.next();
      } while(!entity.isAlive() || !(entity instanceof EndCrystalEntity));

      return true;
   }

   public static void sync() {
      Managers.NETWORK.sendPacket(new CloseHandledScreenC2SPacket(Globals.mc.player.currentScreenHandler.syncId));
   }
}
