package dev.realme.ash.mixin.render;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.network.ReachEvent;
import dev.realme.ash.impl.event.render.BobViewEvent;
import dev.realme.ash.impl.event.render.FovEvent;
import dev.realme.ash.impl.event.render.HurtCamEvent;
import dev.realme.ash.impl.event.render.RenderBlockOutlineEvent;
import dev.realme.ash.impl.event.render.RenderFloatingItemEvent;
import dev.realme.ash.impl.event.render.RenderNauseaEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.impl.event.render.TargetEntityEvent;
import dev.realme.ash.impl.event.world.UpdateCrosshairTargetEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.init.Programs;
import dev.realme.ash.util.Globals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({GameRenderer.class})
public class MixinGameRenderer implements Globals {
   @Shadow
   @Final
   MinecraftClient client;
   @Shadow
   private float lastFovMultiplier;
   @Shadow
   private float fovMultiplier;
   @Shadow
   private float zoom;
   @Shadow
   private float zoomX;
   @Shadow
   private float zoomY;
   @Shadow
   private float viewDistance;

   @Inject(
      method = {"renderWorld"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
   ordinal = 1
)}
   )
   private void hookRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
      RenderWorldEvent.Game renderWorldEvent = new RenderWorldEvent.Game(matrices, tickDelta);
      Ash.EVENT_HANDLER.dispatch(renderWorldEvent);
   }

   @Inject(
      method = {"updateTargetedEntity"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
   shift = Shift.AFTER
)}
   )
   private void hookUpdateTargetedEntity$1(float tickDelta, CallbackInfo info) {
      UpdateCrosshairTargetEvent event = new UpdateCrosshairTargetEvent(tickDelta, this.client.getCameraEntity());
      Ash.EVENT_HANDLER.dispatch(event);
   }

   @Inject(
      method = {"getBasicProjectionMatrix"},
      at = {@At("TAIL")},
      cancellable = true
   )
   public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable cir) {
      if (Modules.CLIENT_SETTING.aspectRatio.getValue()) {
         MatrixStack matrixStack = new MatrixStack();
         matrixStack.peek().getPositionMatrix().identity();
         if (this.zoom != 1.0F) {
            matrixStack.translate(this.zoomX, -this.zoomY, 0.0F);
            matrixStack.scale(this.zoom, this.zoom, 1.0F);
         }

         matrixStack.peek().getPositionMatrix().mul((new Matrix4f()).setPerspective((float)(fov * 0.01745329238474369), Modules.CLIENT_SETTING.ratio.getValue(), 0.05F, this.viewDistance * 4.0F));
         cir.setReturnValue(matrixStack.peek().getPositionMatrix());
      }

   }

   @Inject(
      method = {"tiltViewWhenHurt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      HurtCamEvent hurtCamEvent = new HurtCamEvent();
      Ash.EVENT_HANDLER.dispatch(hurtCamEvent);
      if (hurtCamEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"showFloatingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookShowFloatingItem(ItemStack floatingItem, CallbackInfo ci) {
      RenderFloatingItemEvent renderFloatingItemEvent = new RenderFloatingItemEvent(floatingItem);
      Ash.EVENT_HANDLER.dispatch(renderFloatingItemEvent);
      if (renderFloatingItemEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderNausea"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRenderNausea(DrawContext context, float distortionStrength, CallbackInfo ci) {
      RenderNauseaEvent renderNauseaEvent = new RenderNauseaEvent();
      Ash.EVENT_HANDLER.dispatch(renderNauseaEvent);
      if (renderNauseaEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"shouldRenderBlockOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookShouldRenderBlockOutline(CallbackInfoReturnable cir) {
      RenderBlockOutlineEvent renderBlockOutlineEvent = new RenderBlockOutlineEvent();
      Ash.EVENT_HANDLER.dispatch(renderBlockOutlineEvent);
      if (renderBlockOutlineEvent.isCanceled()) {
         cir.setReturnValue(false);
         cir.cancel();
      }

   }

   @Inject(
      method = {"updateTargetedEntity"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"
)},
      cancellable = true
   )
   private void hookUpdateTargetedEntity$2(float tickDelta, CallbackInfo info) {
      TargetEntityEvent targetEntityEvent = new TargetEntityEvent();
      Ash.EVENT_HANDLER.dispatch(targetEntityEvent);
      if (targetEntityEvent.isCanceled() && this.client.crosshairTarget.getType() == Type.BLOCK) {
         this.client.getProfiler().pop();
         info.cancel();
      }

   }

   @ModifyConstant(
      method = {"updateTargetedEntity"},
      constant = {@Constant(
   doubleValue = 9.0
)}
   )
   private double updateTargetedEntityModifySquaredMaxReach(double d) {
      ReachEvent reachEvent = new ReachEvent();
      Ash.EVENT_HANDLER.dispatch(reachEvent);
      double reach = (double)reachEvent.getReach() + 3.0;
      return reachEvent.isCanceled() ? reach * reach : 9.0;
   }

   @Inject(
      method = {"bobView"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      BobViewEvent bobViewEvent = new BobViewEvent();
      Ash.EVENT_HANDLER.dispatch(bobViewEvent);
      if (bobViewEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"getFov"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable cir) {
      FovEvent fovEvent = new FovEvent();
      Ash.EVENT_HANDLER.dispatch(fovEvent);
      if (fovEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(fovEvent.getFov() * (double)MathHelper.lerp(tickDelta, this.lastFovMultiplier, this.fovMultiplier));
      }

   }

   @Inject(
      method = {"loadPrograms"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
   ordinal = 0
)},
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void initPrograms(ResourceFactory factory, CallbackInfo ci) {
      Programs.initPrograms();
   }
}
