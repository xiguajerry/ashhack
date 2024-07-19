package dev.realme.ash.impl.manager.client;

import dev.realme.ash.Ash;
import dev.realme.ash.api.account.config.AccountFile;
import dev.realme.ash.api.account.config.EncryptedAccountFile;
import dev.realme.ash.api.account.msa.MSAAuthenticator;
import dev.realme.ash.api.account.type.MinecraftAccount;
import dev.realme.ash.mixin.accessor.AccessorMinecraftClient;
import dev.realme.ash.util.Globals;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.session.Session;

public final class AccountManager implements Globals {
   public static final MSAAuthenticator MSA_AUTHENTICATOR = new MSAAuthenticator();
   private final List accounts = new LinkedList();
   private AccountFile configFile;

   public void postInit() {
      Path runDir = Ash.CONFIG.getClientDirectory();
      if (runDir.resolve("accounts_enc.json").toFile().exists()) {
         System.out.println("Encrypted account file exists");
         this.configFile = new EncryptedAccountFile(runDir);
      } else {
         System.out.println("Normal account file");
         this.configFile = new AccountFile(runDir);
      }

      Ash.CONFIG.addFile(this.configFile);
   }

   public void register(MinecraftAccount account) {
      this.accounts.add(account);
   }

   public void unregister(MinecraftAccount account) {
      this.accounts.remove(account);
   }

   public void setSession(Session session) {
      ((AccessorMinecraftClient)mc).setSession(session);
      Ash.info("Set session to {} ({})", session.getUsername(), session.getUuidOrNull());
   }

   public List getAccounts() {
      return this.accounts;
   }

   public boolean isEncrypted() {
      return this.configFile instanceof EncryptedAccountFile;
   }
}
