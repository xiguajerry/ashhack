package dev.realme.ash.impl.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;

public class HClipCommand extends Command {
   public HClipCommand() {
      super("HClip", "Horizontally clips the player", literal("hclip"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.then(argument("getDistance", DoubleArgumentType.doubleArg()).executes((c) -> {
         double dist = DoubleArgumentType.getDouble(c, "getDistance");
         double rad = Math.toRadians(mc.player.getYaw() + 90.0F);
         double x = Math.cos(rad) * dist;
         double z = Math.sin(rad) * dist;
         Managers.POSITION.setPositionXZ(x, z);
         ChatUtil.clientSendMessage("Horizontally clipped §s" + dist + "§f blocks");
         return 1;
      })).executes((c) -> {
         ChatUtil.error("Must provide distance!");
         return 1;
      });
   }
}
