package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.Ash;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.util.chat.ChatUtil;
import java.awt.Desktop;
import java.io.IOException;

public class OpenFolderCommand extends Command {
   public OpenFolderCommand() {
      super("OpenFolder", "Opens the client configurations folder", literal("openfolder"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.executes((c) -> {
         try {
            Desktop.getDesktop().open(Ash.CONFIG.getClientDirectory().toFile());
         } catch (IOException var2) {
            IOException e = var2;
            e.printStackTrace();
            ChatUtil.error("Failed to open client folder!");
         }

         return 1;
      });
   }
}
