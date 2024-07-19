package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Event;
import net.minecraft.entity.player.PlayerEntity;

public class TotemPopEvent extends Event {
   private final PlayerEntity entity;

   public TotemPopEvent(PlayerEntity entity) {
      this.entity = entity;
   }

   public PlayerEntity getEntity() {
      return this.entity;
   }
}
