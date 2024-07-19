package dev.realme.ash.impl.event.render.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import java.util.List;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

@Cancelable
public class RenderEntityEvent extends Event {
   public final LivingEntity entity;
   public final float f;
   public final float g;
   public final MatrixStack matrixStack;
   public final VertexConsumerProvider vertexConsumerProvider;
   public final int i;
   public final RenderLayer layer;
   public final EntityModel model;
   public final List<FeatureRenderer> features;

   public RenderEntityEvent(LivingEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, EntityModel model, RenderLayer layer, List features) {
      this.entity = entity;
      this.f = f;
      this.g = g;
      this.matrixStack = matrixStack;
      this.vertexConsumerProvider = vertexConsumerProvider;
      this.i = i;
      this.model = model;
      this.layer = layer;
      this.features = features;
   }
}
