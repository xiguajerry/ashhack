package dev.realme.ash.impl.event.gui;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.StageEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

@Cancelable
public class RenderTooltipEvent extends StageEvent {
   public final DrawContext context;
   private final ItemStack stack;
   private final int x;
   private final int y;

   public RenderTooltipEvent(DrawContext context, ItemStack stack, int x, int y) {
      this.context = context;
      this.stack = stack;
      this.x = x;
      this.y = y;
   }

   public DrawContext getContext() {
      return this.context;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }
}
