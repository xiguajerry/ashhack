package dev.realme.ash.impl.manager.tick;

public enum TickSync {
   CURRENT,
   AVERAGE,
   MINIMAL,
   NONE;

   // $FF: synthetic method
   private static TickSync[] $values() {
      return new TickSync[]{CURRENT, AVERAGE, MINIMAL, NONE};
   }
}
