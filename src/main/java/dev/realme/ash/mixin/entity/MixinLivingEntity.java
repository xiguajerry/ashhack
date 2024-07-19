package dev.realme.ash.mixin.entity;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.entity.ConsumeItemEvent;
import dev.realme.ash.impl.event.entity.JumpDelayEvent;
import dev.realme.ash.impl.event.entity.JumpRotationEvent;
import dev.realme.ash.impl.event.entity.LevitationEvent;
import dev.realme.ash.impl.event.entity.StatusEffectEvent;
import dev.realme.ash.util.Globals;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {LivingEntity.class},
   priority = 10000
)
public abstract class MixinLivingEntity extends MixinEntity implements Globals {
   @Shadow
   protected ItemStack activeItemStack;
   @Shadow
   public int deathTime;
   @Shadow
   private int jumpingCooldown;

   @Shadow
   public abstract boolean hasStatusEffect(StatusEffect var1);

   @Shadow
   public abstract float getYaw(float var1);

   @Shadow
   protected abstract float getJumpVelocity();

   @Shadow
   public abstract boolean isDead();

   @Inject(
      method = {"jump"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookJump$getYaw(CallbackInfo ci) {
      if ((Object)this == mc.player) {
         JumpRotationEvent event = new JumpRotationEvent();
         Ash.EVENT_HANDLER.dispatch(event);
         if (event.isCanceled()) {
            ci.cancel();
            Vec3d vec3d = this.getVelocity();
            this.setVelocity(new Vec3d(vec3d.x, this.getJumpVelocity(), vec3d.z));
            if (this.isSprinting()) {
               float f = event.getYaw() * 0.017453292F;
               this.setVelocity(this.getVelocity().add(-MathHelper.sin(f) * 0.2F, 0.0, MathHelper.cos(f) * 0.2F));
            }

            this.velocityDirty = true;
         }

      }
   }

   @Redirect(
      method = {"travel"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"
)
   )
   private boolean hookHasStatusEffect(LivingEntity instance, StatusEffect effect) {
      if (!instance.equals(mc.player)) {
         return this.hasStatusEffect(effect);
      } else {
         LevitationEvent levitationEvent = new LevitationEvent();
         Ash.EVENT_HANDLER.dispatch(levitationEvent);
         return !levitationEvent.isCanceled() && this.hasStatusEffect(effect);
      }
   }

   @Inject(
      method = {"consumeItem"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;",
   shift = Shift.AFTER
)}
   )
   private void hookConsumeItem(CallbackInfo ci) {
      if ((Object)this == mc.player) {
         ConsumeItemEvent consumeItemEvent = new ConsumeItemEvent(this.activeItemStack);
         Ash.EVENT_HANDLER.dispatch(consumeItemEvent);
      }
   }

   @Inject(
      method = {"tickMovement"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookTickMovement(CallbackInfo ci) {
      JumpDelayEvent jumpDelayEvent = new JumpDelayEvent();
      Ash.EVENT_HANDLER.dispatch(jumpDelayEvent);
      if (jumpDelayEvent.isCanceled()) {
         this.jumpingCooldown = 0;
      }

   }

   @Inject(
      method = {"onStatusEffectApplied"},
      at = {@At("HEAD")}
   )
   private void hookAddStatusEffect(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
      if ((Object)this == mc.player) {
         StatusEffectEvent.Add statusEffectEvent = new StatusEffectEvent.Add(effect);
         Ash.EVENT_HANDLER.dispatch(statusEffectEvent);
      }
   }

   @Inject(
      method = {"onStatusEffectRemoved"},
      at = {@At("HEAD")}
   )
   private void hookRemoveStatusEffect(StatusEffectInstance effect, CallbackInfo ci) {
      if ((Object)this == mc.player) {
         StatusEffectEvent.Remove statusEffectEvent = new StatusEffectEvent.Remove(effect);
         Ash.EVENT_HANDLER.dispatch(statusEffectEvent);
      }
   }
}
