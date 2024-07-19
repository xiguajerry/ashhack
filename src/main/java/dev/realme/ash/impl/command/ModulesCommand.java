package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.AshMod;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;
import java.util.Iterator;
import net.minecraft.util.Formatting;

public class ModulesCommand extends Command {
   public ModulesCommand() {
      super("Modules", "Displays all client modules", literal("modules"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.executes((c) -> {
         StringBuilder modulesList = new StringBuilder();
         Iterator var2 = Managers.MODULE.getModules().iterator();

         while(var2.hasNext()) {
            String var10000;
            Module module;
            label24: {
               module = (Module)var2.next();
               if (module instanceof ToggleModule t) {
                  if (t.isEnabled()) {
                     var10000 = "§s";
                     break label24;
                  }
               }

               var10000 = "§f";
            }

            String formatting = var10000;
            modulesList.append(formatting);
            modulesList.append(module.getName());
            modulesList.append(Formatting.RESET);
            if (!module.getName().equalsIgnoreCase(AshMod.isBaritonePresent() ? "Baritone" : "Speedmine")) {
               modulesList.append(", ");
            }
         }

         ChatUtil.clientSendMessageRaw(" §7Modules:§f " + modulesList);
         return 1;
      });
   }
}
