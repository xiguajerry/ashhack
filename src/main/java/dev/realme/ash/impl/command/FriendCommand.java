package dev.realme.ash.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;

public class FriendCommand extends Command {
   public FriendCommand() {
      super("Friend", "Adds/Removes a friend from the player list", literal("friend"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      ((LiteralArgumentBuilder)builder.then(((RequiredArgumentBuilder)argument("add/del", StringArgumentType.string()).suggests(suggest(new String[]{"add", "del", "remove"})).then(argument("friend_name", StringArgumentType.string()).executes((c) -> {
         String playerName = StringArgumentType.getString(c, "friend_name");
         String action = StringArgumentType.getString(c, "add/del");
         if (action.equalsIgnoreCase("add")) {
            if (Managers.SOCIAL.isFriend(playerName)) {
               ChatUtil.error("Player is already friended!");
               return 0;
            }

            ChatUtil.clientSendMessage("Added friend with name §s" + playerName);
            Managers.SOCIAL.addFriend(playerName);
         } else if (action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("del")) {
            if (!Managers.SOCIAL.isFriend(playerName)) {
               ChatUtil.error("Player is not friended!");
               return 0;
            }

            ChatUtil.clientSendMessage("Removed friend with name §c" + playerName);
            Managers.SOCIAL.remove(playerName);
         }

         return 1;
      }))).executes((c) -> {
         ChatUtil.error("Must provide player to friend!");
         return 1;
      }))).executes((c) -> {
         ChatUtil.error("Invalid usage! Usage: " + this.getUsage());
         return 1;
      });
   }
}
