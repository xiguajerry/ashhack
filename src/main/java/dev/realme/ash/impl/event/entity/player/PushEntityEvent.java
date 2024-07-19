package dev.realme.ash.impl.event.entity.player;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.Entity;

@Cancelable
public class PushEntityEvent extends Event {
   private final Entity pushed;
   private final Entity pusher;

   public PushEntityEvent(Entity pushed, Entity pusher) {
      this.pushed = pushed;
      this.pusher = pusher;
   }

   public Entity getPushed() {
      return this.pushed;
   }

   public Entity getPusher() {
      return this.pusher;
   }
}
