package dev.realme.ash.impl.event.network;

import dev.realme.ash.api.event.Event;
import net.minecraft.client.input.Input;

public class MovementSlowdownEvent extends Event {
   public final Input input;

   public MovementSlowdownEvent(Input input) {
      this.input = input;
   }

   public Input getInput() {
      return this.input;
   }
}
