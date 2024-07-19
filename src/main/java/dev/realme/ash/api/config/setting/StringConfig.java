package dev.realme.ash.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.api.config.Config;

public class StringConfig extends Config {
   public StringConfig(String name, String desc, String value) {
      super(name, desc, value);
   }

   public JsonObject toJson() {
      JsonObject configObj = super.toJson();
      configObj.addProperty("value", (String)this.getValue());
      return configObj;
   }

   public String fromJson(JsonObject jsonObj) {
      if (jsonObj.has("value")) {
         JsonElement element = jsonObj.get("value");
         return element.getAsString();
      } else {
         return null;
      }
   }
}
