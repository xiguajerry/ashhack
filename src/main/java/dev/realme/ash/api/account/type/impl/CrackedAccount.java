package dev.realme.ash.api.account.type.impl;

import dev.realme.ash.api.account.type.MinecraftAccount;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;

public record CrackedAccount(String username) implements MinecraftAccount {
   public CrackedAccount(String username) {
      this.username = username;
   }

   public Session login() {
      return new Session(this.username(), UUID.randomUUID(), "", Optional.empty(), Optional.empty(), AccountType.LEGACY);
   }

   public String username() {
      return this.username;
   }
}
