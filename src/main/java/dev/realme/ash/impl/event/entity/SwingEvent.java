package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.util.Hand;

@Cancelable
public class SwingEvent extends Event {
   private final Hand hand;

   public SwingEvent(Hand hand) {
      this.hand = hand;
   }

   public Hand getHand() {
      return this.hand;
   }
}
