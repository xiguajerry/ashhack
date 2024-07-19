package dev.realme.ash.impl.gui.click.impl.config;

import dev.realme.ash.api.module.Module;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.gui.click.ClickGuiScreen;
import dev.realme.ash.impl.gui.click.component.Frame;
import dev.realme.ash.impl.gui.click.impl.config.setting.ColorButton;
import dev.realme.ash.impl.gui.click.impl.config.setting.ConfigButton;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.render.animation.Animation;
import dev.realme.ash.util.render.animation.Easing;
import dev.realme.ash.util.string.EnumFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.DrawContext;

public class CategoryFrame extends Frame {
   private final String name;
   private final ModuleCategory category;
   private final List moduleButtons;
   private float off;
   private float inner;
   private boolean open;
   private boolean drag;
   private final Animation categoryAnimation;

   public CategoryFrame(ModuleCategory category, float x, float y, float width, float height) {
      super(x, y, width, height);
      this.moduleButtons = new CopyOnWriteArrayList();
      this.categoryAnimation = new Animation(false, 200.0F, Easing.CUBIC_IN_OUT);
      this.category = category;
      this.name = EnumFormatter.formatEnum(category);
      Iterator var6 = Managers.MODULE.getModules().iterator();

      while(var6.hasNext()) {
         Module module = (Module)var6.next();
         if (module.getCategory() == category) {
            this.moduleButtons.add(new ModuleButton(module, this, x, y));
         }
      }

      this.categoryAnimation.setState(true);
      this.open = true;
   }

   public CategoryFrame(ModuleCategory category, float x, float y) {
      this(category, x, y, 105.0F, 15.0F);
   }

   public void render(DrawContext context, float mouseX, float mouseY, float delta) {
      if (this.drag) {
         this.x += (float)ClickGuiScreen.MOUSE_X - this.px;
         this.y += (float)ClickGuiScreen.MOUSE_Y - this.py;
      }

      this.fheight = 2.0F;
      Iterator var5 = this.moduleButtons.iterator();

      while(true) {
         ModuleButton moduleButton;
         do {
            if (!var5.hasNext()) {
               if (this.y < -(this.fheight - 10.0F)) {
                  this.y = -(this.fheight - 10.0F);
               }

               if (this.y > (float)(mc.getWindow().getHeight() - 10)) {
                  this.y = (float)(mc.getWindow().getHeight() - 10);
               }

               this.rect(context, Modules.CLICK_GUI.getColor(1.7F));
               RenderManager.renderText(context, this.name, this.x + 3.0F, this.y + 4.0F, -1);
               if (this.categoryAnimation.getFactor() > 0.009999999776482582) {
                  this.enableScissor((int)this.x, (int)(this.y + this.height), (int)(this.x + this.width), (int)((double)(this.y + this.height) + (double)this.fheight * this.categoryAnimation.getFactor()));
                  this.fill(context, this.x, this.y + this.height, this.width, this.fheight, 1996488704);
                  this.off = this.y + this.height + 1.0F;
                  this.inner = this.off;

                  for(var5 = this.moduleButtons.iterator(); var5.hasNext(); this.inner += moduleButton.getHeight() + 1.0F) {
                     moduleButton = (ModuleButton)var5.next();
                     moduleButton.render(context, this.x + 1.0F, this.inner + 1.0F, mouseX, mouseY, delta);
                     this.off += (float)((double)(moduleButton.getHeight() + 1.0F) * this.categoryAnimation.getFactor());
                  }

                  this.disableScissor();
               }

               this.px = (float)ClickGuiScreen.MOUSE_X;
               this.py = (float)ClickGuiScreen.MOUSE_Y;
               return;
            }

            moduleButton = (ModuleButton)var5.next();
            this.fheight += moduleButton.getHeight() + 1.0F;
         } while(moduleButton.getScaledTime() < 0.01F);

         this.fheight += 3.0F * moduleButton.getScaledTime();
         Iterator var7 = moduleButton.getConfigButtons().iterator();

         while(var7.hasNext()) {
            ConfigButton configButton = (ConfigButton)var7.next();
            if (configButton.getConfig().isVisible()) {
               this.fheight += configButton.getHeight() * moduleButton.getScaledTime();
               if (configButton instanceof ColorButton colorPicker) {
                   if (colorPicker.getScaledTime() > 0.01F) {
                     this.fheight += colorPicker.getPickerHeight() * colorPicker.getScaledTime() * moduleButton.getScaledTime();
                  }
               }
            }
         }
      }
   }

   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton == 1 && this.isWithin(mouseX, mouseY)) {
         this.open = !this.open;
         this.categoryAnimation.setState(this.open);
      }

      if (this.open) {
         Iterator var6 = this.moduleButtons.iterator();

         while(var6.hasNext()) {
            ModuleButton button = (ModuleButton)var6.next();
            button.mouseClicked(mouseX, mouseY, mouseButton);
         }
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
      super.mouseReleased(mouseX, mouseY, mouseButton);
      this.drag = false;
      if (this.open) {
         Iterator var6 = this.moduleButtons.iterator();

         while(var6.hasNext()) {
            ModuleButton button = (ModuleButton)var6.next();
            button.mouseReleased(mouseX, mouseY, mouseButton);
         }
      }

   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      super.keyPressed(keyCode, scanCode, modifiers);
      if (this.open) {
         Iterator var4 = this.moduleButtons.iterator();

         while(var4.hasNext()) {
            ModuleButton button = (ModuleButton)var4.next();
            button.keyPressed(keyCode, scanCode, modifiers);
         }
      }

   }

   public boolean isWithinTotal(float mx, float my) {
      return this.isMouseOver(mx, my, this.x, this.y, this.width, this.getTotalHeight());
   }

   public void offset(float in) {
      this.off += in;
      this.inner += in;
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public float getTotalHeight() {
      return this.height + this.fheight;
   }

   public List getModuleButtons() {
      return this.moduleButtons;
   }

   public void setDragging(boolean drag) {
      this.drag = drag;
   }

   public boolean isDragging() {
      return this.drag;
   }
}
