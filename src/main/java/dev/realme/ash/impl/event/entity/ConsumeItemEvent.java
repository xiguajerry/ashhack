package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ConsumeItemEvent extends Event {
   private final ItemStack activeItemStack;

   public ConsumeItemEvent(ItemStack activeItemStack) {
      this.activeItemStack = activeItemStack;
   }

   public ItemStack getStack() {
      return this.activeItemStack;
   }

   public Item getItem() {
      return this.activeItemStack.getItem();
   }
}
