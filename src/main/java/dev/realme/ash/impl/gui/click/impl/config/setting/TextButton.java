package dev.realme.ash.impl.gui.click.impl.config.setting;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.gui.click.impl.config.CategoryFrame;
import dev.realme.ash.impl.gui.click.impl.config.ModuleButton;
import net.minecraft.client.gui.DrawContext;

public class TextButton extends ConfigButton {
   private final StringBuilder text;
   private boolean typing;

   public TextButton(CategoryFrame frame, ModuleButton moduleButton, Config config, float x, float y) {
      super(frame, moduleButton, config, x, y);
      this.text = new StringBuilder((String)config.getValue());
   }

   public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta) {
      RenderManager.renderText(context, (String)this.config.getValue(), ix + 3.0F, iy + 3.0F, -1);
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isWithin(mouseX, mouseY) && button == 0) {
         this.typing = !this.typing;
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.typing) {
      }

   }
}
