package dev.realme.ash.impl.event.entity.decoration;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.Entity;

@Cancelable
public class TeamColorEvent extends Event {
   private final Entity entity;
   private int color;

   public TeamColorEvent(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public int getColor() {
      return this.color;
   }

   public void setColor(int color) {
      this.color = color;
   }
}
