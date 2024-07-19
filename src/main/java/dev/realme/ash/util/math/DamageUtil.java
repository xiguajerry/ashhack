package dev.realme.ash.util.math;

import com.mojang.authlib.GameProfile;
import dev.realme.ash.impl.imixin.IRaycastContext;
import dev.realme.ash.impl.imixin.IVec3d;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.IExplosion;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class DamageUtil implements Globals {
   public static boolean terrainIgnore = false;
   public static final float field_29962 = 20.0F;
   public static final float field_29963 = 25.0F;
   public static final float field_29964 = 2.0F;
   public static final float field_29965 = 0.2F;
   private static final int field_29966 = 4;
   public static RaycastContext raycastContext;
   private static final Vec3d vec3d;

   public static float getCrystalDamage(Vec3d crystalPos, PlayerEntity target) {
      if (Modules.COMBAT_SETTING.oldVersion.getValue()) {
         return oldVerCrystal(target, target.getBoundingBox(), crystalPos, null, false);
      } else {
         return Modules.AUTO_CRYSTAL.predictTicks.getValue() == 0 ? getExplosionDamage(crystalPos, target) : getExplosionDamageWPredict(crystalPos, target, PredictUtility.predictPlayer(target, Modules.AUTO_CRYSTAL.predictTicks.getValue()));
      }
   }

   public static float getAnchorDamage(BlockPos pos, PlayerEntity target) {
      PlayerEntity copyEntity = new PlayerEntity(mc.world, PlayerUtil.playerPos(target).down(), target.getYaw(), new GameProfile(UUID.fromString("66123666-6666-6666-6666-667563866600"), "PredictEntity")) {
         public boolean isSpectator() {
            return false;
         }

         public boolean isCreative() {
            return false;
         }
      };
      copyEntity.setPosition(EntityUtil.getEntityPosVec(target, Modules.AUTO_ANCHOR.predictTicks.getValue()));
      return calculateDamage(pos, pos.toCenterPos(), target, copyEntity, 5.0F);
   }

   public static float getCrystalDamageOfBase(Vec3d explosionPos, PlayerEntity target, BlockPos bp) {
      if (mc.world.getDifficulty() == Difficulty.PEACEFUL) {
         return 0.0F;
      } else {
         Explosion explosion = new Explosion(mc.world, mc.player, 1.0, 33.0, 7.0, 6.0F, false, DestructionType.DESTROY);
         ((IExplosion)explosion).setX(explosionPos.x);
         ((IExplosion)explosion).setY(explosionPos.y);
         ((IExplosion)explosion).setZ(explosionPos.z);
         if (((IExplosion)explosion).getWorld() != mc.world) {
            ((IExplosion)explosion).setWorld(mc.world);
         }

         if (!(new Box(MathHelper.floor(explosionPos.x - 11.0), MathHelper.floor(explosionPos.y - 11.0), MathHelper.floor(explosionPos.z - 11.0), MathHelper.floor(explosionPos.x + 13.0), MathHelper.floor(explosionPos.y + 13.0), MathHelper.floor(explosionPos.z + 13.0))).intersects(target.getBoundingBox())) {
            return 0.0F;
         } else {
            if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
               double distExposure = (float)target.squaredDistanceTo(explosionPos) / 144.0F;
               if (distExposure <= 1.0) {
                  double exposure = getExposureGhost(explosionPos, target, bp);
                  double finalExposure = (1.0 - distExposure) * exposure;
                  float toDamage = (float)Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12.0 + 1.0);
                  if (mc.world.getDifficulty() == Difficulty.EASY) {
                     toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                  } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                     toDamage = toDamage * 3.0F / 2.0F;
                  }

                  toDamage = getDamageLeft(toDamage, (float)target.getArmor(), (float)target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
                  int protAmount;
                  if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                     protAmount = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                     float resistance_1 = toDamage * (float)protAmount;
                     toDamage = Math.max(resistance_1 / 25.0F, 0.0F);
                  }

                  if (toDamage <= 0.0F) {
                     toDamage = 0.0F;
                  } else {
                     protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                     if (protAmount > 0) {
                        toDamage = getInflictedDamage(toDamage, (float)protAmount);
                     }
                  }

                  return toDamage;
               }
            }

            return 0.0F;
         }
      }
   }

   private static float getExposureGhost(Vec3d source, Entity entity, BlockPos pos) {
      Box box = entity.getBoundingBox();
      double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
      double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
      double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
      double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
      double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
      if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
         int i = 0;
         int j = 0;

         for(double k = 0.0; k <= 1.0; k += d) {
            for(double l = 0.0; l <= 1.0; l += e) {
               for(double m = 0.0; m <= 1.0; m += f) {
                  double n = MathHelper.lerp(k, box.minX, box.maxX);
                  double o = MathHelper.lerp(l, box.minY, box.maxY);
                  double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                  Vec3d vec3d = new Vec3d(n + g, o, p + h);
                  if (raycastGhost(new RaycastContext(vec3d, source, ShapeType.COLLIDER, FluidHandling.NONE, entity), pos).getType() == Type.MISS) {
                     ++i;
                  }

                  ++j;
               }
            }
         }

         return (float)i / (float)j;
      } else {
         return 0.0F;
      }
   }

   private static BlockHitResult raycastGhost(RaycastContext context, BlockPos bPos) {
      return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
         Vec3d vec3d = innerContext.getStart();
         Vec3d vec3d2 = innerContext.getEnd();
         BlockState blockState;
         if (!pos.equals(bPos)) {
            blockState = mc.world.getBlockState(pos);
         } else {
            blockState = Blocks.OBSIDIAN.getDefaultState();
         }

         VoxelShape voxelShape = innerContext.getBlockShape(blockState, mc.world, pos);
         BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, pos, voxelShape, blockState);
         BlockHitResult blockHitResult2 = VoxelShapes.empty().raycast(vec3d, vec3d2, pos);
         double d = blockHitResult == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult.getPos());
         double e = blockHitResult2 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
         return d <= e ? blockHitResult : blockHitResult2;
      }, (innerContext) -> {
         Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
         return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()));
      });
   }

   public static float calculateDamage(BlockPos pos, Vec3d explosionPos, PlayerEntity target, PlayerEntity predict, float power) {
      if (mc.world.getDifficulty() == Difficulty.PEACEFUL) {
         return 0.0F;
      } else {
         Explosion explosion = new Explosion(mc.world, null, explosionPos.x, explosionPos.y, explosionPos.z, 6.0F, false, DestructionType.DESTROY);
         ((IExplosion)explosion).setWorld(mc.world);
         ((IExplosion)explosion).setX(explosionPos.x);
         ((IExplosion)explosion).setY(explosionPos.y);
         ((IExplosion)explosion).setZ(explosionPos.z);
         ((IExplosion)explosion).setPower(power);
         if (!(new Box(MathHelper.floor(explosionPos.x - 11.0), MathHelper.floor(explosionPos.y - 11.0), MathHelper.floor(explosionPos.z - 11.0), MathHelper.floor(explosionPos.x + 13.0), MathHelper.floor(explosionPos.y + 13.0), MathHelper.floor(explosionPos.z + 13.0))).intersects(predict.getBoundingBox())) {
            return 0.0F;
         } else {
            if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
               double distExposure = (double)MathHelper.sqrt((float)predict.squaredDistanceTo(explosionPos)) / 12.0;
               if (distExposure <= 1.0) {
                  double xDiff = predict.getX() - explosionPos.x;
                  double yDiff = predict.getY() - explosionPos.y;
                  double zDiff = predict.getX() - explosionPos.z;
                  double diff = MathHelper.sqrt((float)(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                  if (diff != 0.0) {
                     double exposure = getExposure(explosionPos, target, target.getBoundingBox(), raycastContext, pos, true);
                     double finalExposure = (1.0 - distExposure) * exposure;
                     float toDamage = (float)Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12.0 + 1.0);
                     if (mc.world.getDifficulty() == Difficulty.EASY) {
                        toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                     } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                        toDamage = toDamage * 3.0F / 2.0F;
                     }

                     toDamage = net.minecraft.entity.DamageUtil.getDamageLeft(toDamage, (float)target.getArmor(), (float) Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue());
                     int protAmount;
                     if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        protAmount = 25 - (Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                        float resistance_1 = toDamage * (float)protAmount;
                        toDamage = Math.max(resistance_1 / 25.0F, 0.0F);
                     }

                     if (toDamage <= 0.0F) {
                        toDamage = 0.0F;
                     } else {
                        protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                        if (protAmount > 0) {
                           toDamage = net.minecraft.entity.DamageUtil.getInflictedDamage(toDamage, (float)protAmount);
                        }
                     }

                     return toDamage;
                  }
               }
            }

            return 0.0F;
         }
      }
   }

   public static float oldVerCrystal(PlayerEntity player, Box bb, Vec3d crystal, BlockPos ignore, boolean ignoreTerrain) {
      ((IVec3d)vec3d).set((bb.minX + bb.maxX) / 2.0, bb.minY, (bb.minZ + bb.maxZ) / 2.0);
      double dist = vec3d.distanceTo(crystal) / 12.0;
      if (dist > 1.0) {
         return 0.0F;
      } else {
         double exposure = getExposure(crystal, player, bb, raycastContext, ignore, ignoreTerrain);
         double d10 = (1.0 - dist) * exposure;
         float damage = (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * 12.0 + 1.0));
         damage = (float)getDamageForDifficulty(damage);
         damage = getDamageAfterAbsorb(damage, (float)player.getArmor(), (float)player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
         damage = oldVerPotionReduce(player, damage);
         return damage;
      }
   }

   private static float oldVerPotionReduce(LivingEntity livingEntity, float damage) {
      int k;
      if (livingEntity.hasStatusEffect(StatusEffects.RESISTANCE)) {
         k = (livingEntity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
         int j = 25 - k;
         float f = damage * (float)j;
         damage = f / 25.0F;
      }

      k = getEnchantmentModifierDamage(livingEntity.getArmorItems());
      if (k > 0) {
         damage = getDamageAfterMagicAbsorb(damage, (float)k);
      }

      return damage;
   }

   private static int getEnchantmentModifierDamage(Iterable stacks) {
      int i = 0;

      ItemStack stack;
      for(Iterator var2 = stacks.iterator(); var2.hasNext(); i += sus(stack)) {
         stack = (ItemStack)var2.next();
      }

      return i;
   }

   private static float getDamageAfterMagicAbsorb(float damage, float enchantModifiers) {
      return getInflictedDamage(damage, enchantModifiers);
   }

   private static int sus(ItemStack stack) {
      int r = 0;
      if (!stack.isEmpty()) {
         NbtList nbttaglist = stack.getEnchantments();

         for(int i = 0; i < nbttaglist.size(); ++i) {
            int j = nbttaglist.getCompound(i).getShort("id");
            int k = nbttaglist.getCompound(i).getShort("lvl") + 1;
            Enchantment e = Enchantment.byRawId(j);
            if (e != null) {
               if (e == Enchantments.BLAST_PROTECTION) {
                  r += k * 2;
               } else if (e == Enchantments.PROTECTION) {
                  r += k;
               }
            }
         }
      }

      return r;
   }

   public static float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute) {
      return getDamageLeft(damage, totalArmor, toughnessAttribute);
   }

   private static double getDamageForDifficulty(double damage) {
      double var10000;
      switch (mc.world.getDifficulty()) {
         case EASY:
            var10000 = Math.min(damage / 2.0 + 1.0, damage);
            break;
         case HARD:
         case PEACEFUL:
            var10000 = damage * 3.0 / 2.0;
            break;
         default:
            var10000 = damage;
      }

      return var10000;
   }

   public static float getDamageLeft(float damage, float armor, float armorToughness) {
      float f = 2.0F + armorToughness / 4.0F;
      float g = MathHelper.clamp(armor - damage / f, armor * 0.2F, 20.0F);
      return damage * (1.0F - g / 25.0F);
   }

   public static float getInflictedDamage(float damageDealt, float protection) {
      float f = MathHelper.clamp(protection, 0.0F, 20.0F);
      return damageDealt * (1.0F - f / 25.0F);
   }

   public static float getExplosionDamage(Vec3d explosionPos, PlayerEntity target) {
      if (mc.world.getDifficulty() == Difficulty.PEACEFUL) {
         return 0.0F;
      } else {
         Explosion explosion = new Explosion(mc.world, mc.player, 1.0, 33.0, 7.0, 6.0F, false, DestructionType.DESTROY);
         ((IExplosion)explosion).setX(explosionPos.x);
         ((IExplosion)explosion).setY(explosionPos.y);
         ((IExplosion)explosion).setZ(explosionPos.z);
         if (((IExplosion)explosion).getWorld() != mc.world) {
            ((IExplosion)explosion).setWorld(mc.world);
         }

         if (!(new Box(MathHelper.floor(explosionPos.x - 11.0), MathHelper.floor(explosionPos.y - 11.0), MathHelper.floor(explosionPos.z - 11.0), MathHelper.floor(explosionPos.x + 13.0), MathHelper.floor(explosionPos.y + 13.0), MathHelper.floor(explosionPos.z + 13.0))).intersects(target.getBoundingBox())) {
            return 0.0F;
         } else {
            if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
               double distExposure = (float)target.squaredDistanceTo(explosionPos) / 144.0F;
               if (distExposure <= 1.0) {
                  double exposure = getExposure(explosionPos, target);
                  double finalExposure = (1.0 - distExposure) * exposure;
                  float toDamage = (float)Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12.0 + 1.0);
                  if (mc.world.getDifficulty() == Difficulty.EASY) {
                     toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                  } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                     toDamage = toDamage * 3.0F / 2.0F;
                  }

                  toDamage = getDamageLeft(toDamage, (float)target.getArmor(), (float)target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
                  int protAmount;
                  if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                     protAmount = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                     float resistance_1 = toDamage * (float)protAmount;
                     toDamage = Math.max(resistance_1 / 25.0F, 0.0F);
                  }

                  if (toDamage <= 0.0F) {
                     toDamage = 0.0F;
                  } else {
                     protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                     if (protAmount > 0) {
                        toDamage = getInflictedDamage(toDamage, (float)protAmount);
                     }
                  }

                  return toDamage;
               }
            }

            return 0.0F;
         }
      }
   }

   public static float getExplosionDamageWPredict(Vec3d explosionPos, PlayerEntity target, PlayerEntity predict) {
      if (mc.world.getDifficulty() == Difficulty.PEACEFUL) {
         return 0.0F;
      } else if (target != null && predict != null) {
         Explosion explosion = new Explosion(mc.world, mc.player, 1.0, 33.0, 7.0, 6.0F, false, DestructionType.DESTROY);
         ((IExplosion)explosion).setX(explosionPos.x);
         ((IExplosion)explosion).setY(explosionPos.y);
         ((IExplosion)explosion).setZ(explosionPos.z);
         if (((IExplosion)explosion).getWorld() != mc.world) {
            ((IExplosion)explosion).setWorld(mc.world);
         }

         if (!(new Box(MathHelper.floor(explosionPos.x - 11.0), MathHelper.floor(explosionPos.y - 11.0), MathHelper.floor(explosionPos.z - 11.0), MathHelper.floor(explosionPos.x + 13.0), MathHelper.floor(explosionPos.y + 13.0), MathHelper.floor(explosionPos.z + 13.0))).intersects(predict.getBoundingBox())) {
            return 0.0F;
         } else {
            if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
               double distExposure = (double)MathHelper.sqrt((float)predict.squaredDistanceTo(explosionPos)) / 12.0;
               if (distExposure <= 1.0) {
                  double exposure = getExposure(explosionPos, predict);
                  double finalExposure = (1.0 - distExposure) * exposure;
                  float toDamage = (float)Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12.0 + 1.0);
                  if (mc.world.getDifficulty() == Difficulty.EASY) {
                     toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                  } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                     toDamage = toDamage * 3.0F / 2.0F;
                  }

                  toDamage = getDamageLeft(toDamage, (float)target.getArmor(), (float) Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue());
                  int protAmount;
                  if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                     protAmount = 25 - (Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                     float resistance_1 = toDamage * (float)protAmount;
                     toDamage = Math.max(resistance_1 / 25.0F, 0.0F);
                  }

                  if (toDamage <= 0.0F) {
                     toDamage = 0.0F;
                  } else {
                     protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                     if (protAmount > 0) {
                        toDamage = getInflictedDamage(toDamage, (float)protAmount);
                     }
                  }

                  return toDamage;
               }
            }

            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   public static float getExplosionDamageWPredict(Vec3d explosionPos, PlayerEntity target, PlayerEntity predict, BlockPos pos) {
      if (mc.world.getDifficulty() == Difficulty.PEACEFUL) {
         return 0.0F;
      } else {
         Explosion explosion = new Explosion(mc.world, null, explosionPos.x, explosionPos.y, explosionPos.z, 6.0F, false, DestructionType.DESTROY);
         if (!(new Box(MathHelper.floor(explosionPos.x - 11.0), MathHelper.floor(explosionPos.y - 11.0), MathHelper.floor(explosionPos.z - 11.0), MathHelper.floor(explosionPos.x + 13.0), MathHelper.floor(explosionPos.y + 13.0), MathHelper.floor(explosionPos.z + 13.0))).intersects(predict.getBoundingBox())) {
            return 0.0F;
         } else {
            if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
               double distExposure = (double)MathHelper.sqrt((float)predict.squaredDistanceTo(explosionPos)) / 12.0;
               if (distExposure <= 1.0) {
                  double xDiff = predict.getX() - explosionPos.x;
                  double yDiff = predict.getY() - explosionPos.y;
                  double zDiff = predict.getX() - explosionPos.z;
                  double diff = MathHelper.sqrt((float)(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                  if (diff != 0.0) {
                     double exposure = getExposure(explosionPos, predict, target.getBoundingBox(), raycastContext, pos, true);
                     double finalExposure = (1.0 - distExposure) * exposure;
                     float toDamage = (float)Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12.0 + 1.0);
                     if (mc.world.getDifficulty() == Difficulty.EASY) {
                        toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                     } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                        toDamage = toDamage * 3.0F / 2.0F;
                     }

                     toDamage = getDamageLeft(toDamage, (float)target.getArmor(), (float) Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue());
                     int protAmount;
                     if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        protAmount = 25 - (Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                        float resistance_1 = toDamage * (float)protAmount;
                        toDamage = Math.max(resistance_1 / 25.0F, 0.0F);
                     }

                     if (toDamage <= 0.0F) {
                        toDamage = 0.0F;
                     } else {
                        protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                        if (protAmount > 0) {
                           toDamage = getInflictedDamage(toDamage, (float)protAmount);
                        }
                     }

                     return toDamage;
                  }
               }
            }

            return 0.0F;
         }
      }
   }

   public static float getExplosionDamage1(Vec3d explosionPos, PlayerEntity target, BlockPos pos) {
      try {
         if (mc.world.getDifficulty() == Difficulty.PEACEFUL) {
            return 0.0F;
         }

         Explosion explosion = new Explosion(mc.world, null, explosionPos.x, explosionPos.y, explosionPos.z, 6.0F, false, DestructionType.DESTROY);
         double maxDist = 12.0;
         if (!(new Box(MathHelper.floor(explosionPos.x - maxDist - 1.0), MathHelper.floor(explosionPos.y - maxDist - 1.0), MathHelper.floor(explosionPos.z - maxDist - 1.0), MathHelper.floor(explosionPos.x + maxDist + 1.0), MathHelper.floor(explosionPos.y + maxDist + 1.0), MathHelper.floor(explosionPos.z + maxDist + 1.0))).intersects(target.getBoundingBox())) {
            return 0.0F;
         }

         if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
            double distExposure = (double)MathHelper.sqrt((float)target.squaredDistanceTo(explosionPos)) / maxDist;
            if (distExposure <= 1.0) {
               double xDiff = target.getX() - explosionPos.x;
               double yDiff = target.getY() - explosionPos.y;
               double zDiff = target.getX() - explosionPos.z;
               double diff = MathHelper.sqrt((float)(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
               if (diff != 0.0) {
                  double exposure = getExposure(explosionPos, target, target.getBoundingBox(), raycastContext, pos, true);
                  double finalExposure = (1.0 - distExposure) * exposure;
                  float toDamage = (float)Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * maxDist + 1.0);
                  if (mc.world.getDifficulty() == Difficulty.EASY) {
                     toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                  } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                     toDamage = toDamage * 3.0F / 2.0F;
                  }

                  toDamage = getDamageLeft(toDamage, (float)target.getArmor(), (float)target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
                  int protAmount;
                  if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                     protAmount = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                     float resistance_1 = toDamage * (float)protAmount;
                     toDamage = Math.max(resistance_1 / 25.0F, 0.0F);
                  }

                  if (toDamage <= 0.0F) {
                     toDamage = 0.0F;
                  } else {
                     protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                     if (protAmount > 0) {
                        toDamage = getInflictedDamage(toDamage, (float)protAmount);
                     }
                  }

                  return toDamage;
               }
            }
         }
      } catch (Exception var23) {
      }

      return 0.0F;
   }

   public static float getExposure(Vec3d source, Entity entity) {
      if (!(Boolean)Modules.AUTO_CRYSTAL.useOptimizedCalc.getValue()) {
         return Explosion.getExposure(source, entity);
      } else {
         Box box = entity.getBoundingBox();
         int miss = 0;
         int hit = 0;

         for(int k = 0; k <= 1; ++k) {
            for(int l = 0; l <= 1; ++l) {
               for(int m = 0; m <= 1; ++m) {
                  double n = MathHelper.lerp(k, box.minX, box.maxX);
                  double o = MathHelper.lerp(l, box.minY, box.maxY);
                  double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                  Vec3d vec3d = new Vec3d(n, o, p);
                  if (raycast(vec3d, source) == Type.MISS) {
                     ++miss;
                  }

                  ++hit;
               }
            }
         }

         return (float)miss / (float)hit;
      }
   }

   public static double getExposure(Vec3d source, Entity entity, Box box, RaycastContext raycastContext, BlockPos ignore, boolean ignoreTerrain) {
      double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
      double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
      double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
      double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
      double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
      if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
         int i = 0;
         int j = 0;

         for(double k = 0.0; k <= 1.0; k += d) {
            for(double l = 0.0; l <= 1.0; l += e) {
               for(double m = 0.0; m <= 1.0; m += f) {
                  double n = MathHelper.lerp(k, box.minX, box.maxX);
                  double o = MathHelper.lerp(l, box.minY, box.maxY);
                  double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                  ((IVec3d)vec3d).set(n + g, o, p + h);
                  ((IRaycastContext)raycastContext).set(vec3d, source, ShapeType.COLLIDER, FluidHandling.NONE, entity);
                  if (raycast(raycastContext, ignore, ignoreTerrain).getType() == Type.MISS) {
                     ++i;
                  }

                  ++j;
               }
            }
         }

         return (double)i / (double)j;
      } else {
         return 0.0;
      }
   }

   private static HitResult.Type raycast(Vec3d start, Vec3d end) {
      return BlockView.raycast(start, end, null, (_null, blockPos) -> {
         BlockState blockState = mc.world.getBlockState(blockPos);
         if (blockState.getBlock().getBlastResistance() < 600.0F) {
            return null;
         } else {
            BlockHitResult hitResult = blockState.getCollisionShape(mc.world, blockPos).raycast(start, end, blockPos);
            return hitResult == null ? null : hitResult.getType();
         }
      }, (_null) -> {
         return Type.MISS;
      });
   }

   private static BlockHitResult raycast(RaycastContext context, BlockPos ignore, boolean ignoreTerrain) {
      return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
         BlockState blockState;
         if (blockPos.equals(ignore)) {
            blockState = Blocks.AIR.getDefaultState();
         } else {
            blockState = mc.world.getBlockState(blockPos);
            if (blockState.getBlock().getBlastResistance() < 600.0F && ignoreTerrain) {
               blockState = Blocks.AIR.getDefaultState();
            }
         }

         Vec3d vec3d = raycastContext.getStart();
         Vec3d vec3d2 = raycastContext.getEnd();
         VoxelShape voxelShape = raycastContext.getBlockShape(blockState, mc.world, blockPos);
         BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
         VoxelShape voxelShape2 = VoxelShapes.empty();
         BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);
         double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
         double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
         return d <= e ? blockHitResult : blockHitResult2;
      }, (raycastContext) -> {
         Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
         return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(raycastContext.getEnd()));
      });
   }

   static {
      raycastContext = new RaycastContext(null, null, ShapeType.COLLIDER, FluidHandling.ANY, mc.player);
      vec3d = new Vec3d(0.0, 0.0, 0.0);
   }
}
