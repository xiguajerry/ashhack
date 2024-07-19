package dev.realme.ash.impl.event.gui.hud;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class PlayerListEvent extends Event {
   private int size;

   public int getSize() {
      return this.size;
   }

   public void setSize(int size) {
      this.size = size;
   }
}
