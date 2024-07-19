// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.mixin.entity;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.camera.EntityCameraPositionEvent;
import dev.realme.ash.impl.event.entity.EntityGameEvent;
import dev.realme.ash.impl.event.entity.EntityRotationVectorEvent;
import dev.realme.ash.impl.event.entity.LookDirectionEvent;
import dev.realme.ash.impl.event.entity.SetBBEvent;
import dev.realme.ash.impl.event.entity.SlowMovementEvent;
import dev.realme.ash.impl.event.entity.UpdateVelocityEvent;
import dev.realme.ash.impl.event.entity.VelocityMultiplierEvent;
import dev.realme.ash.impl.event.entity.decoration.TeamColorEvent;
import dev.realme.ash.impl.event.entity.player.PushEntityEvent;
import dev.realme.ash.util.Globals;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        value = {Entity.class},
        priority = 10000
)
public abstract class MixinEntity implements Globals {
   @Shadow
   public boolean velocityDirty;

   @Shadow
   private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
      return null;
   }

   @Shadow
   public abstract Box getBoundingBox();

   @Shadow
   public abstract Vec3d getVelocity();

   @Shadow
   public abstract void setVelocity(Vec3d var1);

   @Shadow
   public abstract boolean isSprinting();

   @Inject(
           method = {"getRotationVec"},
           at = {@At("RETURN")},
           cancellable = true
   )
   public void hookGetCameraPosVec(float tickDelta, CallbackInfoReturnable<Vec3d> info) {
      EntityRotationVectorEvent event = new EntityRotationVectorEvent(tickDelta, (Entity)((Object) this), info.getReturnValue());
      Ash.EVENT_HANDLER.dispatch(event);
      info.setReturnValue(event.getPosition());
   }

   @Inject(
           method = {"adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"},
           at = {@At("HEAD")}
   )
   public void hookMove(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
      if ((Object) this == mc.player) {
      }
   }

   @Inject(
           method = {"slowMovement"},
           at = {@At("HEAD")},
           cancellable = true
   )
   private void hookSlowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci) {
      if ((Object) this == mc.player) {
         SlowMovementEvent slowMovementEvent = new SlowMovementEvent(state);
         Ash.EVENT_HANDLER.dispatch(slowMovementEvent);
         if (slowMovementEvent.isCanceled()) {
            ci.cancel();
         }

      }
   }

   @Redirect(
           method = {"getVelocityMultiplier"},
           at = @At(
                   value = "INVOKE",
                   target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/ block/Block;"
           )
   )
   private Block hookGetVelocityMultiplier(BlockState instance) {
      if (((Object) this) != mc.player) {
         return instance.getBlock();
      } else {
         VelocityMultiplierEvent velocityMultiplierEvent = new VelocityMultiplierEvent(instance);
         Ash.EVENT_HANDLER.dispatch(velocityMultiplierEvent);
         return velocityMultiplierEvent.isCanceled() ? Blocks.DIRT : instance.getBlock();
      }
   }

   @Inject(
           method = {"updateVelocity"},
           at = {@At("HEAD")},
           cancellable = true
   )
   private void hookUpdateVelocity(float speed, Vec3d movementInput, CallbackInfo ci) {
      if ((Object) this == mc.player) {
         UpdateVelocityEvent updateVelocityEvent = new UpdateVelocityEvent(movementInput, speed, mc.player.getYaw(), movementInputToVelocity(movementInput, speed, mc.player.getYaw()));
         Ash.EVENT_HANDLER.dispatch(updateVelocityEvent);
         if (updateVelocityEvent.isCanceled()) {
            ci.cancel();
            mc.player.setVelocity(mc.player.getVelocity().add(updateVelocityEvent.getVelocity()));
         }
      }

   }

   @Inject(
           method = {"pushAwayFrom"},
           at = {@At("HEAD")},
           cancellable = true
   )
   private void hookPushAwayFrom(Entity entity, CallbackInfo ci) {
      PushEntityEvent pushEntityEvent = new PushEntityEvent((Entity)((Object) this), entity);
      Ash.EVENT_HANDLER.dispatch(pushEntityEvent);
      if (pushEntityEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
           method = {"getTeamColorValue"},
           at = {@At("HEAD")},
           cancellable = true
   )
   private void hookGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
      TeamColorEvent teamColorEvent = new TeamColorEvent((Entity)((Object) this));
      Ash.EVENT_HANDLER.dispatch(teamColorEvent);
      if (teamColorEvent.isCanceled()) {
         cir.setReturnValue(teamColorEvent.getColor());
         cir.cancel();
      }

   }

   @Inject(
           method = {"changeLookDirection"},
           at = {@At("HEAD")},
           cancellable = true
   )
   private void hookChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
      if ((Object) this == mc.player) {
         LookDirectionEvent lookDirectionEvent = new LookDirectionEvent((Entity)((Object) this), cursorDeltaX, cursorDeltaY);
         Ash.EVENT_HANDLER.dispatch(lookDirectionEvent);
         if (lookDirectionEvent.isCanceled()) {
            ci.cancel();
         }
      }

   }

   @Inject(
           method = {"emitGameEvent(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/entity/Entity;)V"},
           at = {@At("HEAD")}
   )
   private void hookEmitGameEvent(GameEvent event, Entity entity, CallbackInfo ci) {
      EntityGameEvent entityGameEvent = new EntityGameEvent(event, entity);
      Ash.EVENT_HANDLER.dispatch(entityGameEvent);
   }

   @Inject(
           method = {"getCameraPosVec"},
           at = {@At("RETURN")},
           cancellable = true
   )
   public void hookCameraPositionVec(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
      EntityCameraPositionEvent cameraPositionEvent = new EntityCameraPositionEvent(cir.getReturnValue(),
              ((Entity) ((Object) this)), tickDelta);
      Ash.EVENT_HANDLER.dispatch(cameraPositionEvent);
      cir.setReturnValue(cameraPositionEvent.getPosition());
   }

   @Inject(
           method = {"setBoundingBox"},
           at = {@At("HEAD")}
   )
   private void hookSetBoundingBox(Box boundingBox, CallbackInfo ci) {
      if ((Object) this == mc.player) {
         SetBBEvent setBBEvent = new SetBBEvent(boundingBox);
         Ash.EVENT_HANDLER.dispatch(setBBEvent);
      }

   }
}
 