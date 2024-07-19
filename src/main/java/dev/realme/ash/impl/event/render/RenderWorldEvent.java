package dev.realme.ash.impl.event.render;

import dev.realme.ash.api.event.Event;
import net.minecraft.client.util.math.MatrixStack;

public class RenderWorldEvent extends Event {
   private final MatrixStack matrices;
   private final float tickDelta;

   public RenderWorldEvent(MatrixStack matrices, float tickDelta) {
      this.matrices = matrices;
      this.tickDelta = tickDelta;
   }

   public MatrixStack getMatrices() {
      return this.matrices;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }

   public static class Game extends RenderWorldEvent {
      public Game(MatrixStack matrices, float tickDelta) {
         super(matrices, tickDelta);
      }
   }
}
