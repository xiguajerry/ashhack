package dev.realme.ash.mixin.gui.screen;

import dev.realme.ash.impl.gui.account.AccountSelectorScreen;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.AccessibilityOnboardingButtons;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreen.class})
public abstract class MixinTitleScreen extends Screen {
   @Shadow
   private @Nullable SplashTextRenderer splashText;
   @Shadow
   @Final
   public static Text COPYRIGHT;
   @Shadow
   private @Nullable RealmsNotificationsScreen realmsNotificationGui;
   @Shadow
   private long backgroundFadeStart;
   @Shadow
   @Final
   private boolean doBackgroundFade;

   @Shadow
   protected abstract void initWidgetsDemo(int var1, int var2);

   @Shadow
   protected abstract void initWidgetsNormal(int var1, int var2);

   @Shadow
   protected abstract boolean isRealmsNotificationsGuiDisplayed();

   public MixinTitleScreen(Text title) {
      super(title);
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   public void hookRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
      float f = this.doBackgroundFade ? (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
      float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
      int i = MathHelper.ceil(g * 255.0F) << 24;
      if ((i & -67108864) != 0) {
         TextRenderer var10001 = this.client.textRenderer;
         int var10004 = this.height;
         Objects.requireNonNull(this.client.textRenderer);
         context.drawTextWithShadow(var10001, "Ash 3.0", 2, var10004 - 9 * 2 - 2, 16777215 | i);
      }
   }

   @Inject(
      method = {"init"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookInit(CallbackInfo ci) {
      ci.cancel();
      if (this.splashText == null) {
         this.splashText = this.client.getSplashTextLoader().get();
      }

      int i = this.textRenderer.getWidth(COPYRIGHT);
      int j = this.width - i - 2;
      int l = this.height / 4 + 48;
      if (this.client.isDemo()) {
         this.initWidgetsDemo(l, 24);
      } else {
         this.initWidgetsNormal(l, 24);
      }

      TextIconButtonWidget textIconButtonWidget = this.addDrawableChild(AccessibilityOnboardingButtons.createLanguageButton(20, (button) -> this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager())), true));
      textIconButtonWidget.setPosition(this.width / 2 - 124, l + 72 + 24);
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), (button) -> this.client.setScreen(new OptionsScreen(this, this.client.options))).dimensions(this.width / 2 - 100, l + 72 + 24, 98, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), (button) -> this.client.scheduleStop()).dimensions(this.width / 2 + 2, l + 72 + 24, 98, 20).build());
      TextIconButtonWidget textIconButtonWidget2 = this.addDrawableChild(AccessibilityOnboardingButtons.createAccessibilityButton(20, (button) -> this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options)), true));
      textIconButtonWidget2.setPosition(this.width / 2 + 104, l + 72 + 24);
      this.addDrawableChild(new PressableTextWidget(j, this.height - 10, i, 10, COPYRIGHT, (button) -> this.client.setScreen(new CreditsAndAttributionScreen(this)), this.textRenderer));
      if (this.realmsNotificationGui == null) {
         this.realmsNotificationGui = new RealmsNotificationsScreen();
      }

      if (this.isRealmsNotificationsGuiDisplayed()) {
         this.realmsNotificationGui.init(this.client, this.width, this.height);
      }

   }

   @Inject(
      method = {"initWidgetsNormal"},
      at = {@At(
   target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",
   value = "INVOKE",
   shift = Shift.AFTER,
   ordinal = 2
)}
   )
   public void hookInit(int y, int spacingY, CallbackInfo ci) {
      ButtonWidget widget = ButtonWidget.builder(Text.of("Account Manager"), (action) -> this.client.setScreen(new AccountSelectorScreen(this))).dimensions(this.width / 2 - 100, y + spacingY * 3, 200, 20).tooltip(Tooltip.of(Text.of("Allows you to switch your in-game account"))).build();
      widget.active = true;
      this.addDrawableChild(widget);
   }
}
