package dev.realme.ash.impl.event.text;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

@Cancelable
public class TextVisitEvent extends Event {
   private String text;

   public TextVisitEvent(String text) {
      this.text = text;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }
}
