package dev.realme.ash.impl.event.render;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class FovEvent extends Event {
   private double fov;

   public double getFov() {
      return this.fov;
   }

   public void setFov(double fov) {
      this.fov = fov;
   }
}
