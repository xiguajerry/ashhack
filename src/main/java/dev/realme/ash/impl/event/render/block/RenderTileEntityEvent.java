package dev.realme.ash.impl.event.render.block;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;

public class RenderTileEntityEvent extends Event {
   @Cancelable
   public static class EnchantingTableBook extends RenderTileEntityEvent {
   }
}
