package dev.realme.ash.impl.event.block;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

@Cancelable
public class PlaceBlockEvent extends Event {
   private static final PlaceBlockEvent INSTANCE = new PlaceBlockEvent();
   public BlockPos blockPos;
   public Block block;

   public static PlaceBlockEvent get(BlockPos blockPos, Block block) {
      INSTANCE.blockPos = blockPos;
      INSTANCE.block = block;
      return INSTANCE;
   }
}
