package dev.realme.ash.impl.event.render;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class CameraClipEvent extends Event {
   private double getDistance;

   public CameraClipEvent(double distance) {
      this.getDistance = distance;
   }

   public double getDistance() {
      return this.getDistance;
   }

   public void setDistance(double distance) {
      this.getDistance = distance;
   }
}
