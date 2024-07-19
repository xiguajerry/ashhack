package dev.realme.ash.util.chat;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.imixin.IChatHud;
import dev.realme.ash.util.Globals;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatUtil implements Globals {
   private static final String PREFIX;

   public static void sendChatMessageWidthId(String message, int id) {
      if (mc.inGameHud != null && Ash.loaded) {
         ((IChatHud)mc.inGameHud.getChatHud()).addMessage(Text.of(PREFIX + message), id);
      }
   }

   public static void clientSendMessage(String message) {
      mc.inGameHud.getChatHud().addMessage(Text.of(PREFIX + message), (MessageSignatureData)null, (MessageIndicator)null);
   }

   public static void clientSendMessage(String message, Object... params) {
      clientSendMessage(String.format(message, params));
   }

   public static void clientSendMessageRaw(String message) {
      mc.inGameHud.getChatHud().addMessage(Text.of(message), (MessageSignatureData)null, (MessageIndicator)null);
   }

   public static void clientSendMessageRaw(String message, Object... params) {
      clientSendMessageRaw(String.format(message, params));
   }

   public static void serverSendCommand(String message) {
      if (mc.player != null) {
         mc.player.networkHandler.sendChatCommand(message);
      }

   }

   public static void serverSendMessage(String message) {
      if (mc.player != null) {
         mc.player.networkHandler.sendChatMessage(message);
      }

   }

   public static void serverSendMessage(PlayerEntity player, String message) {
      if (mc.player != null) {
         String reply = "/msg " + player.getName().getString() + " ";
         mc.player.networkHandler.sendChatMessage(reply + message);
      }

   }

   public static void error(String message) {
      clientSendMessage(Formatting.RED + message);
   }

   public static void error(String message, Object... params) {
      clientSendMessage(Formatting.RED + message, params);
   }

   static {
      PREFIX = Formatting.RED + "[" + Formatting.RED + "Ash\ud83d\udd25" + Formatting.RED + "] ";
   }
}
