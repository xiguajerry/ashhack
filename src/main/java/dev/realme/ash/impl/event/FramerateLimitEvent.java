package dev.realme.ash.impl.event;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class FramerateLimitEvent extends Event {
   private int framerateLimit;

   public int getFramerateLimit() {
      return this.framerateLimit;
   }

   public void setFramerateLimit(int framerateLimit) {
      this.framerateLimit = framerateLimit;
   }
}
