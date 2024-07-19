package dev.realme.ash.mixin.world;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.world.AddEntityEvent;
import dev.realme.ash.impl.event.world.RemoveEntityEvent;
import dev.realme.ash.impl.event.world.SkyboxEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientWorld.class})
public abstract class MixinClientWorld {
   @Shadow
   public abstract @Nullable Entity getEntityById(int var1);

   @Inject(
      method = {"addEntity"},
      at = {@At("HEAD")}
   )
   private void hookAddEntity(Entity entity, CallbackInfo ci) {
      AddEntityEvent addEntityEvent = new AddEntityEvent(entity);
      Ash.EVENT_HANDLER.dispatch(addEntityEvent);
   }

   @Inject(
      method = {"removeEntity"},
      at = {@At("HEAD")}
   )
   private void hookRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
      Entity entity = this.getEntityById(entityId);
      if (entity != null) {
         RemoveEntityEvent addEntityEvent = new RemoveEntityEvent(entity, removalReason);
         Ash.EVENT_HANDLER.dispatch(addEntityEvent);
      }
   }

   @Inject(
      method = {"getSkyColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable cir) {
      SkyboxEvent.Sky skyboxEvent = new SkyboxEvent.Sky();
      Ash.EVENT_HANDLER.dispatch(skyboxEvent);
      if (skyboxEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(skyboxEvent.getColorVec());
      }

   }

   @Inject(
      method = {"getCloudsColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetCloudsColor(float tickDelta, CallbackInfoReturnable cir) {
      SkyboxEvent.Cloud skyboxEvent = new SkyboxEvent.Cloud();
      Ash.EVENT_HANDLER.dispatch(skyboxEvent);
      if (skyboxEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(skyboxEvent.getColorVec());
      }

   }
}
