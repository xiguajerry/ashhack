package dev.realme.ash.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.api.config.Config;
import java.util.function.Supplier;

public class BooleanConfig extends Config<Boolean> {
   public BooleanConfig(String name, String desc, Boolean val) {
      super(name, desc, val);
   }

   public BooleanConfig(String name, String desc, Boolean val, Supplier<Boolean> visible) {
      super(name, desc, val, visible);
      this.configAnimation.setState(val);
   }

   public void setValue(Boolean in) {
      super.setValue(in);
      this.configAnimation.setState(in);
   }

   public JsonObject toJson() {
      JsonObject configObj = super.toJson();
      configObj.addProperty("value", this.getValue());
      return configObj;
   }

   public Boolean fromJson(JsonObject jsonObj) {
      if (jsonObj.has("value")) {
         JsonElement element = jsonObj.get("value");
         return element.getAsBoolean();
      } else {
         return null;
      }
   }
}
