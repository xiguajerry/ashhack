package dev.realme.ash.impl.event;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class MouseClickEvent extends Event {
   private final int button;
   private final int action;

   public MouseClickEvent(int button, int action) {
      this.button = button;
      this.action = action;
   }

   public int getButton() {
      return this.button;
   }

   public int getAction() {
      return this.action;
   }
}
