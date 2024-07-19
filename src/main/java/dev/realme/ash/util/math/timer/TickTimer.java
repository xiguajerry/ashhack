package dev.realme.ash.util.math.timer;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.impl.event.TickEvent;

public class TickTimer implements Timer {
   private long ticks = 0L;

   public TickTimer() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (event.getStage() == EventStage.PRE) {
         ++this.ticks;
      }

   }

   public boolean passed(Number time) {
      return this.ticks >= time.longValue();
   }

   public void reset() {
      this.setElapsedTime(0);
   }

   public long getElapsedTime() {
      return this.ticks;
   }

   public void setElapsedTime(Number time) {
      this.ticks = time.longValue();
   }
}
