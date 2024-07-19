package dev.realme.ash.mixin.render.entity;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.entity.RenderPlayerEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerEntityRenderer.class})
public class MixinPlayerEntityRenderer {
   @Unique
   private float yaw;
   @Unique
   private float prevYaw;
   @Unique
   private float bodyYaw;
   @Unique
   private float prevBodyYaw;
   @Unique
   private float headYaw;
   @Unique
   private float prevHeadYaw;
   @Unique
   private float pitch;
   @Unique
   private float prevPitch;

   @Inject(
      method = {"render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render /VertexConsumerProvider;I)V"},
      at = {@At("HEAD")}
   )
   private void onRenderHead(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      RenderPlayerEvent renderPlayerEvent = new RenderPlayerEvent(abstractClientPlayerEntity);
      Ash.EVENT_HANDLER.dispatch(renderPlayerEvent);
      this.yaw = abstractClientPlayerEntity.getYaw();
      this.prevYaw = abstractClientPlayerEntity.prevYaw;
      this.bodyYaw = abstractClientPlayerEntity.bodyYaw;
      this.prevBodyYaw = abstractClientPlayerEntity.prevBodyYaw;
      this.headYaw = abstractClientPlayerEntity.headYaw;
      this.prevHeadYaw = abstractClientPlayerEntity.prevHeadYaw;
      this.pitch = abstractClientPlayerEntity.getPitch();
      this.prevPitch = abstractClientPlayerEntity.prevPitch;
      if (renderPlayerEvent.isCanceled()) {
         abstractClientPlayerEntity.setYaw(renderPlayerEvent.getYaw());
         abstractClientPlayerEntity.prevYaw = renderPlayerEvent.getYaw();
         abstractClientPlayerEntity.setBodyYaw(renderPlayerEvent.getYaw());
         abstractClientPlayerEntity.prevBodyYaw = renderPlayerEvent.getYaw();
         abstractClientPlayerEntity.setHeadYaw(renderPlayerEvent.getYaw());
         abstractClientPlayerEntity.prevHeadYaw = renderPlayerEvent.getYaw();
         abstractClientPlayerEntity.setPitch(renderPlayerEvent.getPitch());
         abstractClientPlayerEntity.prevPitch = renderPlayerEvent.getPitch();
      }

   }

   @Inject(
      method = {"render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render /VertexConsumerProvider;I)V"},
      at = {@At("TAIL")}
   )
   private void onRenderTail(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      abstractClientPlayerEntity.setYaw(this.yaw);
      abstractClientPlayerEntity.prevYaw = this.prevYaw;
      abstractClientPlayerEntity.setBodyYaw(this.bodyYaw);
      abstractClientPlayerEntity.prevBodyYaw = this.prevBodyYaw;
      abstractClientPlayerEntity.setHeadYaw(this.headYaw);
      abstractClientPlayerEntity.prevHeadYaw = this.prevHeadYaw;
      abstractClientPlayerEntity.setPitch(this.pitch);
      abstractClientPlayerEntity.prevPitch = this.prevPitch;
   }
}
