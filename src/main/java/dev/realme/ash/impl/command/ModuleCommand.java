package dev.realme.ash.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.api.command.ConfigArgumentType;
import dev.realme.ash.api.command.ItemArgumentType;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.ItemListConfig;
import dev.realme.ash.api.macro.Macro;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.util.KeyboardUtil;
import dev.realme.ash.util.chat.ChatUtil;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.Item;

public class ModuleCommand extends Command {
   private final Module module;

   public ModuleCommand(Module module) {
      super(module.getName(), module.getDescription(), literal(module.getName().toLowerCase()));
      this.module = module;
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      ((LiteralArgumentBuilder)builder.then(((RequiredArgumentBuilder)argument("setting", ConfigArgumentType.config(this.module)).then(((RequiredArgumentBuilder)argument("value", StringArgumentType.string()).executes((c) -> {
         Config config = ConfigArgumentType.getConfig(c, "setting");
         String value = StringArgumentType.getString(c, "value");
         if (value.equalsIgnoreCase("list")) {
            return this.listItems(config, value);
         } else if (value.equalsIgnoreCase("reset")) {
            config.resetValue();
            ChatUtil.clientSendMessage("§7%s§f was reset to default value", config.getName());
            return 1;
         } else {
            return this.updateValue(config, value);
         }
      })).then(argument("item", ItemArgumentType.item()).executes((c) -> {
         Config config = ConfigArgumentType.getConfig(c, "setting");
         String action = StringArgumentType.getString(c, "value");
         Item value = ItemArgumentType.getItem(c, "item");
         return this.addDeleteItem(config, action, value);
      })))).executes((c) -> {
         ChatUtil.error("Must provide a value!");
         return 1;
      }))).executes((c) -> {
         Module patt2799$temp = this.module;
         if (patt2799$temp instanceof ToggleModule m) {
            m.toggle();
            Object[] var10001 = new Object[2];
            String var10004 = m.getName();
            var10001[0] = "§7" + var10004 + "§f";
            var10001[1] = m.isEnabled() ? "§senabled§f" : "§cdisabled§f";
            ChatUtil.clientSendMessage("%s is now %s", var10001);
         }

         return 1;
      });
   }

   private int addDeleteItem(Config config, String action, Item value) {
      if (config instanceof ItemListConfig) {
         List list = (List)config.getValue();
         String var10000;
         if (action.equalsIgnoreCase("add")) {
            list.add(value);
            var10000 = value.getName().getString();
            ChatUtil.clientSendMessage("Added §s" + var10000 + "§f to §7" + config.getName());
         } else if (action.equalsIgnoreCase("del") || action.equalsIgnoreCase("remove")) {
            list.remove(value);
            var10000 = value.getName().getString();
            ChatUtil.clientSendMessage("Removed §c" + var10000 + "§f from §7" + config.getName());
         }
      }

      return 1;
   }

   private int listItems(Config config, String action) {
      if (config instanceof ItemListConfig) {
         List list = (List)config.getValue();
         if (action.equalsIgnoreCase("list")) {
            if (list.isEmpty()) {
               ChatUtil.error("There are no items in the list!");
               return 1;
            }

            StringBuilder listString = new StringBuilder();

            for(int i = 0; i < list.size(); ++i) {
               Item item = (Item)list.get(i);
               listString.append(item.getName().getString());
               if (i <= list.size() - 1) {
                  listString.append(", ");
               }
            }

            String var10000 = config.getName();
            ChatUtil.clientSendMessage("§7" + var10000 + "§f: " + listString);
         }
      }

      return 1;
   }

   private int updateValue(Config config, String value) {
      if (config != null && value != null) {
         try {
            if (config.getValue() instanceof Integer) {
               Integer val = Integer.parseInt(value);
               config.setValue(val);
               ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), val.toString());
            } else if (config.getValue() instanceof Float) {
               Float val = Float.parseFloat(value);
               config.setValue(val);
               ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), val.toString());
            } else if (config.getValue() instanceof Double) {
               Double val = Double.parseDouble(value);
               config.setValue(val);
               ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), val.toString());
            }
         } catch (NumberFormatException var7) {
            ChatUtil.error("Not a number!");
         }

         if (config.getValue() instanceof Boolean) {
            Boolean val = Boolean.parseBoolean(value);
            config.setValue(val);
            ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), val ? "True" : "False");
         } else if (config.getValue() instanceof Enum) {
            String[] values = (String[])Arrays.stream((Enum[])((Enum)config.getValue()).getClass().getEnumConstants()).map(Enum::name).toArray((x$0) -> {
               return new String[x$0];
            });
            int ix = -1;

            for(int i = 0; i < values.length; ++i) {
               if (values[i].equalsIgnoreCase(value)) {
                  ix = i;
                  break;
               }
            }

            if (ix == -1) {
               ChatUtil.error("Not a valid mode!");
               return 0;
            }

            Enum val = Enum.valueOf(((Enum)config.getValue()).getClass(), values[ix]);
            config.setValue(val);
            ChatUtil.clientSendMessage("§7%s§f was set to mode §s%s", config.getName(), value);
         } else {
            Object var12 = config.getValue();
            if (var12 instanceof Macro) {
               Macro macro = (Macro)var12;
               if (config.getName().equalsIgnoreCase("Keybind")) {
                  ChatUtil.error("Use the 'bind' command to keybind modules!");
                  return 0;
               }

               config.setValue(new Macro(config.getId(), KeyboardUtil.getKeyCode(value), macro.getRunnable()));
               ChatUtil.clientSendMessage("§7%s§f was set to key §s%s", config.getName(), value);
            } else if (config.getValue() instanceof String) {
               config.setValue(value);
            }
         }

         return 1;
      } else {
         return 0;
      }
   }
}
