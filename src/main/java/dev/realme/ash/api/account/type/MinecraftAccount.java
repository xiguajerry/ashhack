package dev.realme.ash.api.account.type;

import com.google.gson.JsonObject;
import net.minecraft.client.session.Session;

import java.io.IOException;

public interface MinecraftAccount {
   Session login() throws IOException;

   String username();

   default JsonObject toJSON() {
      JsonObject object = new JsonObject();
      object.addProperty("username", this.username());
      return object;
   }
}
