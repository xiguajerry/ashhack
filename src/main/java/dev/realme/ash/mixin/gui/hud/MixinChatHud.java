package dev.realme.ash.mixin.gui.hud;

import com.llamalad7.mixinextras.sugar.Local;
import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.gui.hud.ChatMessageEvent;
import dev.realme.ash.impl.imixin.IChatHud;
import dev.realme.ash.impl.imixin.IChatHudLine;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.render.FadeUtils;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ChatHud.class},
   priority = 10000
)
public abstract class MixinChatHud implements IChatHud {
   @Shadow
   @Final
   private List messages;
   @Shadow
   @Final
   private List visibleMessages;
   private ChatHudLine current = null;
   private int currentId;
   @Unique
   private int nextId = 0;
   @Unique
   private final HashMap map = new HashMap();
   @Unique
   private ChatHudLine.Visible last;

   @Shadow
   public abstract void addMessage(Text var1);

   @Inject(
      method = {"render"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"
)}
   )
   private void hookTimeAdded(CallbackInfo ci, @Local(ordinal = 13) int chatLineIndex) {
      try {
         this.current = (ChatHudLine)this.messages.get(chatLineIndex);
      } catch (Exception var4) {
      }

   }

   @ModifyArg(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;get(I)Ljava/lang/Object;",
   ordinal = 0,
   remap = false
)
   )
   private int get(int i) {
      this.last = (ChatHudLine.Visible)this.visibleMessages.get(i);
      if (this.last != null && !this.map.containsKey(this.last) && Modules.CLIENT_SETTING.animation.getValue()) {
         this.map.put(this.last, (new FadeUtils((long) Modules.CLIENT_SETTING.animationTime.getValue())).reset());
      }

      return i;
   }

   @Inject(
      method = {"render"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I",
   ordinal = 0,
   shift = Shift.BEFORE
)}
   )
   private void translate(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
      if (this.map.containsKey(this.last) && Modules.CLIENT_SETTING.animation.getValue()) {
         context.getMatrices().translate((double) Modules.CLIENT_SETTING.animationOffset.getValue() * (1.0 - ((FadeUtils)this.map.get(this.last)).easeOutQuad()), 0.0, 0.0);
      }

   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = {@At("HEAD")}
   )
   private void hookAddMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
      ChatMessageEvent chatMessageEvent = new ChatMessageEvent(message);
      Ash.EVENT_HANDLER.dispatch(chatMessageEvent);
   }

   public void addMessage(Text message, int id) {
      this.nextId = id;
      this.addMessage(message);
      this.nextId = 0;
   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/util/List;add(ILjava/lang/Object;)V",
   ordinal = 0,
   shift = Shift.AFTER
)}
   )
   private void onAddMessageAfterNewChatHudLineVisible(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo info) {
      ((IChatHudLine)this.visibleMessages.get(0)).setId(this.nextId);
   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/util/List;add(ILjava/lang/Object;)V",
   ordinal = 1,
   shift = Shift.AFTER
)}
   )
   private void onAddMessageAfterNewChatHudLine(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo info) {
      ((IChatHudLine)this.messages.get(0)).setId(this.nextId);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"}
   )
   private void onAddMessage(Text message, @Nullable MessageSignatureData signature, int ticks, @Nullable MessageIndicator indicator, boolean refresh, CallbackInfo info) {
      if (this.nextId != 0) {
         this.visibleMessages.removeIf((msg) -> {
            return msg == null || ((IChatHudLine)msg).getId() == this.nextId;
         });
         this.messages.removeIf((msg) -> {
            return msg == null || ((IChatHudLine)msg).getId() == this.nextId;
         });
      }

   }
}
