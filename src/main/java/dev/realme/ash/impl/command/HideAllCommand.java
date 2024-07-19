package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;
import java.util.Iterator;

public class HideAllCommand extends Command {
   public HideAllCommand() {
      super("HideAll", "Hides all modules from the arraylist", literal("hideall"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.executes((c) -> {
         Iterator var1 = Managers.MODULE.getModules().iterator();

         while(var1.hasNext()) {
            Module module = (Module)var1.next();
            if (module instanceof ToggleModule toggleModule) {
               if (!toggleModule.isHidden()) {
                  toggleModule.setHidden(true);
               }
            }
         }

         ChatUtil.clientSendMessage("All modules are hidden");
         return 1;
      });
   }
}
