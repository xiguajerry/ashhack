package dev.realme.ash.impl.event.network;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.util.math.BlockPos;

@Cancelable
public class BreakBlockEvent extends Event {
   private final BlockPos pos;

   public BreakBlockEvent(BlockPos pos) {
      this.pos = pos;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
