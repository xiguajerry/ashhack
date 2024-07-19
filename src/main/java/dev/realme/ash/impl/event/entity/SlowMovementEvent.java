package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.block.BlockState;

@Cancelable
public class SlowMovementEvent extends Event {
   private final BlockState state;

   public SlowMovementEvent(BlockState state) {
      this.state = state;
   }

   public BlockState getState() {
      return this.state;
   }
}
