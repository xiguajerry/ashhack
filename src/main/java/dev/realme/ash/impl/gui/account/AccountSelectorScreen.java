package dev.realme.ash.impl.gui.account;

import dev.realme.ash.impl.gui.account.list.AccountEntry;
import dev.realme.ash.impl.gui.account.list.AccountListWidget;
import dev.realme.ash.impl.manager.client.AccountManager;
import dev.realme.ash.init.Managers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.io.IOException;

public final class AccountSelectorScreen extends Screen {
   private final Screen parent;
   private AccountListWidget accountListWidget;
   private TextFieldWidget searchWidget;

   public AccountSelectorScreen(Screen parent) {
      super(Text.of("Account Selector"));
      this.parent = parent;
   }

   protected void init() {
      this.accountListWidget = new AccountListWidget(this.client, this.width, this.height - 64 - 32, 32, 25);
      this.clearChildren();
      this.accountListWidget.setDimensionsAndPosition(this.width, this.height - 64 - 32, 0, 32);
      this.accountListWidget.populateEntries();
      this.accountListWidget.setSearchFilter(null);
       assert this.client != null;
       this.addDrawableChild(this.searchWidget = new TextFieldWidget(this.client.textRenderer, 135, 20, Text.of("Search...")));
      this.searchWidget.setPosition(this.width / 2 - this.searchWidget.getWidth() / 2, 4);
      this.searchWidget.setPlaceholder(Text.of("Search..."));
      this.addDrawableChild(ButtonWidget.builder(Text.of("Add"), (action) -> this.client.setScreen(new AccountAddAccountScreen(this))).dimensions(this.width / 2 + 2, this.accountListWidget.getHeight() + 40, 110, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.of("Login"), (action) -> {
         AccountEntry entry = (AccountEntry)this.accountListWidget.getSelectedOrNull();
         if (entry != null) {
             Session session = null;
             try {
                 session = entry.getAccount().login();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
             if (session != null) {
               Managers.ACCOUNT.setSession(session);
            }
         }

      }).dimensions(this.width / 2 - 110 - 2, this.accountListWidget.getHeight() + 40, 110, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), (action) -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 110 - 2, this.accountListWidget.getHeight() + 40 + 20 + 2, 110, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.of("Delete"), (action) -> {
         AccountEntry entry = (AccountEntry)this.accountListWidget.getSelectedOrNull();
         if (entry != null) {
            if (InputUtil.isKeyPressed(this.client.getWindow().getHandle(), 340)) {
               Managers.ACCOUNT.unregister(entry.getAccount());
               this.client.setScreen(this);
            } else {
               this.client.setScreen(new ConfirmScreen((value) -> {
                  if (value) {
                     Managers.ACCOUNT.unregister(entry.getAccount());
                  }

                  this.client.setScreen(this);
               }, Text.of("Delete account?"), Text.of("Are you sure you would like to delete " + entry.getAccount().username() + "?"), Text.of("Yes"), Text.of("No")));
            }
         }
      }).dimensions(this.width / 2 + 2, this.accountListWidget.getHeight() + 40 + 20 + 2, 110, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.of(Managers.ACCOUNT.isEncrypted() ? "Decrypt" : "Encrypt"), (action) -> {
         if (!Managers.ACCOUNT.isEncrypted()) {
            this.client.setScreen(new AccountEncryptionScreen(this));
         }

      }).dimensions(this.width - 110 - 4, 6, 110, 20).build());
   }

   public void onDisplayed() {
      if (this.accountListWidget != null) {
         this.accountListWidget.populateEntries();
      }

   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.accountListWidget.render(context, mouseX, mouseY, delta);
       assert this.client != null;
       context.drawTextWithShadow(this.client.textRenderer, Text.of(this.getLoginInfo()), 2, 2, 11184810);
      if (this.searchWidget.isSelected()) {
         String content = this.searchWidget.getText();
         if (content == null || content.isEmpty()) {
            this.accountListWidget.setSearchFilter(null);
            return;
         }

         this.accountListWidget.setSearchFilter(content.replaceAll("\\s*", ""));
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.accountListWidget.mouseClicked(mouseX, mouseY, button);
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.accountListWidget.mouseReleased(mouseX, mouseY, button);
      return super.mouseReleased(mouseX, mouseY, button);
   }

   private String getLoginInfo() {
       if (AccountManager.MSA_AUTHENTICATOR.getLoginStage().isEmpty()) {
           assert this.client != null;
           return "Logged in as " + this.client.getSession().getUsername();
       } else {
           return AccountManager.MSA_AUTHENTICATOR.getLoginStage();
       }
   }
}
