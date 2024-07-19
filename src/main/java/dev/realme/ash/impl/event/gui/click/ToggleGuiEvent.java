package dev.realme.ash.impl.event.gui.click;

import dev.realme.ash.api.event.Event;
import dev.realme.ash.api.module.ToggleModule;

public class ToggleGuiEvent extends Event {
   private final ToggleModule module;

   public ToggleGuiEvent(ToggleModule module) {
      this.module = module;
   }

   public ToggleModule getModule() {
      return this.module;
   }

   public boolean isEnabled() {
      return this.module.isEnabled();
   }
}
