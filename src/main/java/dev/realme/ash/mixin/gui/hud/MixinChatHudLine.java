package dev.realme.ash.mixin.gui.hud;

import dev.realme.ash.impl.imixin.IChatHudLine;
import dev.realme.ash.util.Globals;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ChatHudLine.class})
public abstract class MixinChatHudLine implements IChatHudLine, Globals {
   @Unique
   private int id;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }
}
