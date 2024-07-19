package dev.realme.ash.impl.event.entity.projectile;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.entity.projectile.FireworkRocketEntity;

@Cancelable
public class RemoveFireworkEvent extends Event {
   private final FireworkRocketEntity rocketEntity;

   public RemoveFireworkEvent(FireworkRocketEntity rocketEntity) {
      this.rocketEntity = rocketEntity;
   }

   public FireworkRocketEntity getRocketEntity() {
      return this.rocketEntity;
   }
}
