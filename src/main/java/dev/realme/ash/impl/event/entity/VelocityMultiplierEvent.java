package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

@Cancelable
public class VelocityMultiplierEvent extends Event {
   private final BlockState state;

   public VelocityMultiplierEvent(BlockState state) {
      this.state = state;
   }

   public Block getBlock() {
      return this.state.getBlock();
   }
}
