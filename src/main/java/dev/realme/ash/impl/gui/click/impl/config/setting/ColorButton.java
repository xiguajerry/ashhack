package dev.realme.ash.impl.gui.click.impl.config.setting;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.gui.click.ClickGuiScreen;
import dev.realme.ash.impl.gui.click.impl.config.CategoryFrame;
import dev.realme.ash.impl.gui.click.impl.config.ModuleButton;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.render.animation.Animation;
import dev.realme.ash.util.render.animation.Easing;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class ColorButton extends ConfigButton {
   private boolean open;
   private final Animation pickerAnimation;
   private float[] selectedColor;

   public ColorButton(CategoryFrame frame, ModuleButton moduleButton, Config config, float x, float y) {
      super(frame, moduleButton, config, x, y);
      this.pickerAnimation = new Animation(false, 200.0F, Easing.CUBIC_IN_OUT);
      float[] hsb = ((ColorConfig)config).getHsb();
      this.selectedColor = new float[]{hsb[0], hsb[1], 1.0F - hsb[2], hsb[3]};
   }

   public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta) {
      this.x = ix;
      this.y = iy;
      this.fill(context, ix + this.width - 11.0F, iy + 2.0F, 10.0, 10.0, ((ColorConfig)this.config).getRgb());
      RenderManager.renderText(context, this.config.getName(), ix + 2.0F, iy + 4.0F, -1);
      if (this.pickerAnimation.getFactor() > 0.009999999776482582) {
         ColorConfig colorConfig = (ColorConfig)this.config;
         if (ClickGuiScreen.MOUSE_LEFT_HOLD) {
            if (this.isMouseOver(mouseX, mouseY, this.x + 1.0F, this.y + this.height + 2.0F, this.width - 2.0F, this.width) && !colorConfig.isGlobal()) {
               this.selectedColor[1] = (mouseX - (this.x + 1.0F)) / (this.width - 1.0F);
               this.selectedColor[2] = (mouseY - (this.y + this.height + 2.0F)) / this.width;
            }

            if (this.isMouseOver(mouseX, mouseY, this.x + 1.0F, this.y + this.height + 4.0F + this.width, this.width - 2.0F, 10.0) && !colorConfig.isGlobal()) {
               this.selectedColor[0] = (mouseX - (this.x + 1.0F)) / (this.width - 1.0F);
            }

            if (colorConfig.allowAlpha() && this.isMouseOver(mouseX, mouseY, this.x + 1.0F, this.y + this.height + 17.0F + this.width, this.width - 2.0F, 10.0)) {
               this.selectedColor[3] = (mouseX - (this.x + 1.0F)) / (this.width - 1.0F);
            }

            Color color = Color.getHSBColor(MathHelper.clamp(this.selectedColor[0], 0.001F, 0.999F), MathHelper.clamp(this.selectedColor[1], 0.001F, 0.999F), 1.0F - MathHelper.clamp(this.selectedColor[2], 0.001F, 0.999F));
            color = new Color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, MathHelper.clamp(this.selectedColor[3], 0.0F, 1.0F));
            colorConfig.setValue(color);
         }

         float[] hsb = colorConfig.getHsb();
         int color = Color.HSBtoRGB(hsb[0], 1.0F, 1.0F);
         this.enableScissor((int)this.x, (int)(this.y + this.height), (int)(this.x + this.width), (int)(this.y + this.height + this.getPickerHeight() * this.getScaledTime()));

         for(float i = 0.0F; i < this.width - 2.0F; ++i) {
            float hue = i / (this.width - 2.0F);
            this.fill(context, this.x + 1.0F + i, this.y + this.height + 4.0F + this.width, 1.0, 10.0, Color.getHSBColor(hue, 1.0F, 1.0F).getRGB());
         }

         this.fill(context, this.x + 1.0F + (this.width - 2.0F) * hsb[0], this.y + this.height + 4.0F + this.width, 1.0, 10.0, -1);
         this.fillGradientQuad(context, this.x + 1.0F, this.y + this.height + 2.0F, this.x + this.width - 1.0F, this.y + this.height + 2.0F + this.width, -1, color, true);
         this.fillGradientQuad(context, this.x + 1.0F, this.y + this.height + 2.0F, this.x + this.width - 1.0F, this.y + this.height + 2.0F + this.width, 0, -16777216, false);
         this.fill(context, this.x + this.width * hsb[1], this.y + this.height + 1.0F + this.width * (1.0F - hsb[2]), 2.0, 2.0, -1);
         if (colorConfig.allowAlpha()) {
            this.fillGradient(context, this.x + 1.0F, this.y + this.height + 17.0F + this.width, this.x + this.width - 1.0F, this.y + this.height + 27.0F + this.width, color, -16777216);
            this.fill(context, this.x + 1.0F + (this.width - 2.0F) * hsb[3], this.y + this.height + 17.0F + this.width, 1.0, 10.0, -1);
         }

         if (!this.config.getContainer().getName().equalsIgnoreCase("Colors")) {
            Animation globalAnimation = colorConfig.getAnimation();
            if (globalAnimation.getFactor() > 0.01) {
               this.fill(context, this.x + 1.0F, this.y + this.height + (colorConfig.allowAlpha() ? 29.0F : 17.0F) + this.width, this.width - 2.0F, 13.0, Modules.CLICK_GUI.getColor((float)globalAnimation.getFactor()));
            }

            RenderManager.renderText(context, "ClientColor", this.x + 3.0F, this.y + this.height + (colorConfig.allowAlpha() ? 31.0F : 21.0F) + this.width, -1);
         }

         this.moduleButton.offset((float)((double)this.getPickerHeight() * this.pickerAnimation.getFactor()));
         ((CategoryFrame)this.frame).offset((float)((double)this.getPickerHeight() * this.pickerAnimation.getFactor() * (double)this.moduleButton.getScaledTime()));
         this.disableScissor();
      }

   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isWithin(mouseX, mouseY) && button == 1) {
         this.open = !this.open;
         this.pickerAnimation.setState(this.open);
      }

      if (!this.config.getContainer().getName().equalsIgnoreCase("Colors") && this.isMouseOver(mouseX, mouseY, this.x + 1.0F, this.y + this.height + (((ColorConfig)this.config).allowAlpha() ? 29.0F : 17.0F) + this.width, this.width - 2.0F, 13.0) && button == 0) {
         ColorConfig colorConfig = (ColorConfig)this.config;
         boolean val = !colorConfig.isGlobal();
         colorConfig.setGlobal(val);
         float[] hsb = ((ColorConfig)this.config).getHsb();
         this.selectedColor = new float[]{hsb[0], hsb[1], 1.0F - hsb[2], hsb[3]};
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
   }

   public float getPickerHeight() {
      float pickerHeight = 16.0F;
      if (((ColorConfig)this.config).allowAlpha()) {
         pickerHeight += 12.0F;
      }

      if (!this.config.getContainer().getName().equalsIgnoreCase("Colors")) {
         pickerHeight += 15.0F;
      }

      return pickerHeight + this.width;
   }

   public float getScaledTime() {
      return (float)this.pickerAnimation.getFactor();
   }
}
