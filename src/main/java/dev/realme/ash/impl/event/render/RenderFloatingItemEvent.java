package dev.realme.ash.impl.event.render;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Cancelable
public class RenderFloatingItemEvent extends Event {
   private final ItemStack floatingItem;

   public RenderFloatingItemEvent(ItemStack floatingItem) {
      this.floatingItem = floatingItem;
   }

   public Item getFloatingItem() {
      return this.floatingItem.getItem();
   }

   public ItemStack getFloatingItemStack() {
      return this.floatingItem;
   }
}
