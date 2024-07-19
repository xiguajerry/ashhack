package dev.realme.ash.mixin.render;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.TickCounterEvent;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({RenderTickCounter.class})
public class MixinRenderTickCounter {
   @Shadow
   private float lastFrameDuration;
   @Shadow
   private float tickDelta;
   @Shadow
   private long prevTimeMillis;
   @Shadow
   private float tickTime;

   @Inject(
      method = {"beginRenderTick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookBeginRenderTick(long timeMillis, CallbackInfoReturnable cir) {
      TickCounterEvent tickCounterEvent = new TickCounterEvent();
      Ash.EVENT_HANDLER.dispatch(tickCounterEvent);
      if (tickCounterEvent.isCanceled()) {
         this.lastFrameDuration = (float)(timeMillis - this.prevTimeMillis) / this.tickTime * tickCounterEvent.getTicks();
         this.prevTimeMillis = timeMillis;
         this.tickDelta += this.lastFrameDuration;
         int i = (int)this.tickDelta;
         this.tickDelta -= (float)i;
         cir.setReturnValue(i);
      }

   }
}
