// Decompiled with: CFR 0.152
// Class Version: 17
package dev.realme.ash.api.file;

import dev.realme.ash.Ash;
import dev.realme.ash.api.file.ConfigFile;
import dev.realme.ash.api.macro.MacroFile;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.api.module.file.ModuleConfigFile;
import dev.realme.ash.api.module.file.ModuleFile;
import dev.realme.ash.api.social.SocialFile;
import dev.realme.ash.api.social.SocialRelation;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.Set;

public class ClientConfiguration
        implements Globals {
   private final Set<ConfigFile> files = new HashSet<ConfigFile>();
   private Path clientDir;

   /*
    * WARNING - Removed try catching itself - possible behaviour change.
    */
   public ClientConfiguration() {
      Path runningDir = ClientConfiguration.mc.runDirectory.toPath();
      try {
         File homeDir = new File(System.getProperty("user.home"));
         this.clientDir = homeDir.toPath();
      }
      catch (Exception e) {
         Ash.error("Could not access home dir, defaulting to running dir");
         e.printStackTrace();
         this.clientDir = runningDir;
      }
      finally {
         Path configDir;
         if (this.clientDir == null || !Files.exists(this.clientDir) || !Files.isWritable(this.clientDir)) {
            this.clientDir = runningDir;
         }
         this.clientDir = this.clientDir.resolve("Ash");
         if (!Files.exists(this.clientDir)) {
            try {
               Files.createDirectory(this.clientDir);
            }
            catch (IOException e) {
               Ash.error("Could not create client dir");
               e.printStackTrace();
            }
         }
         if (!Files.exists(configDir = this.clientDir.resolve("Configs"))) {
            try {
               Files.createDirectory(configDir);
            }
            catch (IOException e) {
               Ash.error("Could not create config dir");
               e.printStackTrace();
            }
         }
      }
      this.files.add(new MacroFile(this.clientDir));
      for (Module module : Managers.MODULE.getModules()) {
         this.files.add(new ModuleFile(this.clientDir.resolve("Modules"), module));
      }
      this.files.add(Modules.INV_CLEANER.getBlacklistFile(this.clientDir));
      for (SocialRelation relation : SocialRelation.values()) {
         this.files.add(new SocialFile(this.clientDir, relation));
      }
   }

   public void saveClient() {
      for (ConfigFile file : this.files) {
         file.save();
      }
   }

   public void loadClient() {
      for (ConfigFile file : this.files) {
         file.load();
      }
   }

   public void saveModuleConfiguration(String configFile) {
      ModuleConfigFile file = new ModuleConfigFile(this.clientDir.resolve("Configs"), configFile);
      file.save();
   }

   public void loadModuleConfiguration(String configFile) {
      ModuleConfigFile file = new ModuleConfigFile(this.clientDir.resolve("Configs"), configFile);
      file.load();
   }

   public Set<ConfigFile> getFiles() {
      return this.files;
   }

   public void addFile(ConfigFile configFile) {
      this.files.add(configFile);
   }

   public void removeFile(ConfigFile configFile) {
      this.files.remove(configFile);
   }

   public Path getClientDirectory() {
      return this.clientDir;
   }
}
