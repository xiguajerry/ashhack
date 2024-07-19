package dev.realme.ash.mixin.gui.screen;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.gui.RenderTooltipEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HandledScreen.class})
public class MixinHandledScreen {
   @Shadow
   protected @Nullable Slot focusedSlot;

   @Inject(
      method = {"drawMouseoverTooltip"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
      if (this.focusedSlot != null) {
         RenderTooltipEvent renderTooltipEvent = new RenderTooltipEvent(context, this.focusedSlot.getStack(), x, y);
         Ash.EVENT_HANDLER.dispatch(renderTooltipEvent);
         if (renderTooltipEvent.isCanceled()) {
            ci.cancel();
         }

      }
   }
}
