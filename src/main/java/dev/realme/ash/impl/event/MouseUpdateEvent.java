package dev.realme.ash.impl.event;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class MouseUpdateEvent extends Event {
   private final double cursorDeltaX;
   private final double cursorDeltaY;

   public MouseUpdateEvent(double cursorDeltaX, double cursorDeltaY) {
      this.cursorDeltaX = cursorDeltaX;
      this.cursorDeltaY = cursorDeltaY;
   }

   public double getCursorDeltaX() {
      return this.cursorDeltaX;
   }

   public double getCursorDeltaY() {
      return this.cursorDeltaY;
   }
}
