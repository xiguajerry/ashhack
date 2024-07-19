package dev.realme.ash.impl.event.network;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import dev.realme.ash.init.Managers;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends Event {
   private final Packet packet;

   public PacketEvent(Packet packet) {
      this.packet = packet;
   }

   public Packet getPacket() {
      return this.packet;
   }

   @Cancelable
   public static class Send extends PacketEvent {
      private final boolean cached;

      public Send(Packet packet) {
         super(packet);
         this.cached = Managers.NETWORK.isCached(packet);
      }

      public boolean isClientPacket() {
         return this.cached;
      }
   }

   @Cancelable
   public static class Receive extends PacketEvent {
      private final PacketListener packetListener;

      public Receive(PacketListener packetListener, Packet packet) {
         super(packet);
         this.packetListener = packetListener;
      }

      public PacketListener getPacketListener() {
         return this.packetListener;
      }
   }
}
