package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.util.chat.ChatUtil;
import net.minecraft.item.ItemStack;

public class NbtCommand extends Command {
   public NbtCommand() {
      super("Nbt", "Displays all nbt tags on the held item", literal("nbt"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.executes((context) -> {
         ItemStack mainhand = mc.player.getMainHandStack();
         if (mainhand.hasNbt() && mainhand.getNbt() != null) {
            ChatUtil.clientSendMessage(mainhand.getNbt().toString());
            return 1;
         } else {
            ChatUtil.error("No Nbt tags on this item!");
            return 0;
         }
      });
   }
}
