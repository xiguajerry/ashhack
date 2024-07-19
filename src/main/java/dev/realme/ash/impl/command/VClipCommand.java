package dev.realme.ash.impl.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;

public class VClipCommand extends Command {
   public VClipCommand() {
      super("VClip", "Vertically clips the player", literal("vclip"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.then(argument("getDistance", DoubleArgumentType.doubleArg()).executes((c) -> {
         double dist = DoubleArgumentType.getDouble(c, "getDistance");
         double y = Managers.POSITION.getY();
         if (Math.abs(y) != 256.0) {
            Managers.POSITION.setPositionY(y + dist);
            ChatUtil.clientSendMessage("Vertically clipped §s" + dist + "§f blocks");
         }

         return 1;
      })).executes((c) -> {
         ChatUtil.error("Must provide distance!");
         return 1;
      });
   }
}
