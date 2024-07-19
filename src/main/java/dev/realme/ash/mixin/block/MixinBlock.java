package dev.realme.ash.mixin.block;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.block.BlockSlipperinessEvent;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Block.class})
public class MixinBlock {
   @Inject(
      method = {"getSlipperiness"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void hookGetSlipperiness(CallbackInfoReturnable cir) {
      BlockSlipperinessEvent blockSlipperinessEvent = new BlockSlipperinessEvent((Block)((Object) this), cir.getReturnValueF());
      Ash.EVENT_HANDLER.dispatch(blockSlipperinessEvent);
      if (blockSlipperinessEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(blockSlipperinessEvent.getSlipperiness());
      }

   }
}
