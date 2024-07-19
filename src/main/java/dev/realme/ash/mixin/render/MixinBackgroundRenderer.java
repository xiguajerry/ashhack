package dev.realme.ash.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.RenderFogEvent;
import dev.realme.ash.impl.event.world.BlindnessEvent;
import dev.realme.ash.impl.event.world.SkyboxEvent;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BackgroundRenderer.class})
public class MixinBackgroundRenderer {
   @Shadow
   private static float red;
   @Shadow
   private static float green;
   @Shadow
   private static float blue;

   @Inject(
      method = {"applyFog"},
      at = {@At("TAIL")}
   )
   private static void hookApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
      if (fogType == FogType.FOG_TERRAIN) {
         RenderFogEvent renderFogEvent = new RenderFogEvent();
         Ash.EVENT_HANDLER.dispatch(renderFogEvent);
         if (renderFogEvent.isCanceled()) {
            RenderSystem.setShaderFogStart(viewDistance * 4.0F);
            RenderSystem.setShaderFogEnd(viewDistance * 4.25F);
         }

      }
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void hookRender(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
      SkyboxEvent.Fog skyboxEvent = new SkyboxEvent.Fog(tickDelta);
      Ash.EVENT_HANDLER.dispatch(skyboxEvent);
      if (skyboxEvent.isCanceled()) {
         ci.cancel();
         Vec3d vec3d = skyboxEvent.getColorVec();
         red = (float)vec3d.x;
         green = (float)vec3d.y;
         blue = (float)vec3d.z;
         RenderSystem.clearColor(red, green, blue, 0.0F);
      }

   }

   @Inject(
      method = {"getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable cir) {
      BlindnessEvent blindnessEvent = new BlindnessEvent();
      Ash.EVENT_HANDLER.dispatch(blindnessEvent);
      if (blindnessEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(null);
      }

   }
}
