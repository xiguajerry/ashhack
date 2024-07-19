package dev.realme.ash;

public class ShutdownHook extends Thread {
   public ShutdownHook() {
      this.setName("Ash-ShutdownHook");
   }

   public void run() {
      Ash.info("Saving configurations and shutting down!");
      Ash.CONFIG.saveClient();
      Ash.loaded = false;
   }
}
