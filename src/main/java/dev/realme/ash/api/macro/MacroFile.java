// Decompiled with: Procyon 0.6.0
// Class Version: 17
package dev.realme.ash.api.macro;

import dev.realme.ash.api.module.Module;
import com.google.gson.JsonObject;
import dev.realme.ash.api.module.ToggleModule;
import java.util.Iterator;
import java.io.IOException;
import dev.realme.ash.Ash;
import com.google.gson.JsonElement;
import dev.realme.ash.init.Managers;
import com.google.gson.JsonArray;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import dev.realme.ash.api.file.ConfigFile;

public class MacroFile extends ConfigFile
{
   public MacroFile(final Path dir) {
      super(dir, "macros");
   }

   @Override
   public void save() {
      try {
         final Path filepath = this.getFilepath();
         if (!Files.exists(filepath)) {
            Files.createFile(filepath, new FileAttribute[0]);
         }
         final JsonArray object = new JsonArray();
         for (final Macro macro : Managers.MACRO.getMacros()) {
            object.add(macro.toJson());
         }
         this.write(filepath, this.serialize(object));
      }
      catch (final IOException e) {
         Ash.error("Could not save macro file!");
         e.printStackTrace();
      }
   }

   @Override
   public void load() {
      try {
         final Path filepath = this.getFilepath();
         if (Files.exists(filepath)) {
            final String content = this.read(filepath);
            final JsonArray object = this.parseArray(content);
            for (final JsonElement element : object.getAsJsonArray()) {
               final JsonObject jsonObject = element.getAsJsonObject();
               if (jsonObject.has("id")) {
                  final String id = jsonObject.get("id").getAsString();
                  final Macro macro = Managers.MACRO.getMacro(m -> m.getId().equals(id));
                  if (macro == null) {
                     continue;
                  }
                  macro.fromJson(jsonObject);
                  final Module module = Managers.MODULE.getModule(id.substring(0, id.length() - 6));
                  if (!(module instanceof ToggleModule t)) {
                     continue;
                  }
                   t.keybind(jsonObject.get("value").getAsInt());
               }
            }
         }
      }
      catch (final IOException e) {
         Ash.error("Could not read macro file!");
         e.printStackTrace();
      }
   }
}
