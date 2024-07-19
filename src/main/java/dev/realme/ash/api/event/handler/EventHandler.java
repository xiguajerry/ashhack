package dev.realme.ash.api.event.handler;

import dev.realme.ash.api.event.Event;

public interface EventHandler {
   void subscribe(Object var1);

   void unsubscribe(Object var1);

   boolean dispatch(Event var1);
}
