package dev.realme.ash.impl.event.gui.chat;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.util.StringHelper;
import org.apache.commons.lang3.StringUtils;

@Cancelable
public class ChatMessageEvent extends Event {
   public final String message;

   public ChatMessageEvent(String message) {
      this.message = message;
   }

   public String getMessage() {
      return this.normalize(this.message);
   }

   private String normalize(String chatText) {
      return StringHelper.truncateChat(StringUtils.normalizeSpace(chatText.trim()));
   }

   @Cancelable
   public static class Server extends ChatMessageEvent {
      public Server(String message) {
         super(message);
      }
   }

   @Cancelable
   public static class Client extends ChatMessageEvent {
      public Client(String message) {
         super(message);
      }
   }
}
