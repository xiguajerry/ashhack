package dev.realme.ash.api.module;

import dev.realme.ash.Ash;

public class ConcurrentModule extends Module {
   public ConcurrentModule(String name, String desc, ModuleCategory category) {
      super(name, desc, category);
      Ash.EVENT_HANDLER.subscribe(this);
   }
}
