package dev.realme.ash.impl.event.render;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import java.awt.Color;

@Cancelable
public class AmbientColorEvent extends Event {
   private Color color;

   public Color getColor() {
      return this.color;
   }

   public void setColor(Color color) {
      this.color = color;
   }
}
