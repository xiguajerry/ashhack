package dev.realme.ash.impl.event.gui;

import dev.realme.ash.api.event.Event;
import net.minecraft.client.util.math.MatrixStack;

public class RenderScreenEvent extends Event {
   public final MatrixStack matrixStack;

   public RenderScreenEvent(MatrixStack matrixStack) {
      this.matrixStack = matrixStack;
   }
}
