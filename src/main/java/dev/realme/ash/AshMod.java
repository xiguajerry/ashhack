package dev.realme.ash;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class AshMod implements ClientModInitializer {
   public static final String MOD_NAME = "Ash";
   public static final String MOD_VER = "3.0";

   public void onInitializeClient() {
      Ash.init();
   }

   public static boolean isBaritonePresent() {
      return FabricLoader.getInstance().getModContainer("baritone").isPresent();
   }
}
