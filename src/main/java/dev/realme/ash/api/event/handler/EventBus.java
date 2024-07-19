// Decompiled with: CFR 0.152
// Class Version: 17
package dev.realme.ash.api.event.handler;

import dev.realme.ash.api.event.Event;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.event.listener.Listener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus
        implements EventHandler {
   private final Set<Object> subscribers = Collections.synchronizedSet(new HashSet<>());
   private final Map<Object, PriorityQueue<Listener>> listeners = new ConcurrentHashMap<>();

   @Override
   public void subscribe(Object obj) {
      if (this.subscribers.contains(obj)) {
         return;
      }
      this.subscribers.add(obj);
      for (Method method : obj.getClass().getMethods()) {
         Class<?>[] params;
         method.trySetAccessible();
         if (!method.isAnnotationPresent(EventListener.class)) continue;
         EventListener listener = method.getAnnotation(EventListener.class);
         if (method.getReturnType() != Void.TYPE || (params = method.getParameterTypes()).length != 1) continue;
         PriorityQueue<Listener> active = this.listeners.computeIfAbsent(params[0], v -> new PriorityQueue<>());
         active.add(new Listener(method, obj, listener.receiveCanceled(), listener.priority()));
      }
   }

   @Override
   public void unsubscribe(Object obj) {
      if (this.subscribers.remove(obj)) {
         this.listeners.values().forEach(set -> set.removeIf(l -> l.getSubscriber() == obj));
         this.listeners.entrySet().removeIf(e -> e.getValue().isEmpty());
      }
   }

   @Override
   public boolean dispatch(Event event) {
      if (event == null) {
         return false;
      }
      PriorityQueue<Listener> active = this.listeners.get(event.getClass());
      if (active == null || active.isEmpty()) {
         return false;
      }
      for (Listener listener : new ArrayList<>(active)) {
         if (event.isCanceled() && !listener.isReceiveCanceled()) continue;
         listener.invokeSubscriber(event);
      }
      return event.isCanceled();
   }
}
