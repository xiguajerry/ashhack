package dev.realme.ash.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.api.config.Config;
import java.util.function.Supplier;

public class EnumConfig<E extends Enum<E>> extends Config<E> {
   private final Enum[] values;

   public EnumConfig(String name, String desc, E val, E[] values) {
      super(name, desc, (E) val);
      this.values = values;
   }

   public EnumConfig(String name, String desc, E val, E[] values, Supplier visible) {
      super(name, desc, (E) val, visible);
      this.values = values;
   }

   public String getValueName() {
      return ((Enum)this.getValue()).name();
   }

   public Enum[] getValues() {
      return this.values;
   }

   public JsonObject toJson() {
      JsonObject configObj = super.toJson();
      configObj.addProperty("value", this.getValueName());
      return configObj;
   }

   public Enum fromJson(JsonObject jsonObj) {
      if (jsonObj.has("value")) {
         JsonElement element = jsonObj.get("value");

         try {
            return Enum.valueOf(((Enum)this.getValue()).getClass(), element.getAsString());
         } catch (IllegalArgumentException var4) {
            return null;
         }
      } else {
         return (Enum)this.getValue();
      }
   }
}
