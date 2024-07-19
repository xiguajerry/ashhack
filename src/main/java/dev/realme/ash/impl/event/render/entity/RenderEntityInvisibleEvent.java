package dev.realme.ash.impl.event.render.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.LivingEntity;

@Cancelable
public class RenderEntityInvisibleEvent extends Event {
   private final LivingEntity entity;

   public RenderEntityInvisibleEvent(LivingEntity entity) {
      this.entity = entity;
   }

   public LivingEntity getEntity() {
      return this.entity;
   }
}
