package dev.realme.ash.impl.event.config;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.event.StageEvent;

public class ConfigUpdateEvent extends StageEvent {
   private final Config config;

   public ConfigUpdateEvent(Config config) {
      this.config = config;
   }

   public Config getConfig() {
      return this.config;
   }
}
