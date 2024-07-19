package dev.realme.ash.api.config;

import dev.realme.ash.Ash;
import java.lang.reflect.Field;

public class ConfigFactory {
   protected final Object configObj;

   public ConfigFactory(Object configObj) {
      this.configObj = configObj;
   }

   public Config build(Field f) {
      f.setAccessible(true);

      try {
         return (Config)f.get(this.configObj);
      } catch (IllegalAccessException | IllegalArgumentException var3) {
         Exception e = var3;
         Ash.error("Failed to build config from field {}!", f.getName());
         e.printStackTrace();
         throw new RuntimeException("Invalid field!");
      }
   }
}
