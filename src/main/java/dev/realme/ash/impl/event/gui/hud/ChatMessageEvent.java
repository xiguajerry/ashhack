package dev.realme.ash.impl.event.gui.hud;

import dev.realme.ash.api.event.Event;
import net.minecraft.text.Text;

public class ChatMessageEvent extends Event {
   private final Text text;

   public ChatMessageEvent(Text text) {
      this.text = text;
   }

   public Text getText() {
      return this.text;
   }
}
