package dev.realme.ash.impl.event.keyboard;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class KeyboardInputEvent extends Event {
   private final int keycode;
   private final int action;

   public KeyboardInputEvent(int keycode, int action) {
      this.keycode = keycode;
      this.action = action;
   }

   public int getKeycode() {
      return this.keycode;
   }

   public int getAction() {
      return this.action;
   }
}
