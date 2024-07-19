package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Event;

public class StepEvent extends Event {
   private final double stepHeight;

   public StepEvent(double stepHeight) {
      this.stepHeight = stepHeight;
   }

   public double getStepHeight() {
      return this.stepHeight;
   }
}
