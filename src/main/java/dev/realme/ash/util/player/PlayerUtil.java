package dev.realme.ash.util.player;

import dev.realme.ash.impl.module.client.CombatSettingModule;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.world.BlockPosX;
import dev.realme.ash.util.world.BlockUtil;
import java.util.Iterator;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class PlayerUtil implements Globals {
   public static void attackEntity(PlayerEntity player, boolean swing) {
      mc.interactionManager.attackEntity(mc.player, player);
      if (swing) {
         doSwing();
      }

   }

   public static void doSwing() {
      switch ((CombatSettingModule.SwingMode)Modules.COMBAT_SETTING.swingMode.getValue()) {
         case Normal -> mc.player.swingHand(Hand.MAIN_HAND);
         case Client -> mc.player.swingHand(Hand.MAIN_HAND, false);
         case Server -> Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
      }

   }

   public static void doSwing(Hand hand) {
      switch ((CombatSettingModule.SwingMode)Modules.COMBAT_SETTING.swingMode.getValue()) {
         case Normal -> mc.player.swingHand(hand);
         case Client -> mc.player.swingHand(hand, false);
         case Server -> Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
      }

   }

   public static boolean isTurtle(PlayerEntity player) {
      int duration = -1;
      Iterator var2 = player.getStatusEffects().iterator();

      while(var2.hasNext()) {
         StatusEffectInstance e = (StatusEffectInstance)var2.next();
         if (e.getEffectType().equals(StatusEffects.RESISTANCE) && e.getAmplifier() > 2) {
            duration = e.getDuration();
         }
      }

      return duration >= 0;
   }

   public static boolean isInWeb(PlayerEntity player) {
      Vec3d playerPos = player.getPos();
      float[] var2 = new float[]{0.0F, 0.3F, -0.3F};
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         float x = var2[var4];
         float[] var6 = new float[]{0.0F, 0.3F, -0.3F};
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            float z = var6[var8];
            float[] var10 = new float[]{0.0F, 1.0F, -1.0F};
            int var11 = var10.length;

            for(int var12 = 0; var12 < var11; ++var12) {
               float y = var10[var12];
               BlockPosX pos = new BlockPosX(playerPos.getX() + (double)x, playerPos.getY() + (double)y, playerPos.getZ() + (double)z);
               if (isTargetHere(pos, player) && BlockUtil.getBlock(pos) == Blocks.COBWEB) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean isInWeb(PlayerEntity player, boolean face, boolean feet) {
      Vec3d playerPos = player.getPos();
      float[] var4 = new float[]{0.0F, 0.3F, -0.3F};
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         float x = var4[var6];
         float[] var8 = new float[]{0.0F, 0.3F, -0.3F};
         int var9 = var8.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            float z = var8[var10];
            float[] var12 = new float[]{0.0F, 1.0F, -1.0F};
            int var13 = var12.length;

            for(int var14 = 0; var14 < var13; ++var14) {
               float y = var12[var14];
               if (!face) {
                  y = 0.0F;
               }

               if (!feet) {
                  x = 0.0F;
                  z = 0.0F;
               }

               BlockPosX pos = new BlockPosX(playerPos.getX() + (double)x, playerPos.getY() + (double)y, playerPos.getZ() + (double)z);
               if (isTargetHere(pos, player) && BlockUtil.getBlock(pos) == Blocks.COBWEB) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean isInWeb(PlayerEntity player, int min) {
      int n = 0;
      if (BlockUtil.getBlock(new BlockPosX(player.getX() + 0.3, player.getY() + 0.5, player.getZ() + 0.3)) == Blocks.COBWEB) {
         ++n;
      }

      if (BlockUtil.getBlock(new BlockPosX(player.getX() - 0.3, player.getY() + 0.5, player.getZ() - 0.3)) == Blocks.COBWEB) {
         ++n;
      }

      if (BlockUtil.getBlock(new BlockPosX(player.getX() - 0.3, player.getY() + 0.5, player.getZ() + 0.3)) == Blocks.COBWEB) {
         ++n;
      }

      if (BlockUtil.getBlock(new BlockPosX(player.getX() + 0.3, player.getY() + 0.5, player.getZ() - 0.3)) == Blocks.COBWEB) {
         ++n;
      }

      return n > min;
   }

   public static boolean isInBurrow(PlayerEntity player, int min) {
      int n = 0;
      if (BlockUtil.mineBlocks.contains(BlockUtil.getBlock(new BlockPosX(player.getX() + 0.3, player.getY() + 0.5, player.getZ() + 0.3)))) {
         ++n;
      }

      if (BlockUtil.mineBlocks.contains(BlockUtil.getBlock(new BlockPosX(player.getX() - 0.3, player.getY() + 0.5, player.getZ() - 0.3)))) {
         ++n;
      }

      if (BlockUtil.mineBlocks.contains(BlockUtil.getBlock(new BlockPosX(player.getX() - 0.3, player.getY() + 0.5, player.getZ() + 0.3)))) {
         ++n;
      }

      if (BlockUtil.mineBlocks.contains(BlockUtil.getBlock(new BlockPosX(player.getX() + 0.3, player.getY() + 0.5, player.getZ() - 0.3)))) {
         ++n;
      }

      return n > min;
   }

   public static boolean isInBurrow(PlayerEntity player) {
      Vec3d playerPos = player.getPos();
      float[] var2 = new float[]{0.0F, 0.3F, -0.3F};
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         float x = var2[var4];
         float[] var6 = new float[]{0.0F, 0.3F, -0.3F};
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            float z = var6[var8];
            BlockPosX pos = new BlockPosX(playerPos.getX() + (double)x, playerPos.getY(), playerPos.getZ() + (double)z);
            if (isTargetHere(pos, player) && BlockUtil.mineBlocks.contains(BlockUtil.getBlock(pos))) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isTargetHere(BlockPos pos, Entity target) {
      return (new Box(pos)).intersects(target.getBoundingBox());
   }

   public static boolean isHotbarKeysPressed() {
      KeyBinding[] var0 = mc.options.hotbarKeys;
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         KeyBinding binding = var0[var2];
         if (binding.isPressed()) {
            return true;
         }
      }

      return false;
   }

   public static BlockPos getRoundedBlockPos(double posY) {
      int flooredX = MathHelper.floor(mc.player.getX());
      int flooredY = (int)Math.round(posY);
      int flooredZ = MathHelper.floor(mc.player.getZ());
      return new BlockPos(flooredX, flooredY, flooredZ);
   }

   public static BlockPos getRoundedBlockPos(double x, double y, double z) {
      int flooredX = MathHelper.floor(x);
      int flooredY = (int)Math.round(y);
      int flooredZ = MathHelper.floor(z);
      return new BlockPos(flooredX, flooredY, flooredZ);
   }

   public static float getLocalPlayerHealth() {
      return mc.player.getHealth() + mc.player.getAbsorptionAmount();
   }

   public static int computeFallDamage(float fallDistance, float damageMultiplier) {
      if (mc.player.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
         return 0;
      } else {
         StatusEffectInstance statusEffectInstance = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST);
         float f = statusEffectInstance == null ? 0.0F : (float)(statusEffectInstance.getAmplifier() + 1);
         return MathHelper.ceil((fallDistance - 3.0F - f) * damageMultiplier);
      }
   }

   public static BlockPos playerPos(PlayerEntity targetEntity) {
      return new BlockPos((int)Math.floor(targetEntity.getX()), (int)Math.round(targetEntity.getY()), (int)Math.floor(targetEntity.getZ()));
   }

   public static boolean isInsideBlock(PlayerEntity player) {
      return BlockUtil.getBlock(playerPos(player)) == Blocks.ENDER_CHEST ? true : mc.world.canCollide(player, player.getBoundingBox());
   }

   public static boolean isInsideBlock() {
      return BlockUtil.getBlock(playerPos(mc.player)) == Blocks.ENDER_CHEST ? true : mc.world.canCollide(mc.player, mc.player.getBoundingBox());
   }

   public static boolean isHolding(Item item) {
      ItemStack itemStack = mc.player.getMainHandStack();
      if (!itemStack.isEmpty() && itemStack.getItem() == item) {
         return true;
      } else {
         itemStack = mc.player.getOffHandStack();
         return !itemStack.isEmpty() && itemStack.getItem() == item;
      }
   }
}
