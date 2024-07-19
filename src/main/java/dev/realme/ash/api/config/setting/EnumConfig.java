package dev.realme.ash.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.api.config.Config;
import java.util.function.Supplier;

public class EnumConfig<E extends Enum<E>> extends Config<E> {
   private final Enum[] values;

   public EnumConfig(String name, String desc, E val, E[] values) {
      super(name, desc, val);
      this.values = values;
   }

   public EnumConfig(String name, String desc, E val, E[] values, Supplier visible) {
      super(name, desc, val, visible);
      this.values = values;
   }

   public String getValueName() {
      return this.getValue().name();
   }

   public Enum[] getValues() {
      return this.values;
   }

   public JsonObject toJson() {
      JsonObject configObj = super.toJson();
      configObj.addProperty("value", this.getValueName());
      return configObj;
   }

   public Enum<E> fromJson(JsonObject jsonObj) {
      if (jsonObj.has("value")) {
         JsonElement element = jsonObj.get("value");

         try {
            return Enum.<E>valueOf((Class<E>) this.getValue().getClass(), element.getAsString());
         } catch (IllegalArgumentException var4) {
            return null;
         }
      } else {
         return this.getValue();
      }
   }
}
