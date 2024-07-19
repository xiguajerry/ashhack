package dev.realme.ash.api.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.realme.ash.Ash;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public abstract class ConfigFile {
   protected static final Gson GSON = (new GsonBuilder()).setLenient().setPrettyPrinting().create();
   private final String fileName;
   private final Path filepath;

   public ConfigFile(Path dir, String path) {
      if (!Files.exists(dir)) {
         try {
            Files.createDirectory(dir);
         } catch (IOException var4) {
            IOException e = var4;
            Ash.error("Could not create {} dir", dir);
            e.printStackTrace();
         }
      }

      this.fileName = dir.getFileName().toString();
      this.filepath = dir.resolve(this.toJsonPath(path));
   }

   protected String read(Path path) throws IOException {
      StringBuilder content = new StringBuilder();
      BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

      String line;
      try {
         while((line = reader.readLine()) != null) {
            content.append(line).append("\n");
         }
      } catch (Throwable var7) {
         if (reader != null) {
            try {
               reader.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (reader != null) {
         reader.close();
      }

      return content.toString();
   }

   protected String serialize(Object obj) {
      return GSON.toJson(obj);
   }

   protected JsonObject parseObject(String json) {
      return (JsonObject)this.parse(json, JsonObject.class);
   }

   protected JsonArray parseArray(String json) {
      return (JsonArray)this.parse(json, JsonArray.class);
   }

   protected Object parse(String json, Class type) {
      try {
         return GSON.fromJson(json, type);
      } catch (JsonSyntaxException var4) {
         JsonSyntaxException e = var4;
         Ash.error("Invalid json syntax!");
         e.printStackTrace();
         return null;
      }
   }

   protected void write(Path path, String content) throws IOException {
      OutputStream out = Files.newOutputStream(path);
      byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
      out.write(bytes, 0, bytes.length);
      out.close();
   }

   public String getFileName() {
      return this.fileName;
   }

   public Path getFilepath() {
      return this.filepath;
   }

   public abstract void save();

   public abstract void load();

   private String toJsonPath(String fileName) {
      return String.format("%s.json", fileName).toLowerCase();
   }
}
