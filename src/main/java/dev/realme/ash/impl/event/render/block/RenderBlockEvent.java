package dev.realme.ash.impl.event.render.block;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@Cancelable
public class RenderBlockEvent extends Event {
   private final BlockState state;
   private final BlockPos pos;

   public RenderBlockEvent(BlockState state, BlockPos pos) {
      this.state = state;
      this.pos = pos;
   }

   public BlockState getState() {
      return this.state;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Block getBlock() {
      return this.state.getBlock();
   }
}
