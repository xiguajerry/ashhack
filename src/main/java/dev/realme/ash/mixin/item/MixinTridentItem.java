package dev.realme.ash.mixin.item;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.item.TridentPullbackEvent;
import dev.realme.ash.impl.event.item.TridentWaterEvent;
import dev.realme.ash.util.Globals;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({TridentItem.class})
public abstract class MixinTridentItem implements Globals {
   @Shadow
   public abstract int getMaxUseTime(ItemStack var1);

   @Inject(
      method = {"use"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable cir) {
      TridentWaterEvent tridentWaterEvent = new TridentWaterEvent();
      Ash.EVENT_HANDLER.dispatch(tridentWaterEvent);
      if (tridentWaterEvent.isCanceled()) {
         cir.cancel();
         ItemStack itemStack = user.getStackInHand(hand);
         if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
            cir.setReturnValue(TypedActionResult.fail(itemStack));
            return;
         }

         user.setCurrentHand(hand);
         cir.setReturnValue(TypedActionResult.consume(itemStack));
      }

   }

   @Inject(
      method = {"onStoppedUsing"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookOnStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
      if (user instanceof PlayerEntity) {
         int i = this.getMaxUseTime(stack) - remainingUseTicks;
         TridentPullbackEvent tridentPullbackEvent = new TridentPullbackEvent();
         Ash.EVENT_HANDLER.dispatch(tridentPullbackEvent);
         if (tridentPullbackEvent.isCanceled() || i >= 10) {
            TridentWaterEvent tridentWaterEvent = new TridentWaterEvent();
            Ash.EVENT_HANDLER.dispatch(tridentWaterEvent);
            if (tridentWaterEvent.isCanceled()) {
               ci.cancel();
               PlayerEntity playerEntity = (PlayerEntity)user;
               int j = EnchantmentHelper.getRiptide(stack);
               if (!mc.world.isClient) {
                  stack.damage(1, playerEntity, (p) -> {
                     p.sendToolBreakStatus(user.getActiveHand());
                  });
                  if (j == 0) {
                     TridentEntity tridentEntity = new TridentEntity(world, playerEntity, stack);
                     tridentEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 2.5F + (float)j * 0.5F, 1.0F);
                     if (playerEntity.getAbilities().creativeMode) {
                        tridentEntity.pickupType = PickupPermission.CREATIVE_ONLY;
                     }

                     world.spawnEntity(tridentEntity);
                     world.playSoundFromEntity(null, tridentEntity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                     if (!playerEntity.getAbilities().creativeMode) {
                        playerEntity.getInventory().removeOne(stack);
                     }
                  }
               }

               playerEntity.incrementStat(Stats.USED.getOrCreateStat((TridentItem)(Object)this));
               if (j > 0) {
                  float f = playerEntity.getYaw();
                  float g = playerEntity.getPitch();
                  float h = -MathHelper.sin(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                  float k = -MathHelper.sin(g * 0.017453292F);
                  float l = MathHelper.cos(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                  float m = MathHelper.sqrt(h * h + k * k + l * l);
                  float n = 3.0F * ((1.0F + (float)j) / 4.0F);
                  playerEntity.addVelocity(h * (n / m), k * (n / m), l * (n / m));
                  playerEntity.useRiptide(20);
                  if (playerEntity.isOnGround()) {
                     float o = 1.1999999F;
                     playerEntity.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));
                  }

                  SoundEvent soundEvent = j >= 3 ? SoundEvents.ITEM_TRIDENT_RIPTIDE_3 : (j == 2 ? SoundEvents.ITEM_TRIDENT_RIPTIDE_2 : SoundEvents.ITEM_TRIDENT_RIPTIDE_1);
                  world.playSoundFromEntity(null, playerEntity, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
               }
            }

         }
      }
   }
}
