package dev.realme.ash.api.macro;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.api.Identifiable;
import dev.realme.ash.api.config.Serializable;
import dev.realme.ash.util.KeyboardUtil;

public class Macro implements Identifiable, Serializable {
   private final String name;
   private final Runnable macro;
   private int keycode;

   public Macro(String name, int keycode, Runnable macro) {
      this.name = name;
      this.keycode = keycode;
      this.macro = macro;
   }

   public void runMacro() {
      this.macro.run();
   }

   public String getName() {
      return this.name;
   }

   public Runnable getRunnable() {
      return this.macro;
   }

   public int getKeycode() {
      return this.keycode;
   }

   public void setKeycode(int keycode) {
      this.keycode = keycode;
   }

   public String getId() {
      return String.format("%s-macro", this.name.toLowerCase());
   }

   public String getKeyName() {
      if (this.keycode != -1) {
         String name = KeyboardUtil.getKeyName(this.keycode);
         return name != null ? name.toUpperCase() : "NONE";
      } else {
         return "NONE";
      }
   }

   public JsonObject toJson() {
      JsonObject obj = new JsonObject();
      obj.addProperty("id", this.getId());
      obj.addProperty("value", this.getKeycode());
      return obj;
   }

   public Macro fromJson(JsonObject jsonObj) {
      if (jsonObj.has("value")) {
         JsonElement element = jsonObj.get("value");
         return new Macro(this.getId(), element.getAsInt(), this.getRunnable());
      } else {
         return null;
      }
   }
}
