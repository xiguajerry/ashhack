package dev.realme.ash.impl.event.network;

import dev.realme.ash.api.event.Event;
import dev.realme.ash.util.Globals;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class SetCurrentHandEvent extends Event implements Globals {
   private final Hand hand;

   public SetCurrentHandEvent(Hand hand) {
      this.hand = hand;
   }

   public Hand getHand() {
      return this.hand;
   }

   public ItemStack getStackInHand() {
      return mc.player.getStackInHand(this.hand);
   }
}
