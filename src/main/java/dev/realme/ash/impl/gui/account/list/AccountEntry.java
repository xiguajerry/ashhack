package dev.realme.ash.impl.gui.account.list;

import dev.realme.ash.api.account.type.MinecraftAccount;
import dev.realme.ash.api.account.type.impl.CrackedAccount;
import dev.realme.ash.api.account.type.impl.MicrosoftAccount;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.network.TextureDownloader;

import java.io.IOException;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AccountEntry extends AlwaysSelectedEntryListWidget.Entry implements Globals {
   private static final TextureDownloader FACE_DOWNLOADER = new TextureDownloader();
   private final MinecraftAccount account;
   private long lastClickTime = -1L;

   public AccountEntry(MinecraftAccount account) {
      this.account = account;
   }

   public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
      TextRenderer var10001 = mc.textRenderer;
      Text var10002 = Text.of(this.account.username());
      int var10003 = x + 20;
      int var10004 = y + entryHeight / 2;
      Objects.requireNonNull(mc.textRenderer);
      context.drawTextWithShadow(var10001, var10002, var10003, var10004 - 9 / 2, hovered ? 5635925 : -1);
      if (!(this.account instanceof CrackedAccount)) {
         MinecraftAccount var12 = this.account;
         if (!(var12 instanceof MicrosoftAccount msa)) {
            return;
         }

          if (msa.getUsernameOrNull() == null) {
            return;
         }
      }

      String id = "face_" + this.account.username().toLowerCase();
      if (!FACE_DOWNLOADER.exists(id)) {
         if (!FACE_DOWNLOADER.isDownloading(id)) {
            FACE_DOWNLOADER.downloadTexture(id, "https://minotar.net/helm/" + this.account.username() + "/15", false);
         }

      } else {
         Identifier texture = FACE_DOWNLOADER.get(id);
         if (texture != null) {
            context.drawTexture(texture, x + 2, y + 2, 0.0F, 0.0F, 15, 15, 15, 15);
         }

      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         long time = System.currentTimeMillis() - this.lastClickTime;
         if (time > 0L && time < 500L) {
             Session session = null;
             try {
                 session = this.account.login();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
             if (session != null) {
               Managers.ACCOUNT.setSession(session);
            }
         }

         this.lastClickTime = System.currentTimeMillis();
         return false;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public Text getNarration() {
      MinecraftAccount var2 = this.account;
      if (var2 instanceof MicrosoftAccount msa) {
         if (msa.username() == null) {
            return null;
         }
      }

      return Text.of(this.account.username());
   }

   public MinecraftAccount getAccount() {
      return this.account;
   }
}
