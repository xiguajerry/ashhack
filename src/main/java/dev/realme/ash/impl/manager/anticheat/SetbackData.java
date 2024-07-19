package dev.realme.ash.impl.manager.anticheat;

import net.minecraft.util.math.Vec3d;

public record SetbackData(Vec3d position, long timeMS, int teleportID) {
   public SetbackData(Vec3d position, long timeMS, int teleportID) {
      this.position = position;
      this.timeMS = timeMS;
      this.teleportID = teleportID;
   }

   public long timeSince() {
      return System.currentTimeMillis() - this.timeMS;
   }

   public Vec3d position() {
      return this.position;
   }

   public long timeMS() {
      return this.timeMS;
   }

   public int teleportID() {
      return this.teleportID;
   }
}
