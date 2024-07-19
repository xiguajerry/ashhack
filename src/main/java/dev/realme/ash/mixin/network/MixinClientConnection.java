package dev.realme.ash.mixin.network;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.init.Modules;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientConnection.class})
public class MixinClientConnection {
   @Shadow
   private volatile @Nullable PacketListener packetListener;
   @Shadow
   @Final
   private static Logger LOGGER;

   @Inject(
      method = {"exceptionCaught"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookExceptionCaught(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
      if (Modules.SERVER.isPacketKick()) {
         LOGGER.error("Exception caught on network thread:", ex);
         ci.cancel();
      }

   }

   @Inject(
      method = {"sendImmediately"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookSendImmediately(Packet packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
      PacketEvent.Send packetOutboundEvent = new PacketEvent.Send(packet);
      Ash.EVENT_HANDLER.dispatch(packetOutboundEvent);
      if (packetOutboundEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookChannelRead0(ChannelHandlerContext channelHandlerContext, Packet packet, CallbackInfo ci) {
      PacketEvent.Receive packetInboundEvent = new PacketEvent.Receive(this.packetListener, packet);
      Ash.EVENT_HANDLER.dispatch(packetInboundEvent);
      if (packetInboundEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"disconnect"},
      at = {@At("HEAD")}
   )
   private void hookDisconnect(Text disconnectReason, CallbackInfo ci) {
      DisconnectEvent disconnectEvent = new DisconnectEvent();
      Ash.EVENT_HANDLER.dispatch(disconnectEvent);
   }
}
