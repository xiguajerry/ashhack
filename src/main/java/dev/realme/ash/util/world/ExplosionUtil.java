// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.util.world;

import com.google.common.collect.Multimap;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.world.ExplosionUtil;
import java.util.function.BiFunction;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;

public class ExplosionUtil implements Globals {
   public static double getDamageTo(Entity entity, Vec3d explosion) {
      return getDamageTo(entity, explosion, false);
   }

   public static double getDamageTo(Entity entity, Vec3d explosion, boolean ignoreTerrain) {
      return getDamageTo(entity, explosion, ignoreTerrain, 12.0F);
   }

   public static double getDamageTo(Entity entity, Vec3d explosion, boolean ignoreTerrain, float power) {
      double d = Math.sqrt(entity.squaredDistanceTo(explosion));
      double ab = getExposure(explosion, entity, ignoreTerrain);
      double w = d / (double)power;
      double ac = (1.0D - w) * ab;
      double dmg = (float)((int)((ac * ac + ac) / 2.0D * 7.0D * 12.0D + 1.0D));
      dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
      return Math.max(0.0D, dmg);
   }

   public static double getDamageToPos(Vec3d pos, Entity entity, Vec3d explosion, boolean ignoreTerrain) {
      Box bb = entity.getBoundingBox();
      double dx = pos.getX() - bb.minX;
      double dy = pos.getY() - bb.minY;
      double dz = pos.getZ() - bb.minZ;
      Box box = bb.offset(dx, dy, dz);
      double ab = getExposure(explosion, box, ignoreTerrain);
      double w = Math.sqrt(pos.squaredDistanceTo(explosion)) / 12.0D;
      double ac = (1.0D - w) * ab;
      double dmg = (float)((int)((ac * ac + ac) / 2.0D * 7.0D * 12.0D + 1.0D));
      dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
      return Math.max(0.0D, dmg);
   }

   private static double getReduction(Entity entity, DamageSource damageSource, double damage) {
      if (damageSource.isScaledWithDifficulty()) {
         switch(mc.world.getDifficulty()) {
            case EASY:
               damage = Math.min(damage / 2.0D + 1.0D, damage);
               break;
            case HARD:
               damage *= 1.5D;
         }
      }

      if (entity instanceof LivingEntity livingEntity) {
          damage = DamageUtil.getDamageLeft((float)damage, getArmor(livingEntity), (float)getAttributeValue(livingEntity, EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
         damage = getProtectionReduction(entity, damage, damageSource);
      }

      return Math.max(damage, 0.0D);
   }

   private static float getArmor(LivingEntity entity) {
      return (float)Math.floor(getAttributeValue(entity, EntityAttributes.GENERIC_ARMOR));
   }

   private static float getProtectionReduction(Entity player, double damage, DamageSource source) {
      int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), source);
      return DamageUtil.getInflictedDamage((float)damage, (float)protLevel);
   }

   public static double getAttributeValue(LivingEntity entity, EntityAttribute attribute) {
      return getAttributeInstance(entity, attribute).getValue();
   }

   public static EntityAttributeInstance getAttributeInstance(LivingEntity entity, EntityAttribute attribute) {
      double baseValue = getDefaultForEntity(entity).getBaseValue(attribute);
      EntityAttributeInstance attributeInstance = new EntityAttributeInstance(attribute, (o1) -> {
      });
      attributeInstance.setBaseValue(baseValue);

      for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
         ItemStack stack = entity.getEquippedStack(equipmentSlot);
         Multimap<EntityAttribute, EntityAttributeModifier> modifiers = stack.getAttributeModifiers(equipmentSlot);

         for(EntityAttributeModifier modifier : modifiers.get(attribute)) {
            attributeInstance.addTemporaryModifier(modifier);
         }
      }

      return attributeInstance;
   }

   private static <T extends LivingEntity> DefaultAttributeContainer getDefaultForEntity(T entity) {
      return DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) entity.getType());
   }

   private static float getExposure(Vec3d source, Entity entity, boolean ignoreTerrain) {
      Box box = entity.getBoundingBox();
      return getExposure(source, box, ignoreTerrain);
   }

   private static float getExposure(Vec3d source, Box box, boolean ignoreTerrain) {
      ExplosionUtil.RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain);
      double xDiff = box.maxX - box.minX;
      double yDiff = box.maxY - box.minY;
      double zDiff = box.maxZ - box.minZ;
      double xStep = 1.0D / (xDiff * 2.0D + 1.0D);
      double yStep = 1.0D / (yDiff * 2.0D + 1.0D);
      double zStep = 1.0D / (zDiff * 2.0D + 1.0D);
      if (xStep > 0.0D && yStep > 0.0D && zStep > 0.0D) {
         int misses = 0;
         int hits = 0;
         double xOffset = (1.0D - Math.floor(1.0D / xStep) * xStep) * 0.5D;
         double zOffset = (1.0D - Math.floor(1.0D / zStep) * zStep) * 0.5D;
         xStep *= xDiff;
         yStep *= yDiff;
         zStep *= zDiff;
         double startX = box.minX + xOffset;
         double startY = box.minY;
         double startZ = box.minZ + zOffset;
         double endX = box.maxX + xOffset;
         double endY = box.maxY;
         double endZ = box.maxZ + zOffset;

         for(double x = startX; x <= endX; x += xStep) {
            for(double y = startY; y <= endY; y += yStep) {
               for(double z = startZ; z <= endZ; z += zStep) {
                  Vec3d position = new Vec3d(x, y, z);
                  if (raycast(new ExplosionUtil.ExposureRaycastContext(position, source), raycastFactory) == null) {
                     ++misses;
                  }

                  ++hits;
               }
            }
         }

         return (float)misses / (float)hits;
      } else {
         return 0.0F;
      }
   }

   private static ExplosionUtil.RaycastFactory getRaycastFactory(boolean ignoreTerrain) {
      return ignoreTerrain ? (context, blockPos) -> {
         BlockState blockState = mc.world.getBlockState(blockPos);
         return blockState.getBlock().getBlastResistance() < 600.0F ? null : blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
      } : (context, blockPos) -> {
         BlockState blockState = mc.world.getBlockState(blockPos);
         return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
      };
   }

   private static BlockHitResult raycast(ExplosionUtil.ExposureRaycastContext context, ExplosionUtil.RaycastFactory raycastFactory) {
      return BlockView.raycast(context.start, context.end, context, raycastFactory, (ctx) -> null);
   }

   public record ExposureRaycastContext(Vec3d start, Vec3d end) {
   }

   @FunctionalInterface
   public interface RaycastFactory extends BiFunction<ExplosionUtil.ExposureRaycastContext, BlockPos, BlockHitResult> {
   }
}
 