package dev.realme.ash.mixin.block;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.block.SteppedOnSlimeBlockEvent;
import dev.realme.ash.util.Globals;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SlimeBlock.class})
public class MixinSlimeBlock implements Globals {
   @Inject(
      method = {"onSteppedOn"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookOnSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
      SteppedOnSlimeBlockEvent steppedOnSlimeBlockEvent = new SteppedOnSlimeBlockEvent();
      Ash.EVENT_HANDLER.dispatch(steppedOnSlimeBlockEvent);
      if (steppedOnSlimeBlockEvent.isCanceled() && entity == mc.player) {
         ci.cancel();
      }

   }
}
