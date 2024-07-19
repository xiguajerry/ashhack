package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.util.chat.ChatUtil;

public class ReloadSoundCommand extends Command {
   public ReloadSoundCommand() {
      super("ReloadSound", "Reloads the Minecraft sound system", literal("reloadsound"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.executes((context) -> {
         mc.getSoundManager().reloadSounds();
         ChatUtil.clientSendMessage("Reloaded the SoundSystem");
         return 1;
      });
   }
}
