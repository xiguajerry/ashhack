package dev.realme.ash.util.math;

import com.mojang.authlib.GameProfile;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.player.PlayerUtil;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PredictUtility {
   public static PlayerEntity predictPlayer(PlayerEntity entity, int ticks) {
      if (entity == null) {
         return null;
      } else {
         Vec3d posVec = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
         double motionX = entity.getX() - entity.prevX;
         double motionY = entity.getY() - entity.prevY;
         double motionZ = entity.getZ() - entity.prevZ;

         for(int i = 0; i < ticks; ++i) {
            if (!Globals.mc.world.isAir(BlockPos.ofFloored(posVec.add(0.0, motionY, 0.0)))) {
               motionY = 0.0;
            }

            if (!Globals.mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX, 0.0, 0.0))) || !Globals.mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX, 1.0, 0.0)))) {
               motionX = 0.0;
            }

            if (!Globals.mc.world.isAir(BlockPos.ofFloored(posVec.add(0.0, 0.0, motionZ))) || !Globals.mc.world.isAir(BlockPos.ofFloored(posVec.add(0.0, 1.0, motionZ)))) {
               motionZ = 0.0;
            }

            posVec = posVec.add(motionX, motionY, motionZ);
         }

         return equipAndReturn(entity, posVec);
      }
   }

   public static PlayerEntity equipAndReturn(PlayerEntity original, Vec3d posVec) {
      PlayerEntity copyEntity = new PlayerEntity(Globals.mc.world, PlayerUtil.playerPos(original).down(), original.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
         public boolean isSpectator() {
            return false;
         }

         public boolean isCreative() {
            return false;
         }
      };
      copyEntity.setPosition(posVec);
      copyEntity.setHealth(original.getHealth());
      copyEntity.prevX = original.prevX;
      copyEntity.prevZ = original.prevZ;
      copyEntity.prevY = original.prevY;
      copyEntity.getInventory().clone(original.getInventory());
      Iterator var3 = original.getStatusEffects().iterator();

      while(var3.hasNext()) {
         StatusEffectInstance se = (StatusEffectInstance)var3.next();
         copyEntity.addStatusEffect(se);
      }

      return copyEntity;
   }
}
