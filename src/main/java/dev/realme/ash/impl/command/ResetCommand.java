package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.api.command.ModuleArgumentType;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.util.chat.ChatUtil;
import java.util.Iterator;

public class ResetCommand extends Command {
   public ResetCommand() {
      super("Reset", "Resets the values of modules", literal("reset"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.then(argument("module", ModuleArgumentType.module()).executes((context) -> {
         Module module = ModuleArgumentType.getModule(context, "module");
         if (module == null) {
            ChatUtil.error("Invalid module!");
            return 0;
         } else {

             for (Config<?> value : module.getConfigs()) {
                 Config config = (Config) value;
                 if (!config.getName().equalsIgnoreCase("Enabled") && !config.getName().equalsIgnoreCase("Keybind") && !config.getName().equalsIgnoreCase("Hidden")) {
                     config.resetValue();
                 }
             }

            ChatUtil.clientSendMessage("§7" + module.getName() + "§f settings were reset to default values");
            return 1;
         }
      })).executes((context) -> {
         ChatUtil.error("Must provide module to reset!");
         return 1;
      });
   }
}
