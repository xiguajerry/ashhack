package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.api.command.ModuleArgumentType;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.util.chat.ChatUtil;

public class ToggleCommand extends Command {
   public ToggleCommand() {
      super("Toggle", "Enables/Disables a module", literal("toggle"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      ((LiteralArgumentBuilder)builder.then(argument("module", ModuleArgumentType.module()).executes((c) -> {
         Module module = ModuleArgumentType.getModule(c, "module");
         if (module instanceof ToggleModule t) {
            t.toggle();
            Object[] var10001 = new Object[2];
            String var10004 = t.getName();
            var10001[0] = "§7" + var10004 + "§f";
            var10001[1] = t.isEnabled() ? "§senabled§f" : "§cdisabled§f";
            ChatUtil.clientSendMessage("%s is now %s", var10001);
         }

         return 1;
      }))).executes((c) -> {
         ChatUtil.error("Must provide module to toggle!");
         return 1;
      });
   }
}
