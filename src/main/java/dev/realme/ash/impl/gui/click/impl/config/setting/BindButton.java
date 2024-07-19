package dev.realme.ash.impl.gui.click.impl.config.setting;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.MacroConfig;
import dev.realme.ash.api.macro.Macro;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.gui.click.impl.config.CategoryFrame;
import dev.realme.ash.impl.gui.click.impl.config.ModuleButton;
import dev.realme.ash.impl.module.client.ClickGuiModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

public class BindButton extends ConfigButton {
   private boolean listening;

   public BindButton(CategoryFrame frame, ModuleButton moduleButton, Config config, float x, float y) {
      super(frame, moduleButton, config, x, y);
   }

   public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta) {
      ClickGuiModule.CLICK_GUI_SCREEN.setCloseOnEscape(!this.listening);
      this.x = ix;
      this.y = iy;
      Macro macro = (Macro)this.config.getValue();
      String val = this.listening ? "..." : macro.getKeyName();
      this.rect(context, 0);
      RenderManager.renderText(context, this.config.getName() + Formatting.GRAY + " " + val, ix + 2.0F, iy + 4.0F, -1);
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isWithin(mouseX, mouseY)) {
         if (button == 0) {
            this.listening = !this.listening;
         } else if (button == 1 && !this.listening) {
            ((MacroConfig)this.config).setValue(-1);
         } else if (this.listening) {
            if (button != 1) {
               ((MacroConfig)this.config).setValue(1000 + button);
            }

            this.listening = false;
         }
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.listening) {
         if (keyCode != 256 && keyCode != 259) {
            ((MacroConfig)this.config).setValue(keyCode);
         } else {
            ((MacroConfig)this.config).setValue(-1);
         }

         this.listening = false;
      }

   }

   public boolean isListening() {
      return this.listening;
   }

   public void setListening(boolean listening) {
      this.listening = listening;
   }
}
