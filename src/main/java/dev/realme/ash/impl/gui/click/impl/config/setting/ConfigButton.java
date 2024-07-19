package dev.realme.ash.impl.gui.click.impl.config.setting;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.impl.gui.click.component.Button;
import dev.realme.ash.impl.gui.click.impl.config.CategoryFrame;
import dev.realme.ash.impl.gui.click.impl.config.ModuleButton;
import net.minecraft.client.gui.DrawContext;

public abstract class ConfigButton <T> extends Button {
   protected final Config<T> config;
   protected final ModuleButton moduleButton;

   public ConfigButton(CategoryFrame frame, ModuleButton moduleButton, Config<T> config, float x, float y) {
      super(frame, x, y, 99.0F, 13.0F);
      this.moduleButton = moduleButton;
      this.config = config;
   }

   public void render(DrawContext context, float mouseX, float mouseY, float delta) {
      this.render(context, this.x, this.y, mouseX, mouseY, delta);
   }

   public abstract void render(DrawContext var1, float var2, float var3, float var4, float var5, float var6);

   public Config<T> getConfig() {
      return this.config;
   }
}
