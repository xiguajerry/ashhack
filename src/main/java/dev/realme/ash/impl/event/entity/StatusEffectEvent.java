package dev.realme.ash.impl.event.entity;

import dev.realme.ash.api.event.Event;
import net.minecraft.entity.effect.StatusEffectInstance;

public class StatusEffectEvent extends Event {
   private final StatusEffectInstance statusEffectInstance;

   public StatusEffectEvent(StatusEffectInstance statusEffectInstance) {
      this.statusEffectInstance = statusEffectInstance;
   }

   public StatusEffectInstance getStatusEffect() {
      return this.statusEffectInstance;
   }

   public static class Remove extends StatusEffectEvent {
      public Remove(StatusEffectInstance statusEffectInstance) {
         super(statusEffectInstance);
      }
   }

   public static class Add extends StatusEffectEvent {
      public Add(StatusEffectInstance statusEffectInstance) {
         super(statusEffectInstance);
      }
   }
}
