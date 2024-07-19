package dev.realme.ash.impl.gui.click.impl.config.setting;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.gui.click.impl.config.CategoryFrame;
import dev.realme.ash.impl.gui.click.impl.config.ModuleButton;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.render.animation.Animation;
import net.minecraft.client.gui.DrawContext;

public class CheckboxButton extends ConfigButton {
   public CheckboxButton(CategoryFrame frame, ModuleButton moduleButton, Config config, float x, float y) {
      super(frame, moduleButton, config, x, y);
      config.getAnimation().setState((Boolean)config.getValue());
   }

   public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta) {
      this.x = ix;
      this.y = iy;
      Animation checkboxAnimation = this.config.getAnimation();
      this.rectGradient(context, checkboxAnimation.getFactor() > 0.009999999776482582 ? Modules.CLICK_GUI.getColor((float)checkboxAnimation.getFactor()) : 0, checkboxAnimation.getFactor() > 0.009999999776482582 ? Modules.CLICK_GUI.getColor1((float)checkboxAnimation.getFactor()) : 0);
      RenderManager.renderText(context, this.config.getName(), ix + 2.0F, iy + 4.0F, -1);
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isWithin(mouseX, mouseY) && button == 0) {
         boolean val = (Boolean)this.config.getValue();
         this.config.setValue(!val);
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
   }
}
