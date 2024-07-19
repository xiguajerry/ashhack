package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.world.event.GameEvent;

public class EntityGameEvent extends Event {
   private final GameEvent gameEvent;
   private final Entity entity;

   public EntityGameEvent(GameEvent gameEvent, Entity entity) {
      this.gameEvent = gameEvent;
      this.entity = entity;
   }

   public GameEvent getGameEvent() {
      return this.gameEvent;
   }

   public Entity getEntity() {
      return this.entity;
   }
}
