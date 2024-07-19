package dev.realme.ash.api.module;

import dev.realme.ash.api.Hideable;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.MacroConfig;
import dev.realme.ash.api.config.setting.ToggleConfig;
import dev.realme.ash.api.macro.Macro;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.render.animation.Animation;
import dev.realme.ash.util.render.animation.Easing;
import java.util.Objects;
import net.minecraft.util.Formatting;

public class ToggleModule extends Module implements Hideable {
   private final Animation animation;
   Config enabledConfig;
   Config keybindingConfig;
   Config hiddenConfig;

   public ToggleModule(String name, String desc, ModuleCategory category) {
      super(name, desc, category);
      this.animation = new Animation(false, 300.0F, Easing.CUBIC_IN_OUT);
      this.enabledConfig = new ToggleConfig("Enabled", "The module enabled state. This state is true when the module is running.", false);
      this.keybindingConfig = new MacroConfig("Keybind", "The module keybinding. Pressing this key will toggle the module enabled state. Press [BACKSPACE] to delete the keybind.", new Macro(this.getId(), -1, () -> {
         this.toggle();
      }));
      this.hiddenConfig = new BooleanConfig("Hidden", "The hidden state of the module in the Arraylist", false);
      this.register(this.keybindingConfig, this.enabledConfig, this.hiddenConfig);
   }

   public ToggleModule(String name, String desc, ModuleCategory category, Integer keycode) {
      this(name, desc, category);
      this.keybind(keycode);
   }

   public boolean isHidden() {
      return (Boolean)this.hiddenConfig.getValue();
   }

   public void setHidden(boolean hidden) {
      this.hiddenConfig.setValue(hidden);
   }

   public void toggle() {
      if (this.isEnabled()) {
         this.disable();
      } else {
         this.enable();
      }

   }

   public void enable() {
      this.enabledConfig.setValue(true);
      ChatUtil.sendChatMessageWidthId(Formatting.AQUA + this.name + Formatting.GREEN + " Enabled.", this.hashCode());
      this.onEnable();
   }

   public void disable() {
      this.enabledConfig.setValue(false);
      ChatUtil.sendChatMessageWidthId(Formatting.AQUA + this.name + Formatting.RED + " Disabled.", this.hashCode());
      this.onDisable();
   }

   public int hashCode(Objects objects) {
      return Objects.hash(objects);
   }

   public int hashCode() {
      return Objects.hash(this.name);
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   public void keybind(int keycode) {
      this.keybindingConfig.setContainer(this);
      ((MacroConfig)this.keybindingConfig).setValue(keycode);
   }

   public boolean isEnabled() {
      return (Boolean)this.enabledConfig.getValue();
   }

   public Macro getKeybinding() {
      return (Macro)this.keybindingConfig.getValue();
   }

   public Animation getAnimation() {
      return this.animation;
   }
}
