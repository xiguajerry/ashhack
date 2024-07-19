package dev.realme.ash.mixin;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.impl.event.keyboard.KeyboardTickEvent;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public class MixinKeyboardInput {
   @Inject(
      method = {"tick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookTick$Pre(boolean slowDown, float slowDownFactor, CallbackInfo info) {
      KeyboardTickEvent event = new KeyboardTickEvent((Input)(Object)this);
      event.setStage(EventStage.PRE);
      Ash.EVENT_HANDLER.dispatch(event);
      if (event.isCanceled()) {
         info.cancel();
      }

   }

   @Inject(
      method = {"tick"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/input/KeyboardInput;sneaking:Z",
   shift = Shift.BEFORE
)},
      cancellable = true
   )
   private void hookTick$Post(boolean slowDown, float f, CallbackInfo ci) {
      KeyboardTickEvent keyboardTickEvent = new KeyboardTickEvent((Input)(Object)this);
      keyboardTickEvent.setStage(EventStage.POST);
      Ash.EVENT_HANDLER.dispatch(keyboardTickEvent);
      if (keyboardTickEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
