package dev.realme.ash.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import java.util.function.Supplier;

public class NumberConfig<T extends Number> extends Config<T> {
   private final T min;
   private final T max;
   private final NumberDisplay format;
   private final int roundingScale;

   public NumberConfig(String name, String desc, T min, T value, T max, NumberDisplay format) {
      super(name, desc, value);
      this.min = min;
      this.max = max;
      this.format = format;
      String strValue = String.valueOf(this.getValue());
      this.roundingScale = strValue.substring(strValue.indexOf(".") + 1).length();
   }

   public NumberConfig(String name, String desc, T min, T value, T max, NumberDisplay format, int roundingScale) {
      super(name, desc, value);
      this.min = min;
      this.max = max;
      this.format = format;
      this.roundingScale = roundingScale;
   }

   public NumberConfig(String name, String desc, T min, T value, T max, NumberDisplay format, Supplier visible) {
      super(name, desc, value, visible);
      this.min = min;
      this.max = max;
      this.format = format;
      String strValue = String.valueOf(this.getValue());
      this.roundingScale = strValue.substring(strValue.indexOf(".") + 1).length();
   }

   public NumberConfig(String name, String desc, T min, T value, T max) {
      this(name, desc, min, value, max, NumberDisplay.DEFAULT);
   }

   public NumberConfig(String name, String desc, T min, T value, T max, Supplier visible) {
      this(name, desc, min, value, max, NumberDisplay.DEFAULT, visible);
   }

   public Number getMin() {
      return this.min;
   }

   public Number getMax() {
      return this.max;
   }

   public boolean isMin() {
      return this.min.doubleValue() == this.getValue().doubleValue();
   }

   public boolean isMax() {
      return this.max.doubleValue() == this.getValue().doubleValue();
   }

   public int getRoundingScale() {
      return this.roundingScale;
   }

   public NumberDisplay getFormat() {
      return this.format;
   }

   public double getValueSq() {
      T val = this.getValue();
      return val.doubleValue() * val.doubleValue();
   }

   public void setValue(T val) {
      if (val.doubleValue() < this.min.doubleValue()) {
         super.setValue(this.min);
      } else if (val.doubleValue() > this.max.doubleValue()) {
         super.setValue(this.max);
      } else {
         super.setValue(val);
      }

   }

   public JsonObject toJson() {
      JsonObject configObj = super.toJson();
      if (this.getValue() instanceof Integer) {
         configObj.addProperty("value", this.getValue());
      } else if (this.getValue() instanceof Float) {
         configObj.addProperty("value", this.getValue());
      } else if (this.getValue() instanceof Double) {
         configObj.addProperty("value", this.getValue());
      }

      return configObj;
   }

   public Number fromJson(JsonObject jsonObj) {
      if (jsonObj.has("value")) {
         JsonElement element = jsonObj.get("value");
         if (this.getValue() instanceof Integer) {
            Integer val = element.getAsInt();
            return val;
         }

         if (this.getValue() instanceof Float) {
            Float val = element.getAsFloat();
            return val;
         }

         if (this.getValue() instanceof Double) {
            Double val = element.getAsDouble();
            return val;
         }
      }

      return null;
   }
}
