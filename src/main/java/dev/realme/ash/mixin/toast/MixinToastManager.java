package dev.realme.ash.mixin.toast;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.toast.RenderToastEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ToastManager.class})
public class MixinToastManager {
   @Inject(
      method = {"draw"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookDraw(DrawContext context, CallbackInfo ci) {
      RenderToastEvent renderToastEvent = new RenderToastEvent();
      Ash.EVENT_HANDLER.dispatch(renderToastEvent);
      if (renderToastEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
