package dev.realme.ash.mixin.gui.screen;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Screen.class})
public abstract class MixinScreen {
   @Shadow
   public int width;
   @Shadow
   public int height;
   @Shadow
   @Final
   private List drawables;
   @Shadow
   @Final
   protected Text title;
   @Shadow
   protected @Nullable MinecraftClient client;

   @Shadow
   protected abstract Element addDrawableChild(Element var1);

   @Shadow
   public void tick() {
   }

   @Unique
   public List getDrawables() {
      return this.drawables;
   }
}
