package dev.realme.ash.api.account.type.impl;

import com.google.gson.JsonObject;
import dev.realme.ash.api.account.msa.exception.MSAAuthException;
import dev.realme.ash.api.account.type.MinecraftAccount;
import dev.realme.ash.impl.manager.client.AccountManager;
import net.minecraft.client.session.Session;

import java.io.IOException;

public final class MicrosoftAccount implements MinecraftAccount {
   private final String email;
   private final String password;
   private String accessToken;
   private String username;

   public MicrosoftAccount(String accessToken) {
      this((String)null, (String)null);
      if (accessToken != null && !accessToken.isEmpty()) {
         this.accessToken = accessToken;
      } else {
         throw new RuntimeException("Access token should not be null");
      }
   }

   public MicrosoftAccount(String email, String password) {
      this.email = email;
      this.password = password;
   }

   public Session login() throws IOException {
      Session session = null;

      MSAAuthException e;
      try {
         if (this.email != null && this.password != null) {
            try {
               session = AccountManager.MSA_AUTHENTICATOR.loginWithCredentials(this.email, this.password);
            } catch (MSAAuthException var3) {
               e = var3;
               AccountManager.MSA_AUTHENTICATOR.setLoginStage(e.getMessage());
               return null;
            }
         } else if (this.accessToken != null) {
            if (this.accessToken.startsWith("M.")) {
               this.accessToken = AccountManager.MSA_AUTHENTICATOR.getLoginToken(this.accessToken);
            }

            session = AccountManager.MSA_AUTHENTICATOR.loginWithToken(this.accessToken, true);
         }
      } catch (MSAAuthException var4) {
         e = var4;
         e.printStackTrace();
         AccountManager.MSA_AUTHENTICATOR.setLoginStage(e.getMessage());
         return null;
      }

      if (session != null) {
         AccountManager.MSA_AUTHENTICATOR.setLoginStage("");
         this.username = session.getUsername();
         return session;
      } else {
         return null;
      }
   }

   public JsonObject toJSON() {
      JsonObject object = MinecraftAccount.super.toJSON();
      if (this.accessToken != null) {
         object.addProperty("token", this.accessToken);
      } else {
         if (this.email == null || this.password == null) {
            throw new RuntimeException("Email/Password & Access token is null for a MSA?");
         }

         object.addProperty("email", this.email);
         object.addProperty("password", this.password);
      }

      return object;
   }

   public String getEmail() {
      return this.email;
   }

   public String getPassword() {
      return this.password;
   }

   public String username() {
      return this.username != null ? this.username : this.email;
   }

   public String getUsernameOrNull() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getAccessToken() {
      return this.accessToken;
   }
}
