package dev.realme.ash.mixin.gui.hud;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.gui.hud.RenderOverlayEvent;
import dev.realme.ash.util.Globals;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class MixinInGameHud implements Globals {
   @Shadow
   @Final
   private static Identifier PUMPKIN_BLUR;
   @Shadow
   @Final
   private static Identifier POWDER_SNOW_OUTLINE;

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void hookRender(DrawContext context, float tickDelta, CallbackInfo ci) {
      RenderOverlayEvent.Post renderOverlayEvent = new RenderOverlayEvent.Post(context, tickDelta);
      Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
   }

   @Inject(
      method = {"renderStatusEffectOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRenderStatusEffectOverlay(DrawContext context, CallbackInfo ci) {
      RenderOverlayEvent.StatusEffect renderOverlayEvent = new RenderOverlayEvent.StatusEffect(context);
      Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
      if (renderOverlayEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderSpyglassOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRenderSpyglassOverlay(DrawContext context, float scale, CallbackInfo ci) {
      RenderOverlayEvent.Spyglass renderOverlayEvent = new RenderOverlayEvent.Spyglass(context);
      Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
      if (renderOverlayEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRenderOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
      if (texture.getPath().equals(PUMPKIN_BLUR.getPath())) {
         RenderOverlayEvent.Pumpkin renderOverlayEvent = new RenderOverlayEvent.Pumpkin(context);
         Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
         if (renderOverlayEvent.isCanceled()) {
            ci.cancel();
         }
      } else if (texture.getPath().equals(POWDER_SNOW_OUTLINE.getPath())) {
         RenderOverlayEvent.Frostbite renderOverlayEvent = new RenderOverlayEvent.Frostbite(context);
         Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
         if (renderOverlayEvent.isCanceled()) {
            ci.cancel();
         }
      }

   }

   @Redirect(
      method = {"renderHeldItemTooltip"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
)
   )
   private int hookRenderHeldItemTooltip(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color) {
      RenderOverlayEvent.ItemName renderOverlayEvent = new RenderOverlayEvent.ItemName(instance);
      Ash.EVENT_HANDLER.dispatch(renderOverlayEvent);
      if (renderOverlayEvent.isCanceled()) {
         return renderOverlayEvent.isUpdateXY() ? instance.drawText(mc.textRenderer, text, renderOverlayEvent.getX(), renderOverlayEvent.getY(), color, true) : 0;
      } else {
         return instance.drawText(mc.textRenderer, text, x, y, color, true);
      }
   }
}
