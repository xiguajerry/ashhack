package dev.realme.ash.mixin.gui.hud;

import dev.realme.ash.impl.imixin.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ChatHudLine.Visible.class})
public class MixinChatHudLineVisible implements IChatHudLine {
   @Unique
   private int id = 0;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }
}
