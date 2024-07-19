package dev.realme.ash.mixin.gui.hud;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.gui.hud.RenderOverlayEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BossBarHud.class})
public class MixinBossBarHud {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRender(DrawContext context, CallbackInfo ci) {
      RenderOverlayEvent.BossBar renderOverlayEvent = new RenderOverlayEvent.BossBar(context);
      Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
      if (renderOverlayEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
