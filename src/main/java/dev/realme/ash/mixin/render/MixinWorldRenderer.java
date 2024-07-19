package dev.realme.ash.mixin.render;

import dev.realme.ash.Ash;
import dev.realme.ash.api.render.RenderBuffers;
import dev.realme.ash.impl.event.PerspectiveEvent;
import dev.realme.ash.impl.event.render.RenderWorldBorderEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.util.Globals;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldRenderer.class})
public class MixinWorldRenderer implements Globals {
   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void hookRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
      Vec3d pos = mc.getBlockEntityRenderDispatcher().camera.getPos();
      matrices.translate(-pos.x, -pos.y, -pos.z);
      RenderBuffers.preRender();
      RenderWorldEvent renderWorldEvent = new RenderWorldEvent(matrices, tickDelta);
      Ash.EVENT_HANDLER.dispatch(renderWorldEvent);
      RenderBuffers.postRender();
   }

   @Inject(
      method = {"renderWorldBorder"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRenderWorldBorder(Camera camera, CallbackInfo ci) {
      RenderWorldBorderEvent renderWorldBorderEvent = new RenderWorldBorderEvent();
      Ash.EVENT_HANDLER.dispatch(renderWorldBorderEvent);
      if (renderWorldBorderEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"
)
   )
   public boolean hookRender(Camera instance) {
      PerspectiveEvent perspectiveEvent = new PerspectiveEvent(instance);
      Ash.EVENT_HANDLER.dispatch(perspectiveEvent);
      return perspectiveEvent.isCanceled() ? true : instance.isThirdPerson();
   }
}
