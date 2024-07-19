package dev.realme.ash.mixin.render;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.AmbientColorEvent;
import dev.realme.ash.impl.event.render.LightmapGammaEvent;
import java.awt.Color;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({LightmapTextureManager.class})
public class MixinLightmapTextureManager {
   @Shadow
   @Final
   private NativeImage image;

   @ModifyArgs(
      method = {"update"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"
)
   )
   private void hookUpdate(Args args) {
      LightmapGammaEvent lightmapGammaEvent = new LightmapGammaEvent((Integer)args.get(2));
      Ash.EVENT_HANDLER.dispatch(lightmapGammaEvent);
      if (lightmapGammaEvent.isCanceled()) {
         args.set(2, lightmapGammaEvent.getGamma());
      }

   }

   @Inject(
      method = {"update"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/texture/NativeImageBackedTexture;upload()V",
   shift = Shift.BEFORE
)}
   )
   private void hookUpdate(float delta, CallbackInfo ci) {
      AmbientColorEvent ambientColorEvent = new AmbientColorEvent();
      Ash.EVENT_HANDLER.dispatch(ambientColorEvent);
      if (ambientColorEvent.isCanceled()) {
         for(int i = 0; i < 16; ++i) {
            for(int j = 0; j < 16; ++j) {
               int color = this.image.getColor(i, j);
               int[] bgr = new int[]{color >> 16 & 255, color >> 8 & 255, color & 255};
               Vec3d colors = new Vec3d((double)bgr[2] / 255.0, (double)bgr[1] / 255.0, (double)bgr[0] / 255.0);
               Color c = ambientColorEvent.getColor();
               Vec3d ncolors = new Vec3d((double)c.getRed() / 255.0, (double)c.getGreen() / 255.0, (double)c.getBlue() / 255.0);
               Vec3d mix = this.mix(colors, ncolors, (double)c.getAlpha() / 255.0);
               int r = (int)(mix.x * 255.0);
               int g = (int)(mix.y * 255.0);
               int b = (int)(mix.z * 255.0);
               this.image.setColor(i, j, -16777216 | r << 16 | g << 8 | b);
            }
         }
      }

   }

   private Vec3d mix(Vec3d first, Vec3d second, double factor) {
      return new Vec3d(first.x * (1.0 - factor) + second.x * factor, first.y * (1.0 - factor) + second.y * factor, first.z * (1.0 - factor) + first.z * factor);
   }
}
