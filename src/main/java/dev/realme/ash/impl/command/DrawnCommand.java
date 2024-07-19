package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.api.command.ModuleArgumentType;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.util.chat.ChatUtil;
import net.minecraft.util.Formatting;

public class DrawnCommand extends Command {
   public DrawnCommand() {
      super("Drawn", "Toggles the drawn state of the module", literal("drawn"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.then(argument("module", ModuleArgumentType.module()).executes((c) -> {
         Module module = ModuleArgumentType.getModule(c, "module");
         if (module instanceof ToggleModule toggle) {
            boolean hide = !toggle.isHidden();
            toggle.setHidden(hide);
            String var10000 = module.getName();
            ChatUtil.clientSendMessage("§7" + var10000 + "§f is now " + (hide ? "§chidden§f" : "§svisible§f") + Formatting.RESET + " in the Hud");
         }

         return 1;
      })).executes((c) -> {
         ChatUtil.error("Must provide module to draw!");
         return 1;
      });
   }
}
