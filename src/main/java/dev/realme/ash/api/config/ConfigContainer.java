package dev.realme.ash.api.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.Ash;
import dev.realme.ash.api.Identifiable;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.ItemListConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.config.setting.StringConfig;
import dev.realme.ash.api.config.setting.ToggleConfig;
import dev.realme.ash.api.macro.Macro;
import dev.realme.ash.util.Globals;
import net.minecraft.item.Item;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigContainer implements Identifiable, Serializable, Globals {
   protected final String name;
   private final Map configurations = Collections.synchronizedMap(new LinkedHashMap());

   public ConfigContainer(String name) {
      this.name = name;
   }

   protected void register(Config config) {
      config.setContainer(this);
      this.configurations.put(config.getId(), config);
   }

   protected void register(Config... configs) {
      Config[] var2 = configs;
      int var3 = configs.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Config config = var2[var4];
         this.register(config);
      }

   }

   protected void unregister(Config config) {
      this.configurations.remove(config.getId());
   }

   protected void unregister(Config... configs) {
      Config[] var2 = configs;
      int var3 = configs.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Config config = var2[var4];
         this.unregister(config);
      }

   }

   public void reflectConfigs() {
      ConfigFactory factory = new ConfigFactory(this);
      Field[] var2 = this.getClass().getDeclaredFields();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Field field = var2[var4];
         if (Config.class.isAssignableFrom(field.getType())) {
            Config config = factory.build(field);
            if (config == null) {
               Ash.error("Value for field {} is null!", field);
            } else {
               this.register(config);
            }
         }
      }

   }

   public JsonObject toJson() {
      JsonObject out = new JsonObject();
      out.addProperty("name", this.getName());
      out.addProperty("id", this.getId());
      JsonArray array = new JsonArray();
      Iterator var3 = this.getConfigs().iterator();

      while(var3.hasNext()) {
         Config config = (Config)var3.next();
         if (!(config.getValue() instanceof Macro)) {
            array.add(config.toJson());
         }
      }

      out.add("configs", array);
      return out;
   }

   @Override
   public Config fromJson(JsonObject jsonObj) {
      if (jsonObj.has("configs")) {
         JsonElement element = jsonObj.get("configs");
         if (!element.isJsonArray()) {
            return null;
         }

          for (JsonElement je : element.getAsJsonArray()) {
              if (je.isJsonObject()) {
                  JsonObject configObj = je.getAsJsonObject();
                  JsonElement id = configObj.get("id");
                  Config config = this.getConfig(id.getAsString());
                  if (config != null) {
                      try {
                          Object val;
                          if (config instanceof ToggleConfig cfg) {
                              val = cfg.fromJson(configObj);
                              if (mc.world != null) {
                                  if ((Boolean) val) {
                                      cfg.enable();
                                  } else {
                                      cfg.disable();
                                  }
                              } else {
                                  cfg.setValue((Boolean) val);
                              }
                          } else if (config instanceof BooleanConfig) {
                              BooleanConfig cfg = (BooleanConfig) config;
                              val = cfg.fromJson(configObj);
                              cfg.setValue((Boolean) val);
                          } else if (config instanceof ColorConfig cfg) {
                              val = cfg.fromJson(configObj);
                              cfg.setValue(val);
                          } else if (config instanceof EnumConfig) {
                              EnumConfig cfg = (EnumConfig) config;
                              val = cfg.fromJson(configObj);
                              if (val != null) {
                                  cfg.setValue(val);
                              }
                          } else if (config instanceof ItemListConfig) {
                              ItemListConfig cfg = (ItemListConfig) config;
                              val = cfg.fromJson(configObj);
                              cfg.setValue((List<Item>) val);
                          } else if (config instanceof NumberConfig) {
                              NumberConfig cfg = (NumberConfig) config;
                              val = cfg.fromJson(configObj);
                              cfg.setValue((Number) val);
                          } else if (config instanceof StringConfig) {
                              StringConfig cfg = (StringConfig) config;
                              val = cfg.fromJson(configObj);
                              cfg.setValue(val);
                          }
                      } catch (Exception var16) {
                          Exception e = var16;
                          Ash.error("Couldn't parse Json for {}!", config.getName());
                          e.printStackTrace();
                      }
                  }
              }
          }
      }

      return null;
   }

   public String getName() {
      return this.name;
   }

   public String getId() {
      return String.format("%s-container", this.name.toLowerCase());
   }

   public Config getConfig(String id) {
      return (Config)this.configurations.get(id);
   }

   public Collection getConfigs() {
      return this.configurations.values();
   }
}
