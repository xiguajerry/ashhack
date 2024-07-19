package dev.realme.ash.impl.event.keyboard;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.StageEvent;
import net.minecraft.client.input.Input;

@Cancelable
public class KeyboardTickEvent extends StageEvent {
   private final Input input;

   public KeyboardTickEvent(Input input) {
      this.input = input;
   }

   public Input getInput() {
      return this.input;
   }
}
