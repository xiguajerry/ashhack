package dev.realme.ash.impl.event.gui.hud;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.text.OrderedText;

@Cancelable
public class ChatTextEvent extends Event {
   private OrderedText text;

   public ChatTextEvent(OrderedText text) {
      this.text = text;
   }

   public void setText(OrderedText text) {
      this.text = text;
   }

   public OrderedText getText() {
      return this.text;
   }
}
