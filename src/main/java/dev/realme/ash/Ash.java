package dev.realme.ash;

import dev.realme.ash.api.Identifiable;
import dev.realme.ash.api.event.handler.EventBus;
import dev.realme.ash.api.event.handler.EventHandler;
import dev.realme.ash.api.file.ClientConfiguration;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ash {
   public static Logger LOGGER;
   public static EventHandler EVENT_HANDLER;
   public static ClientConfiguration CONFIG;
   public static ShutdownHook SHUTDOWN;
   public static Executor EXECUTOR;
   public static boolean loaded = false;

   public static void init() {
      LOGGER = LogManager.getLogger("Ash");
      info("This build of Ash is on Git hash {} and was compiled on {}", "null", "06/04/1989 24:00");
      info("Starting preInit ...");
      EXECUTOR = Executors.newFixedThreadPool(1);
      EVENT_HANDLER = new EventBus();
      info("Starting init ...");
      Managers.init();
      Modules.init();
      info("Starting postInit ...");
      CONFIG = new ClientConfiguration();
      Managers.postInit();
      SHUTDOWN = new ShutdownHook();
      Runtime.getRuntime().addShutdownHook(SHUTDOWN);
      CONFIG.loadClient();
      loaded = true;
   }

   public static void info(String message) {
      LOGGER.info(String.format("[Ash] %s", message));
   }

   public static void info(String message, Object... params) {
      LOGGER.info(String.format("[Ash] %s", message), params);
   }

   public static void info(Identifiable feature, String message) {
      LOGGER.info(String.format("[%s] %s", feature.getId(), message));
   }

   public static void info(Identifiable feature, String message, Object... params) {
      LOGGER.info(String.format("[%s] %s", feature.getId(), message), params);
   }

   public static void error(String message) {
      LOGGER.error(message);
   }

   public static void error(String message, Object... params) {
      LOGGER.error(message, params);
   }

   public static void error(Identifiable feature, String message) {
      LOGGER.error(String.format("[%s] %s", feature.getId(), message));
   }

   public static void error(Identifiable feature, String message, Object... params) {
      LOGGER.error(String.format("[%s] %s", feature.getId(), message), params);
   }
}
