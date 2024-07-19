package dev.realme.ash.mixin.text;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.text.TextVisitEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({TextVisitFactory.class})
public abstract class MixinTextVisitFactory implements Globals {
   @Shadow
   private static boolean visitRegularCharacter(Style style, CharacterVisitor visitor, int index, char c) {
      return false;
   }

   @ModifyArg(
      method = {"visitFormatted(Ljava/lang/String;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
   ordinal = 0
),
      index = 0
   )
   private static String hookVisitFormatted(String text) {
      if (text == null) {
         return "";
      } else if (mc.player == null) {
         return text;
      } else {
         TextVisitEvent textVisitEvent = new TextVisitEvent(text);
         Ash.EVENT_HANDLER.dispatch(textVisitEvent);
         return textVisitEvent.isCanceled() ? textVisitEvent.getText() : text;
      }
   }

   @Inject(
      method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void hookVisitFormatted$1(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor, CallbackInfoReturnable cir) {
      cir.cancel();
      int i = text.length();
      Style style = startingStyle;

      for(int j = startIndex; j < i; ++j) {
         char c = text.charAt(j);
         char d;
         if (c == 167) {
            if (j + 1 >= i) {
               break;
            }

            d = text.charAt(j + 1);
            if (d == 's') {
               style = style.withColor(Modules.CLIENT_SETTING.getRGB());
            } else {
               Formatting formatting = Formatting.byCode(d);
               if (formatting != null) {
                  style = formatting == Formatting.RESET ? resetStyle : style.withExclusiveFormatting(formatting);
               }
            }

            ++j;
         } else if (Character.isHighSurrogate(c)) {
            if (j + 1 >= i) {
               if (!visitor.accept(j, style, 65533)) {
                  cir.setReturnValue(false);
                  return;
               }
               break;
            }

            d = text.charAt(j + 1);
            if (Character.isLowSurrogate(d)) {
               if (!visitor.accept(j, style, Character.toCodePoint(c, d))) {
                  cir.setReturnValue(false);
                  return;
               }

               ++j;
            } else if (!visitor.accept(j, style, 65533)) {
               cir.setReturnValue(false);
               return;
            }
         } else if (!visitRegularCharacter(style, visitor, j, c)) {
            cir.setReturnValue(false);
            return;
         }
      }

      cir.setReturnValue(true);
   }
}
