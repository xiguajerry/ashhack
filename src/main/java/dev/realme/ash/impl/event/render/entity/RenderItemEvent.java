package dev.realme.ash.impl.event.render.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.ItemEntity;

@Cancelable
public class RenderItemEvent extends Event {
   private final ItemEntity itemEntity;

   public RenderItemEvent(ItemEntity itemEntity) {
      this.itemEntity = itemEntity;
   }

   public ItemEntity getItem() {
      return this.itemEntity;
   }
}
