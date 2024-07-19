package dev.realme.ash.api.module.file;

import com.google.gson.JsonObject;
import dev.realme.ash.Ash;
import dev.realme.ash.api.file.ConfigFile;
import dev.realme.ash.api.module.Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class ModuleFile extends ConfigFile {
   private final Module module;

   public ModuleFile(Path dir, Module module) {
      super(dir, module.getId());
      this.module = module;
   }

   public void save() {
      try {
         Path filepath = this.getFilepath();
         if (!Files.exists(filepath, new LinkOption[0])) {
            Files.createFile(filepath);
         }

         JsonObject json = this.module.toJson();
         this.write(filepath, this.serialize(json));
      } catch (IOException var3) {
         IOException e = var3;
         Ash.error("Could not save file for {}!", this.module.getName());
         e.printStackTrace();
      }

   }

   public void load() {
      try {
         Path filepath = this.getFilepath();
         if (Files.exists(filepath, new LinkOption[0])) {
            String content = this.read(filepath);
            this.module.fromJson(this.parseObject(content));
         }
      } catch (IOException var3) {
         IOException e = var3;
         Ash.error("Could not read file for {}!", this.module.getName());
         e.printStackTrace();
      }

   }
}
