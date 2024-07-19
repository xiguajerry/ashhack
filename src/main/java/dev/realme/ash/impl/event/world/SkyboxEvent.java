package dev.realme.ash.impl.event.world;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import java.awt.Color;
import net.minecraft.util.math.Vec3d;

public class SkyboxEvent extends Event {
   private Color color;

   public Color getColor() {
      return this.color;
   }

   public Vec3d getColorVec() {
      return new Vec3d((double)this.color.getRed() / 255.0, (double)this.color.getGreen() / 255.0, (double)this.color.getBlue() / 255.0);
   }

   public int getRGB() {
      return this.color.getRGB();
   }

   public void setColor(Color color) {
      this.color = color;
   }

   @Cancelable
   public static class Fog extends SkyboxEvent {
      private final float tickDelta;

      public Fog(float tickDelta) {
         this.tickDelta = tickDelta;
      }

      public float getTickDelta() {
         return this.tickDelta;
      }
   }

   @Cancelable
   public static class Cloud extends SkyboxEvent {
   }

   @Cancelable
   public static class Sky extends SkyboxEvent {
   }
}
