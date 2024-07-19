package dev.realme.ash.mixin.entity.passive;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.entity.passive.EntitySteerEvent;
import net.minecraft.entity.passive.PigEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PigEntity.class})
public class MixinPigEntity {
   @Inject(
      method = {"isSaddled"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookIsSaddled(CallbackInfoReturnable cir) {
      EntitySteerEvent entitySteerEvent = new EntitySteerEvent();
      Ash.EVENT_HANDLER.dispatch(entitySteerEvent);
      if (entitySteerEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(true);
      }

   }
}
