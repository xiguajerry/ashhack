package dev.realme.ash.impl.event.gui.hud;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class PlayerListColumnsEvent extends Event {
   private int tabHeight;

   public void setTabHeight(int tabHeight) {
      this.tabHeight = tabHeight;
   }

   public int getTabHeight() {
      return this.tabHeight;
   }
}
