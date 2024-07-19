package dev.realme.ash.api.social;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.realme.ash.Ash;
import dev.realme.ash.api.file.ConfigFile;
import dev.realme.ash.init.Managers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Iterator;

public class SocialFile extends ConfigFile {
   private final SocialRelation relation;

   public SocialFile(Path dir, SocialRelation relation) {
      super(dir, relation.name());
      this.relation = relation;
   }

   public void save() {
      try {
         Path filepath = this.getFilepath();
         if (!Files.exists(filepath, new LinkOption[0])) {
            Files.createFile(filepath);
         }

         JsonArray array = new JsonArray();
         Iterator var3 = Managers.SOCIAL.getRelations(this.relation).iterator();

         while(var3.hasNext()) {
            String socials = (String)var3.next();
            array.add(new JsonPrimitive(socials));
         }

         this.write(filepath, this.serialize(array));
      } catch (IOException var5) {
         IOException e = var5;
         Ash.error("Could not save file for {}.json!", this.relation.name().toLowerCase());
         e.printStackTrace();
      }

   }

   public void load() {
      try {
         Path filepath = this.getFilepath();
         if (Files.exists(filepath, new LinkOption[0])) {
            String content = this.read(filepath);
            JsonArray json = this.parseArray(content);
            if (json == null) {
               return;
            }

            Iterator var4 = json.asList().iterator();

            while(var4.hasNext()) {
               JsonElement element = (JsonElement)var4.next();
               Managers.SOCIAL.addRelation(element.getAsString(), this.relation);
            }
         }
      } catch (IOException var6) {
         IOException e = var6;
         Ash.error("Could not read file for {}.json!", this.relation.name().toLowerCase());
         e.printStackTrace();
      }

   }
}