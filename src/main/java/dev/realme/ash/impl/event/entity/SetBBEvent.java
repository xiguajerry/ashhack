package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Event;
import net.minecraft.util.math.Box;

public class SetBBEvent extends Event {
   private final Box boundingBox;

   public SetBBEvent(Box boundingBox) {
      this.boundingBox = boundingBox;
   }

   public Box getBoundingBox() {
      return this.boundingBox;
   }
}
