package dev.realme.ash.mixin.entity.projectile;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.entity.projectile.RemoveFireworkEvent;
import dev.realme.ash.util.Globals;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FireworkRocketEntity.class})
public class MixinFireworkRocketEntity implements Globals {
   @Shadow
   private int life;

   @Inject(
      method = {"tick"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/projectile/FireworkRocketEntity;updateRotation()V",
   shift = Shift.AFTER
)},
      cancellable = true
   )
   private void hookTickPre(CallbackInfo ci) {
      FireworkRocketEntity rocketEntity = (FireworkRocketEntity)(Object)this;
      RemoveFireworkEvent removeFireworkEvent = new RemoveFireworkEvent(rocketEntity);
      Ash.EVENT_HANDLER.dispatch(removeFireworkEvent);
      if (removeFireworkEvent.isCanceled()) {
         ci.cancel();
         if (this.life == 0 && !rocketEntity.isSilent()) {
            mc.world.playSound((PlayerEntity)null, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0F, 1.0F);
         }

         ++this.life;
         if (mc.world.isClient && this.life % 2 < 2) {
            mc.world.addParticle(ParticleTypes.FIREWORK, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), mc.world.random.nextGaussian() * 0.05, -rocketEntity.getVelocity().y * 0.5, mc.world.random.nextGaussian() * 0.05);
         }
      }

   }
}
