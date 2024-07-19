package dev.realme.ash.mixin.network;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.gui.chat.ChatMessageEvent;
import dev.realme.ash.impl.event.gui.chat.SendMessageEvent;
import dev.realme.ash.impl.event.network.GameJoinEvent;
import dev.realme.ash.impl.event.network.InventoryEvent;
import dev.realme.ash.impl.imixin.IClientPlayNetworkHandler;
import dev.realme.ash.mixin.accessor.AccessorClientConnection;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class MixinClientPlayNetworkHandler implements IClientPlayNetworkHandler {
   @Unique
   private boolean ignoreChatMessage;

   @Shadow
   public abstract void sendChatMessage(String var1);

   @Inject(
      method = {"sendChatMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendChatMessage(String message, CallbackInfo ci) {
      if (!this.ignoreChatMessage) {
         SendMessageEvent event = new SendMessageEvent(message);
         Ash.EVENT_HANDLER.dispatch(event);
         if (event.isCanceled()) {
            ci.cancel();
         } else if (!event.message.equals(event.defaultMessage)) {
            this.ignoreChatMessage = true;
            this.sendChatMessage(event.message);
            this.ignoreChatMessage = false;
            ci.cancel();
         }

      }
   }

   @Shadow
   public abstract ClientConnection getConnection();

   @Inject(
      method = {"sendChatMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookSendChatMessage(String content, CallbackInfo ci) {
      ChatMessageEvent.Server chatInputEvent = new ChatMessageEvent.Server(content);
      Ash.EVENT_HANDLER.dispatch(chatInputEvent);
      if (chatInputEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"onGameJoin"},
      at = {@At("TAIL")}
   )
   private void hookOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
      GameJoinEvent gameJoinEvent = new GameJoinEvent();
      Ash.EVENT_HANDLER.dispatch(gameJoinEvent);
   }

   @Inject(
      method = {"onInventory"},
      at = {@At("TAIL")}
   )
   private void hookOnInventory(InventoryS2CPacket packet, CallbackInfo ci) {
      InventoryEvent inventoryEvent = new InventoryEvent(packet);
      Ash.EVENT_HANDLER.dispatch(inventoryEvent);
   }

   public void sendQuietPacket(Packet packet) {
      ((AccessorClientConnection)this.getConnection()).hookSendInternal(packet, null, true);
   }
}
