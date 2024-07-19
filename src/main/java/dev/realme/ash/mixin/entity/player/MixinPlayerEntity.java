package dev.realme.ash.mixin.entity.player;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.impl.event.entity.player.PlayerJumpEvent;
import dev.realme.ash.impl.event.entity.player.PushFluidsEvent;
import dev.realme.ash.impl.event.entity.player.TravelEvent;
import dev.realme.ash.util.Globals;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {PlayerEntity.class},
   priority = 10000
)
public abstract class MixinPlayerEntity extends LivingEntity implements Globals {
   protected MixinPlayerEntity(EntityType entityType, World world) {
      super(entityType, world);
   }

   @Inject(
      method = {"travel"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookTravelHead(Vec3d movementInput, CallbackInfo ci) {
      TravelEvent travelEvent = new TravelEvent();
      travelEvent.setStage(EventStage.PRE);
      Ash.EVENT_HANDLER.dispatch(travelEvent);
      if (travelEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"travel"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void hookTravelTail(Vec3d movementInput, CallbackInfo ci) {
      TravelEvent travelEvent = new TravelEvent();
      travelEvent.setStage(EventStage.POST);
      Ash.EVENT_HANDLER.dispatch(travelEvent);
      if (travelEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"isPushedByFluids"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookIsPushedByFluids(CallbackInfoReturnable cir) {
      if ((Object)this == mc.player) {
         PushFluidsEvent pushFluidsEvent = new PushFluidsEvent();
         Ash.EVENT_HANDLER.dispatch(pushFluidsEvent);
         if (pushFluidsEvent.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
         }

      }
   }

   @Inject(
      method = {"jump"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookJumpPre(CallbackInfo ci) {
      if ((Object)this == mc.player) {
         PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent();
         playerJumpEvent.setStage(EventStage.PRE);
         Ash.EVENT_HANDLER.dispatch(playerJumpEvent);
         if (playerJumpEvent.isCanceled()) {
            ci.cancel();
         }

      }
   }

   @Inject(
      method = {"jump"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void hookJumpPost(CallbackInfo ci) {
      if ((Object)this == mc.player) {
         PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent();
         playerJumpEvent.setStage(EventStage.POST);
         Ash.EVENT_HANDLER.dispatch(playerJumpEvent);
      }
   }
}
