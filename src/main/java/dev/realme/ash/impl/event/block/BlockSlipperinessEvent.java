package dev.realme.ash.impl.event.block;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.block.Block;

@Cancelable
public class BlockSlipperinessEvent extends Event {
   private final Block block;
   private float slipperiness;

   public BlockSlipperinessEvent(Block block, float slipperiness) {
      this.block = block;
      this.slipperiness = slipperiness;
   }

   public Block getBlock() {
      return this.block;
   }

   public float getSlipperiness() {
      return this.slipperiness;
   }

   public void setSlipperiness(float slipperiness) {
      this.slipperiness = slipperiness;
   }
}
