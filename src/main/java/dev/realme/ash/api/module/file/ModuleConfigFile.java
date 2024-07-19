package dev.realme.ash.api.module.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.api.file.ConfigFile;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.init.Managers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Iterator;

public class ModuleConfigFile extends ConfigFile {
   public ModuleConfigFile(Path dir, String path) {
      super(dir, path);
   }

   public void save() {
      try {
         Path filepath = this.getFilepath();
         if (!Files.exists(filepath)) {
            Files.createFile(filepath);
         }

         JsonObject out = new JsonObject();
         JsonArray array = new JsonArray();

          for (Module module : Managers.MODULE.getModules()) {
              array.add(module.toJson());
          }

         out.add("configs", array);
         this.write(filepath, this.serialize(out));
      } catch (IOException var6) {
         IOException e = var6;
         e.printStackTrace();
      }

   }

   public void load() {
      try {
         Path filepath = this.getFilepath();
         if (Files.exists(filepath)) {
            String content = this.read(filepath);
            JsonObject in = this.parseObject(content);
            if (!in.has("configs")) {
               return;
            }

            JsonArray array = in.getAsJsonArray("configs");

             for (JsonElement element : array.asList()) {
                 JsonObject object = element.getAsJsonObject();
                 if (object.has("id")) {
                     Module module = Managers.MODULE.getModule(object.get("id").getAsString());
                     if (module == null) {
                         return;
                     }

                     module.fromJson(object);
                 }
             }
         }
      } catch (IOException var9) {
         IOException e = var9;
         e.printStackTrace();
      }

   }
}
