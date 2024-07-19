package dev.realme.ash.mixin.gui.screen;

import dev.realme.ash.Ash;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.gui.chat.ChatInputEvent;
import dev.realme.ash.impl.event.gui.chat.ChatKeyInputEvent;
import dev.realme.ash.impl.event.gui.chat.ChatMessageEvent;
import dev.realme.ash.impl.event.gui.chat.ChatRenderEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.AccessorTextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ChatScreen.class})
public class MixinChatScreen extends MixinScreen {
   @Shadow
   protected TextFieldWidget chatField;

   @Inject(
      method = {"onChatFieldUpdate"},
      at = {@At("TAIL")}
   )
   private void hookOnChatFieldUpdate(String chatText, CallbackInfo ci) {
      ChatInputEvent chatInputEvent = new ChatInputEvent(chatText);
      Ash.EVENT_HANDLER.dispatch(chatInputEvent);
   }

   @Inject(
      method = {"keyPressed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable cir) {
      ChatKeyInputEvent keyInputEvent = new ChatKeyInputEvent(keyCode, this.chatField.getText());
      Ash.EVENT_HANDLER.dispatch(keyInputEvent);
      if (keyInputEvent.isCanceled()) {
         cir.cancel();
         this.chatField.setText(keyInputEvent.getChatText());
      }

   }

   @Inject(
      method = {"sendMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookSendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable cir) {
      ChatMessageEvent.Client chatMessageEvent = new ChatMessageEvent.Client(chatText);
      Ash.EVENT_HANDLER.dispatch(chatMessageEvent);
      if (chatMessageEvent.isCanceled()) {
         cir.setReturnValue(true);
         cir.cancel();
      }

   }

   @Inject(
      method = {"render"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
   shift = Shift.BEFORE
)}
   )
   private void hookRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      float x = ((AccessorTextFieldWidget)this.chatField).isDrawsBackground() ? (float)(this.chatField.getX() + 6) : (float)(this.chatField.getX() + 2);
      float y = ((AccessorTextFieldWidget)this.chatField).isDrawsBackground() ? (float)this.chatField.getY() + (float)(this.chatField.getHeight() - 8) / 2.0F : (float)this.chatField.getY();
      ChatRenderEvent chatTextRenderEvent = new ChatRenderEvent(context, x, y);
      Ash.EVENT_HANDLER.dispatch(chatTextRenderEvent);
   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"
)
   )
   private void hookFill(DrawContext instance, int x1, int y1, int x2, int y2, int color) {
      float openAnimation = Modules.HUD.isEnabled() ? 12.0F * Modules.HUD.getChatAnimation() : 12.0F;
      RenderManager.rect(instance.getMatrices(), 2.0, (double)((float)this.height - 2.0F), (double)(this.width - 4), (double)(-openAnimation), this.client.options.getTextBackgroundColor(Integer.MIN_VALUE));
   }

   protected Element addDrawableChild(Element drawableElement) {
      return null;
   }
}
