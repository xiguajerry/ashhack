// Decompiled with: CFR 0.152
// Class Version: 17
package dev.realme.ash.impl.manager.network;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.impl.imixin.IClientPlayNetworkHandler;
import dev.realme.ash.mixin.accessor.AccessorClientWorld;
import dev.realme.ash.util.Globals;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.Packet;

public class NetworkManager
        implements Globals {
   private static final Set<Packet<?>> PACKET_CACHE = new HashSet();
   private ServerAddress address;
   private ServerInfo info;

   public NetworkManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   @EventListener
   public void onDisconnect(DisconnectEvent event) {
      PACKET_CACHE.clear();
   }

   public void sendPacket(Packet<?> p) {
      if (mc.getNetworkHandler() != null) {
         PACKET_CACHE.add(p);
         mc.getNetworkHandler().sendPacket(p);
      }
   }

   public void sendQuietPacket(Packet<?> p) {
      if (mc.getNetworkHandler() != null) {
         PACKET_CACHE.add(p);
         ((IClientPlayNetworkHandler) mc.getNetworkHandler()).sendQuietPacket(p);
      }
   }

   public void sendSequencedPacket(SequencedPacketCreator p) {
      if (NetworkManager.mc.world != null) {
         PendingUpdateManager updater = ((AccessorClientWorld) NetworkManager.mc.world).hookGetPendingUpdateManager().incrementSequence();
         try {
            int i = updater.getSequence();
            Packet packet = p.predict(i);
            this.sendPacket(packet);
         }
         catch (Throwable e) {
            e.printStackTrace();
            if (updater != null) {
               try {
                  updater.close();
               }
               catch (Throwable e1) {
                  e1.printStackTrace();
                  e.addSuppressed(e1);
               }
            }
            throw e;
         }
         updater.close();
      }
   }

   public int getClientLatency() {
      PlayerListEntry playerEntry;
      if (mc.getNetworkHandler() != null && (playerEntry = mc.getNetworkHandler().getPlayerListEntry(NetworkManager.mc.player.getGameProfile().getId())) != null) {
         return playerEntry.getLatency();
      }
      return 0;
   }

   public ServerAddress getAddress() {
      return this.address;
   }

   public void setAddress(ServerAddress address) {
      this.address = address;
   }

   public ServerInfo getInfo() {
      return this.info;
   }

   public void setInfo(ServerInfo info) {
      this.info = info;
   }

   public boolean isCrystalPvpCC() {
      if (this.info != null) {
         return this.info.address.equalsIgnoreCase("us.crystalpvp.cc") || this.info.address.equalsIgnoreCase("crystalpvp.cc");
      }
      return false;
   }

   public boolean isGrimCC() {
      return this.info != null && this.info.address.equalsIgnoreCase("grim.crystalpvp.cc");
   }

   public boolean isCached(Packet<?> p) {
      return PACKET_CACHE.contains(p);
   }
}
