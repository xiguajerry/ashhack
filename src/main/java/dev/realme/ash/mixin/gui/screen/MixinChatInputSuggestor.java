package dev.realme.ash.mixin.gui.screen;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import dev.realme.ash.init.Managers;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({ChatInputSuggestor.class})
public abstract class MixinChatInputSuggestor {
   @Shadow
   private ParseResults parse;
   @Shadow
   @Final
   private TextFieldWidget textField;
   @Shadow
   private boolean completingSuggestions;
   @Shadow
   @Nullable
   private ChatInputSuggestor.@Nullable SuggestionWindow window;
   @Shadow
   private @Nullable CompletableFuture pendingSuggestions;

   @Shadow
   protected abstract void showCommandSuggestions();

   @Inject(
      method = {"refresh"},
      at = {@At(
   value = "INVOKE",
   target = "Lcom/mojang/brigadier/StringReader;canRead()Z",
   remap = false
)},
      cancellable = true,
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void hookRefresh(CallbackInfo ci, String string, StringReader stringReader) {
      if (stringReader.getString().startsWith(Managers.COMMAND.getPrefix(), stringReader.getCursor())) {
         stringReader.setCursor(stringReader.getCursor() + 1);
         if (this.parse == null) {
            this.parse = Managers.COMMAND.getDispatcher().parse(stringReader, Managers.COMMAND.getSource());
         }

         int cursor = this.textField.getCursor();
         if (cursor >= 1 && (this.window == null || !this.completingSuggestions)) {
            this.pendingSuggestions = Managers.COMMAND.getDispatcher().getCompletionSuggestions(this.parse, cursor);
            this.pendingSuggestions.thenRun(() -> {
               if (this.pendingSuggestions.isDone()) {
                  this.showCommandSuggestions();
               }

            });
         }

         ci.cancel();
      }

   }
}
