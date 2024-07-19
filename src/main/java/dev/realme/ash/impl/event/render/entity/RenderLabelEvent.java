package dev.realme.ash.impl.event.render.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.Entity;

@Cancelable
public class RenderLabelEvent extends Event {
   private final Entity entity;

   public RenderLabelEvent(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }
}
