package dev.realme.ash.impl.manager.anticheat;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.util.Globals;
import java.util.Arrays;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public final class AntiCheatManager implements Globals {
   private SetbackData lastSetback;
   private final int[] transactions = new int[4];
   private int index;
   private boolean isGrim;

   public AntiCheatManager() {
      Ash.EVENT_HANDLER.subscribe(this);
      Arrays.fill(this.transactions, -1);
   }

   @EventListener
   public void onPacketInbound(PacketEvent.Receive event) {
      if (!Module.nullCheck()) {
         Packet var4 = event.getPacket();
         if (var4 instanceof CommonPingS2CPacket) {
            CommonPingS2CPacket packet = (CommonPingS2CPacket)var4;
            if (this.index > 3) {
               return;
            }

            int uid = packet.getParameter();
            this.transactions[this.index] = uid;
            ++this.index;
            if (this.index == 4) {
               this.grimCheck();
            }
         } else {
            var4 = event.getPacket();
            if (var4 instanceof PlayerPositionLookS2CPacket) {
               PlayerPositionLookS2CPacket packet = (PlayerPositionLookS2CPacket)var4;
               this.lastSetback = new SetbackData(new Vec3d(packet.getX(), packet.getY(), packet.getZ()), System.currentTimeMillis(), packet.getTeleportId());
            }
         }

      }
   }

   @EventListener
   public void onDisconnect(DisconnectEvent event) {
      Arrays.fill(this.transactions, -1);
      this.index = 0;
      this.isGrim = false;
   }

   private void grimCheck() {
      for(int i = 0; i < 4 && this.transactions[i] == -i; ++i) {
      }

      this.isGrim = true;
      Ash.LOGGER.info("Server is running GrimAC.");
   }

   public boolean isGrim() {
      return this.isGrim;
   }

   public boolean hasPassed(long timeMS) {
      return this.lastSetback != null && this.lastSetback.timeSince() >= timeMS;
   }
}
