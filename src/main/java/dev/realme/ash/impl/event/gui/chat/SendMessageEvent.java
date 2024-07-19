package dev.realme.ash.impl.event.gui.chat;

import dev.realme.ash.api.event.Event;

public class SendMessageEvent extends Event {
   public String message;
   public final String defaultMessage;

   public SendMessageEvent(String message) {
      this.defaultMessage = message;
      this.message = message;
   }
}
