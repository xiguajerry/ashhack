package dev.realme.ash.api.config.setting;

import dev.realme.ash.Ash;
import dev.realme.ash.api.config.ConfigContainer;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.util.render.animation.Animation;

public class ToggleConfig extends BooleanConfig {
   public ToggleConfig(String name, String desc, Boolean val) {
      super(name, desc, val);
   }

   public void setValue(Boolean val) {
      super.setValue(val);
      ConfigContainer container = this.getContainer();
      if (container instanceof ToggleModule toggle) {
         Animation anim = toggle.getAnimation();
         anim.setState(val);
         if (val) {
            Ash.EVENT_HANDLER.subscribe(toggle);
         } else {
            Ash.EVENT_HANDLER.unsubscribe(toggle);
         }
      }

   }

   public void enable() {
      ConfigContainer container = this.getContainer();
      if (container instanceof ToggleModule toggle) {
         toggle.enable();
      }

   }

   public void disable() {
      ConfigContainer container = this.getContainer();
      if (container instanceof ToggleModule toggle) {
         toggle.disable();
      }

   }
}
