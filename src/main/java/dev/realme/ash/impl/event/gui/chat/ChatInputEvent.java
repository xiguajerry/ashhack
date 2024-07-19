package dev.realme.ash.impl.event.gui.chat;

import dev.realme.ash.api.event.Event;

public class ChatInputEvent extends Event {
   private final String chatText;

   public ChatInputEvent(String chatText) {
      this.chatText = chatText;
   }

   public String getChatText() {
      return this.chatText;
   }
}
