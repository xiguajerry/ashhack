package dev.realme.ash.mixin.gui.hud;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.gui.hud.RenderOverlayEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameOverlayRenderer.class})
public class MixinInGameOverlayRenderer {
   @Inject(
      method = {"renderFireOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void hookRenderFireOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
      RenderOverlayEvent.Fire renderOverlayEvent = new RenderOverlayEvent.Fire(null);
      Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
      if (renderOverlayEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderUnderwaterOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void hookRenderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
      RenderOverlayEvent.Water renderOverlayEvent = new RenderOverlayEvent.Water(null);
      Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
      if (renderOverlayEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderInWallOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void hookRenderFireOverlay(Sprite sprite, MatrixStack matrices, CallbackInfo ci) {
      RenderOverlayEvent.Block renderOverlayEvent = new RenderOverlayEvent.Block(null);
      Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
      if (renderOverlayEvent.isCanceled()) {
         ci.cancel();
      }

   }
}
