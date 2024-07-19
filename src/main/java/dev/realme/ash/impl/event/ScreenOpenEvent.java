package dev.realme.ash.impl.event;

import dev.realme.ash.api.event.Event;
import net.minecraft.client.gui.screen.Screen;

public class ScreenOpenEvent extends Event {
   private final Screen screen;

   public ScreenOpenEvent(Screen screen) {
      this.screen = screen;
   }

   public Screen getScreen() {
      return this.screen;
   }
}
