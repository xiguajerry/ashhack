package dev.realme.ash.impl.event.world;

import dev.realme.ash.api.event.Event;
import net.minecraft.entity.Entity;

public final class UpdateCrosshairTargetEvent extends Event {
   private final float tickDelta;
   private final Entity cameraEntity;

   public UpdateCrosshairTargetEvent(float tickDelta, Entity cameraEntity) {
      this.tickDelta = tickDelta;
      this.cameraEntity = cameraEntity;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }

   public Entity getCameraEntity() {
      return this.cameraEntity;
   }
}
