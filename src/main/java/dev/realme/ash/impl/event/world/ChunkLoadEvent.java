package dev.realme.ash.impl.event.world;

import dev.realme.ash.api.event.StageEvent;
import net.minecraft.util.math.ChunkPos;

public class ChunkLoadEvent extends StageEvent {
   private final ChunkPos pos;

   public ChunkLoadEvent(ChunkPos pos) {
      this.pos = pos;
   }

   public ChunkPos getPos() {
      return this.pos;
   }
}
