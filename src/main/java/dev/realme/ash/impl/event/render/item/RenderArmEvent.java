package dev.realme.ash.impl.event.render.item;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

@Cancelable
public class RenderArmEvent extends Event {
   public final MatrixStack matrices;
   public final VertexConsumerProvider vertexConsumers;
   public final int light;
   public final Arm arm;
   public final float equipProgress;
   public final float swingProgress;
   public final PlayerEntityRenderer playerEntityRenderer;

   public RenderArmEvent(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, PlayerEntityRenderer playerEntityRenderer) {
      this.matrices = matrices;
      this.vertexConsumers = vertexConsumers;
      this.light = light;
      this.equipProgress = equipProgress;
      this.swingProgress = swingProgress;
      this.arm = arm;
      this.playerEntityRenderer = playerEntityRenderer;
   }
}
