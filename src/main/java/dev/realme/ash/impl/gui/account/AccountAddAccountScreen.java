package dev.realme.ash.impl.gui.account;

import dev.realme.ash.Ash;
import dev.realme.ash.api.account.msa.exception.MSAAuthException;
import dev.realme.ash.api.account.type.MinecraftAccount;
import dev.realme.ash.api.account.type.impl.CrackedAccount;
import dev.realme.ash.api.account.type.impl.MicrosoftAccount;
import dev.realme.ash.impl.manager.client.AccountManager;
import dev.realme.ash.init.Managers;
import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;

public final class AccountAddAccountScreen extends Screen {
   private final Screen parent;
   private TextFieldWidget email;
   private TextFieldWidget password;

   public AccountAddAccountScreen(Screen parent) {
      super(Text.of("Add or Create an Alt Account"));
      this.parent = parent;
   }

   protected void init() {
      this.clearChildren();
       assert this.client != null;
       this.addDrawableChild(this.email = new TextFieldWidget(this.client.textRenderer, this.width / 2 - 75, this.height / 2 - 30, 150, 20, Text.of("")));
      this.email.setPlaceholder(Text.of("Email or Username..."));
      this.addDrawableChild(this.password = new TextFieldWidget(this.client.textRenderer, this.width / 2 - 75, this.height / 2 - 5, 150, 20, Text.of("")));
      this.password.setPlaceholder(Text.of("Password (Optional)"));
      this.addDrawableChild(ButtonWidget.builder(Text.of("Add"), (action) -> {
         String accountEmail = this.email.getText();
         if (accountEmail.length() >= 3) {
            String accountPassword = this.password.getText();
            Object account;
            if (!accountPassword.isEmpty()) {
               account = new MicrosoftAccount(accountEmail, accountPassword);
            } else {
               account = new CrackedAccount(accountEmail);
            }

            Managers.ACCOUNT.register((MinecraftAccount)account);
            this.client.setScreen(this.parent);
         }

      }).dimensions(this.width / 2 - 72, this.height / 2 + 20, 145, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.of("Browser..."), (action) -> {
         try {
            AccountManager.MSA_AUTHENTICATOR.loginWithBrowser((token) -> Ash.EXECUTOR.execute(() -> {
               MicrosoftAccount account = new MicrosoftAccount(token);
                Session session;
                try {
                    session = account.login();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (session != null) {
                  Managers.ACCOUNT.setSession(session);
                  Managers.ACCOUNT.register(account);
                  this.client.setScreen(this.parent);
               } else {
                  AccountManager.MSA_AUTHENTICATOR.setLoginStage("Could not login to account");
               }

            }));
         } catch (URISyntaxException | MSAAuthException | IOException var3) {
            Exception e = var3;
            e.printStackTrace();
         }

      }).dimensions(this.width / 2 - 72, this.height / 2 + 20 + 22, 145, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.of("Go Back"), (action) -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 72, this.height / 2 + 20 + 44, 145, 20).build());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
       assert this.client != null;
       TextRenderer var10001 = this.client.textRenderer;
      int var10003 = this.email.getX() - 10;
      int var10004 = this.email.getY() + this.email.getHeight() / 2;
      Objects.requireNonNull(this.client.textRenderer);
      context.drawTextWithShadow(var10001, "*", var10003, var10004 - 9 / 2, (this.email.getText().length() >= 3 ? Color.green : Color.red).getRGB());
      context.drawCenteredTextWithShadow(this.client.textRenderer, "Add an Account", this.width / 2, this.height / 2 - 120, -1);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
          assert this.client != null;
          this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
