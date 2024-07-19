package dev.realme.ash.impl.event;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.client.render.Camera;

@Cancelable
public class PerspectiveEvent extends Event {
   public final Camera camera;

   public PerspectiveEvent(Camera camera) {
      this.camera = camera;
   }

   public Camera getCamera() {
      return this.camera;
   }
}
