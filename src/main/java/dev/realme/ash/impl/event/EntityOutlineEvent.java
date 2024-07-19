package dev.realme.ash.impl.event;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.Entity;

@Cancelable
public class EntityOutlineEvent extends Event {
   private final Entity entity;

   public EntityOutlineEvent(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }
}
