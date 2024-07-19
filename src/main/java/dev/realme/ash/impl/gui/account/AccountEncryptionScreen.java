package dev.realme.ash.impl.gui.account;

import dev.realme.ash.init.Managers;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class AccountEncryptionScreen extends Screen {
   private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-={}[]\\|'\";:/?.>,<`";
   private static final String[] REQUIREMENTS = new String[]{"8+ Characters", "A Special Character", "A Number", "An Uppercase Letter"};
   private final Screen parent;
   private TextFieldWidget passwordTextField;

   public AccountEncryptionScreen(Screen parent) {
      super(Text.of("Account Encryption"));
      this.parent = parent;
   }

   protected void init() {
      this.clearChildren();
       assert this.client != null;
       this.passwordTextField = new TextFieldWidget(this.client.textRenderer, 145, 20, Text.empty());
      this.passwordTextField.setPlaceholder(Text.of("Enter Password..."));
      this.passwordTextField.setPosition(this.width / 2 - this.passwordTextField.getWidth() / 2, this.height / 2 - 60);
      this.addDrawableChild(this.passwordTextField);
      this.addDrawableChild(ButtonWidget.builder(Text.of("Encrypt"), (action) -> {
      }).dimensions(this.width / 2 - 72, this.passwordTextField.getY() + 90, 145, 20).tooltip(Tooltip.of(Text.of("This will require you to enter a password every time you enter the account manager the first time!"))).build());
      this.addDrawableChild(ButtonWidget.builder(Text.of("Go Back"), (action) -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 72, this.passwordTextField.getY() + 112, 145, 20).build());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
       assert this.client != null;
       TextRenderer var10001 = this.client.textRenderer;
      int var10002 = Managers.ACCOUNT.getAccounts().size();
      context.drawCenteredTextWithShadow(var10001, "Encrypt Accounts (" + var10002 + ")", this.width / 2, this.height / 2 - 125, -1);
      var10001 = this.client.textRenderer;
      String var7 = (this.isPasswordSecure(this.passwordTextField.getText()) ? Formatting.GREEN : Formatting.RED) + "*";
      int var10003 = this.width / 2 - this.passwordTextField.getWidth() / 2 - 6;
      int var10004 = this.passwordTextField.getY() + 10;
      Objects.requireNonNull(this.client.textRenderer);
      context.drawTextWithShadow(var10001, var7, var10003, var10004 - 9 / 2, -1);
      context.drawTextWithShadow(this.client.textRenderer, "Minimum Requirements:", this.passwordTextField.getX() + 1, this.passwordTextField.getY() + this.passwordTextField.getHeight() + 10, -1);

      for(int i = 0; i < REQUIREMENTS.length; ++i) {
         String requirement = REQUIREMENTS[i];
         var10001 = this.client.textRenderer;
         var7 = "- " + requirement;
         var10003 = this.passwordTextField.getX() + 6;
         var10004 = this.passwordTextField.getY() + this.passwordTextField.getHeight() + 10;
         Objects.requireNonNull(this.textRenderer);
         context.drawTextWithShadow(var10001, var7, var10003, var10004 + (9 + 2) * (1 + i), -1);
      }

   }

   private boolean isPasswordSecure(String password) {
      if (password.length() < 8) {
         return false;
      } else {
         boolean hasUppercase = false;
         boolean hasNumber = false;
         boolean hasSpecial = false;
         char[] characters = password.toCharArray();
         char[] var6 = characters;
         int var7 = characters.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            char c = var6[var8];
            if (Character.isUpperCase(c)) {
               hasUppercase = true;
            }

            if ("!@#$%^&*()_+-={}[]\\|'\";:/?.>,<`".indexOf(c) != -1) {
               hasSpecial = true;
            }

            if (c >= '0' && c <= '9') {
               hasNumber = true;
            }
         }

         return hasUppercase && hasNumber && hasSpecial;
      }
   }
}
