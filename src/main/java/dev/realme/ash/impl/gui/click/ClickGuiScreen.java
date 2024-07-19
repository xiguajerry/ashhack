package dev.realme.ash.impl.gui.click;

import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.impl.gui.click.component.ScissorStack;
import dev.realme.ash.impl.gui.click.impl.config.CategoryFrame;
import dev.realme.ash.impl.gui.click.impl.config.ModuleButton;
import dev.realme.ash.impl.gui.click.impl.config.setting.BindButton;
import dev.realme.ash.impl.gui.click.impl.config.setting.ConfigButton;
import dev.realme.ash.impl.module.client.ClickGuiModule;
import dev.realme.ash.util.Globals;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGuiScreen extends Screen implements Globals {
   public static int MOUSE_X;
   public static int MOUSE_Y;
   public static boolean MOUSE_RIGHT_CLICK;
   public static boolean MOUSE_RIGHT_HOLD;
   public static boolean MOUSE_LEFT_CLICK;
   public static boolean MOUSE_LEFT_HOLD;
   public static final ScissorStack SCISSOR_STACK = new ScissorStack();
   private final List frames = new CopyOnWriteArrayList();
   private final ClickGuiModule module;
   private CategoryFrame focus;
   private boolean closeOnEscape = true;

   public ClickGuiScreen(ClickGuiModule module) {
      super(Text.literal("ClickGui"));
      this.module = module;
      float x = 2.0F;
      ModuleCategory[] var3 = ModuleCategory.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ModuleCategory category = var3[var5];
         CategoryFrame frame = new CategoryFrame(category, x, 15.0F);
         this.frames.add(frame);
         x += frame.getWidth() + 2.0F;
      }

   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      Iterator var5 = this.frames.iterator();

      while(true) {
         CategoryFrame frame;
         float scale;
         do {
            if (!var5.hasNext()) {
               MOUSE_LEFT_CLICK = false;
               MOUSE_RIGHT_CLICK = false;
               MOUSE_X = mouseX;
               MOUSE_Y = mouseY;
               return;
            }

            frame = (CategoryFrame)var5.next();
            if (frame.isWithinTotal((float)mouseX, (float)mouseY)) {
               this.focus = frame;
            }

            if (frame.isWithin((float)mouseX, (float)mouseY) && MOUSE_LEFT_HOLD && this.checkDragging()) {
               frame.setDragging(true);
            }

            frame.render(context, (float)mouseX, (float)mouseY, delta);
            scale = this.module.getScale();
         } while(scale == 1.0F);

         frame.setDimensions(frame.getWidth() * scale, frame.getHeight() * scale);
         Iterator var8 = frame.getModuleButtons().iterator();

         while(var8.hasNext()) {
            ModuleButton button = (ModuleButton)var8.next();
            button.setDimensions(button.getWidth() * scale, button.getHeight() * scale);

            ConfigButton component;
            for(Iterator var10 = button.getConfigButtons().iterator(); var10.hasNext(); component.setDimensions(component.getWidth() * scale, component.getHeight() * scale)) {
               component = (ConfigButton)var10.next();
               if (component instanceof BindButton bindButton) {
                  if (bindButton.isListening() && !button.isOpen()) {
                     bindButton.setListening(false);
                     this.setCloseOnEscape(true);
                  }
               }
            }
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
      if (mouseButton == 0) {
         MOUSE_LEFT_CLICK = true;
         MOUSE_LEFT_HOLD = true;
      } else if (mouseButton == 1) {
         MOUSE_RIGHT_CLICK = true;
         MOUSE_RIGHT_HOLD = true;
      }

      Iterator var6 = this.frames.iterator();

      while(var6.hasNext()) {
         CategoryFrame frame = (CategoryFrame)var6.next();
         frame.mouseClicked(mouseX, mouseY, mouseButton);
      }

      return super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         MOUSE_LEFT_HOLD = false;
      } else if (button == 1) {
         MOUSE_RIGHT_HOLD = false;
      }

      Iterator var6 = this.frames.iterator();

      while(var6.hasNext()) {
         CategoryFrame frame = (CategoryFrame)var6.next();
         frame.mouseReleased(mouseX, mouseY, button);
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.focus != null) {
         this.focus.setPos(this.focus.getX(), (float)((double)this.focus.getY() + verticalAmount * 50.0));
      }

      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 82 && (modifiers & 2) != 0) {
      }

      Iterator var4 = this.frames.iterator();

      while(var4.hasNext()) {
         CategoryFrame frame = (CategoryFrame)var4.next();
         frame.keyPressed(keyCode, scanCode, modifiers);
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean shouldPause() {
      return false;
   }

   public void close() {
      this.module.disable();
      MOUSE_LEFT_CLICK = false;
      MOUSE_LEFT_HOLD = false;
      MOUSE_RIGHT_CLICK = false;
      MOUSE_RIGHT_HOLD = false;
      super.close();
   }

   public boolean shouldCloseOnEsc() {
      return this.closeOnEscape;
   }

   private boolean checkDragging() {
      Iterator var1 = this.frames.iterator();

      CategoryFrame frame;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         frame = (CategoryFrame)var1.next();
      } while(!frame.isDragging());

      return false;
   }

   public void setCloseOnEscape(boolean closeOnEscape) {
      this.closeOnEscape = closeOnEscape;
   }
}
