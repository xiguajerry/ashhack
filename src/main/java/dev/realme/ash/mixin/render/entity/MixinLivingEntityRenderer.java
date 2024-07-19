package dev.realme.ash.mixin.render.entity;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.entity.RenderEntityEvent;
import dev.realme.ash.impl.event.render.entity.RenderEntityInvisibleEvent;
import dev.realme.ash.impl.manager.player.rotation.RotationManager;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public abstract class MixinLivingEntityRenderer {
   @Shadow
   protected EntityModel model;
   @Shadow
   @Final
   protected List features;
   @Unique
   private LivingEntity lastEntity;
   @Unique
   private float originalYaw;
   @Unique
   private float originalHeadYaw;
   @Unique
   private float originalBodyYaw;
   @Unique
   private float originalPitch;
   @Unique
   private float originalPrevYaw;
   @Unique
   private float originalPrevHeadYaw;
   @Unique
   private float originalPrevBodyYaw;

   @Shadow
   protected abstract RenderLayer getRenderLayer(LivingEntity var1, boolean var2, boolean var3, boolean var4);

   @Inject(
      method = {"render*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRender(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      RenderEntityEvent renderEntityEvent = new RenderEntityEvent(livingEntity, f, g, matrixStack, vertexConsumerProvider, i, this.model, this.getRenderLayer(livingEntity, true, false, false), this.features);
      Ash.EVENT_HANDLER.dispatch(renderEntityEvent);
      if (renderEntityEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Redirect(
      method = {"render*"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"
)
   )
   private boolean redirectRender$isInvisibleTo(LivingEntity entity, PlayerEntity player) {
      RenderEntityInvisibleEvent event = new RenderEntityInvisibleEvent(entity);
      Ash.EVENT_HANDLER.dispatch(event);
      return event.isCanceled() ? false : entity.isInvisibleTo(player);
   }

   @Inject(
      method = {"render*"},
      at = {@At("HEAD")}
   )
   public void onRenderPre(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player) {
         this.originalYaw = livingEntity.getYaw();
         this.originalHeadYaw = livingEntity.headYaw;
         this.originalBodyYaw = livingEntity.bodyYaw;
         this.originalPitch = livingEntity.getPitch();
         this.originalPrevYaw = livingEntity.prevYaw;
         this.originalPrevHeadYaw = livingEntity.prevHeadYaw;
         this.originalPrevBodyYaw = livingEntity.prevBodyYaw;
         livingEntity.setYaw(RotationManager.getRenderYawOffset());
         livingEntity.headYaw = RotationManager.getRotationYawHead();
         livingEntity.bodyYaw = RotationManager.getRenderYawOffset();
         livingEntity.setPitch(RotationManager.getRenderPitch());
         livingEntity.prevYaw = RotationManager.getPrevRenderYawOffset();
         livingEntity.prevHeadYaw = RotationManager.getPrevRotationYawHead();
         livingEntity.prevBodyYaw = RotationManager.getPrevRenderYawOffset();
         livingEntity.prevPitch = RotationManager.getPrevPitch();
      }

      this.lastEntity = livingEntity;
   }

   @Inject(
      method = {"render*"},
      at = {@At("TAIL")}
   )
   public void onRenderPost(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player) {
         livingEntity.setYaw(this.originalYaw);
         livingEntity.headYaw = this.originalHeadYaw;
         livingEntity.bodyYaw = this.originalBodyYaw;
         livingEntity.setPitch(this.originalPitch);
         livingEntity.prevYaw = this.originalPrevYaw;
         livingEntity.prevHeadYaw = this.originalPrevHeadYaw;
         livingEntity.prevBodyYaw = this.originalPrevBodyYaw;
         livingEntity.prevPitch = this.originalPitch;
      }

   }
}
