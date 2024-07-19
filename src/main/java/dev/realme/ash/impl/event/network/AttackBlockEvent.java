package dev.realme.ash.impl.event.network;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Cancelable
public class AttackBlockEvent extends Event {
   private final BlockPos pos;
   private final BlockState state;
   private final Direction direction;

   public AttackBlockEvent(BlockPos pos, BlockState state, Direction direction) {
      this.pos = pos;
      this.state = state;
      this.direction = direction;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public BlockState getState() {
      return this.state;
   }

   public Direction getDirection() {
      return this.direction;
   }
}
