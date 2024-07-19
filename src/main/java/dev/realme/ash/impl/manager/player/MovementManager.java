package dev.realme.ash.impl.manager.player;

import dev.realme.ash.Ash;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import net.minecraft.entity.player.PlayerEntity;

public class MovementManager implements Globals {
   public MovementManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   public double getSpeed(PlayerEntity player) {
      if (player == null) {
         return 0.0;
      } else {
         double x = player.getX() - player.prevX;
         double z = player.getZ() - player.prevZ;
         double dist = Math.sqrt(x * x + z * z) / 1000.0;
         double div = 1.388888888888889E-5;
         float timer = Modules.TIMER.isEnabled() ? Modules.TIMER.getTimer() : 1.0F;
         return dist / div * (double)timer;
      }
   }
}
