package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class EntityRotationVectorEvent extends Event {
   private final Entity entity;
   private final float tickDelta;
   private Vec3d position;

   public EntityRotationVectorEvent(float tickDelta, Entity entity, Vec3d position) {
      this.entity = entity;
      this.tickDelta = tickDelta;
      this.position = position;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }

   public Vec3d getPosition() {
      return this.position;
   }

   public void setPosition(Vec3d position) {
      this.position = position;
   }
}
