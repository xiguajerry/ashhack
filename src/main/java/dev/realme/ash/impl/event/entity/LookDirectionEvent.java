package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.Entity;

@Cancelable
public class LookDirectionEvent extends Event {
   private final Entity entity;
   private final double cursorDeltaX;
   private final double cursorDeltaY;

   public LookDirectionEvent(Entity entity, double cursorDeltaX, double cursorDeltaY) {
      this.entity = entity;
      this.cursorDeltaX = cursorDeltaX;
      this.cursorDeltaY = cursorDeltaY;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public double getCursorDeltaX() {
      return this.cursorDeltaX;
   }

   public double getCursorDeltaY() {
      return this.cursorDeltaY;
   }
}
