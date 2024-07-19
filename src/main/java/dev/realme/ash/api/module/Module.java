package dev.realme.ash.api.module;

import dev.realme.ash.api.config.ConfigContainer;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.chat.ChatUtil;

public class Module extends ConfigContainer implements Globals {
   public static final String MODULE_ID_FORMAT = "%s-module";
   private final String desc;
   private final ModuleCategory category;

   public Module(String name, String desc, ModuleCategory category) {
      super(name);
      this.desc = desc;
      this.category = category;
   }

   protected void sendModuleMessage(String message) {
      ChatUtil.clientSendMessageRaw("§s[%s]§f %s", this.name, message);
   }

   protected void sendModuleMessage(String message, Object... params) {
      this.sendModuleMessage(String.format(message, params));
   }

   public String getId() {
      return String.format("%s-module", this.name.toLowerCase());
   }

   public String getDescription() {
      return this.desc;
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public String getModuleData() {
      return "ARRAYLIST_INFO";
   }

   public static boolean nullCheck() {
      return mc.player == null || mc.world == null;
   }
}
