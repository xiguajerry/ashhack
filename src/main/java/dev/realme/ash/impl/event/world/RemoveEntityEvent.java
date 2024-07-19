package dev.realme.ash.impl.event.world;

import dev.realme.ash.api.event.Event;
import dev.realme.ash.util.Globals;
import net.minecraft.entity.Entity;

public class RemoveEntityEvent extends Event implements Globals {
   private final Entity entity;
   private final Entity.RemovalReason removalReason;

   public RemoveEntityEvent(Entity entity, Entity.RemovalReason removalReason) {
      this.entity = entity;
      this.removalReason = removalReason;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public Entity.RemovalReason getRemovalReason() {
      return this.removalReason;
   }
}
