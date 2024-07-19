package dev.realme.ash.impl.event.render.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;

@Cancelable
public class RenderCrystalEvent extends Event {
   public final EndCrystalEntity endCrystalEntity;
   public final float f;
   public final float g;
   public final MatrixStack matrixStack;
   public final int i;
   public final ModelPart core;
   public final ModelPart frame;

   public RenderCrystalEvent(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, int i, ModelPart core, ModelPart frame) {
      this.endCrystalEntity = endCrystalEntity;
      this.f = f;
      this.g = g;
      this.matrixStack = matrixStack;
      this.i = i;
      this.core = core;
      this.frame = frame;
   }
}
