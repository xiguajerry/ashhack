package dev.realme.ash.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.KeyboardUtil;
import dev.realme.ash.util.chat.ChatUtil;

public class PrefixCommand extends Command {
   public PrefixCommand() {
      super("Prefix", "Allows you to change the chat command prefix", literal("prefix"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      ((LiteralArgumentBuilder)builder.then(argument("prefix", StringArgumentType.string()).executes((c) -> {
         String prefix = StringArgumentType.getString(c, "prefix");
         if (prefix.length() > 1) {
            ChatUtil.error("Prefix can only be one character!");
            return 0;
         } else {
            int keycode = KeyboardUtil.getKeyCode(prefix);
            Managers.COMMAND.setPrefix(prefix, keycode);
            ChatUtil.clientSendMessage("Command prefix changed to §s" + prefix);
            return 1;
         }
      }))).executes((c) -> {
         ChatUtil.error("Please provide a new prefix!");
         return 1;
      });
   }
}
