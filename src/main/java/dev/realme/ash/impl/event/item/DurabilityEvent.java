package dev.realme.ash.impl.event.item;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class DurabilityEvent extends Event {
   private int damage;

   public DurabilityEvent(int damage) {
      this.damage = damage;
   }

   public int getItemDamage() {
      return Math.max(0, this.damage);
   }

   public int getDamage() {
      return this.damage;
   }

   public void setDamage(int damage) {
      this.damage = damage;
   }
}
