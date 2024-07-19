package dev.realme.ash.mixin.render.item;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.render.item.RenderArmEvent;
import dev.realme.ash.impl.event.render.item.RenderFirstPersonEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HeldItemRenderer.class})
public class MixinHeldItemRenderer {
   @Shadow
   @Final
   private EntityRenderDispatcher entityRenderDispatcher;
   @Shadow
   @Final
   private MinecraftClient client;

   @Inject(
      method = {"renderArmHoldingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
      PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer)this.entityRenderDispatcher.getRenderer(this.client.player);
      RenderArmEvent renderArmEvent = new RenderArmEvent(matrices, vertexConsumers, light, equipProgress, swingProgress, arm, playerEntityRenderer);
      Ash.EVENT_HANDLER.dispatch(renderArmEvent);
      if (renderArmEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
)}
   )
   private void hookRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      RenderFirstPersonEvent renderFirstPersonEvent = new RenderFirstPersonEvent(hand, item, equipProgress, matrices);
      Ash.EVENT_HANDLER.dispatch(renderFirstPersonEvent);
   }
}
