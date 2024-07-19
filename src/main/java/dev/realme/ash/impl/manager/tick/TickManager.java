package dev.realme.ash.impl.manager.tick;

import com.google.common.collect.Lists;
import dev.realme.ash.Ash;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.render.TickCounterEvent;
import dev.realme.ash.util.EvictingQueue;
import dev.realme.ash.util.Globals;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class TickManager implements Globals {
   private final Deque ticks = new EvictingQueue(20);
   private long time;
   private float clientTick = 1.0F;

   public TickManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   @EventListener
   public void onPacketInbound(PacketEvent.Receive event) {
      if (mc.player != null && mc.world != null) {
         if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            float last = 20000.0F / (float)(System.currentTimeMillis() - this.time);
            this.ticks.addFirst(last);
            this.time = System.currentTimeMillis();
         }

      }
   }

   public void setClientTick(float ticks) {
      this.clientTick = ticks;
   }

   @EventListener
   public void onTickCounter(TickCounterEvent event) {
      if (this.clientTick != 1.0F) {
         event.cancel();
         event.setTicks(this.clientTick);
      }

   }

   public Queue getTicks() {
      return this.ticks;
   }

   public float getTpsAverage() {
      float avg = 0.0F;

      try {
         ArrayList ticksCopy = Lists.newArrayList(this.ticks);
         if (!ticksCopy.isEmpty()) {
            float t;
            for(Iterator var3 = ticksCopy.iterator(); var3.hasNext(); avg += t) {
               t = (Float)var3.next();
            }

            avg /= (float)ticksCopy.size();
         }
      } catch (NullPointerException var5) {
      }

      return Math.min(100.0F, avg);
   }

   public float getTpsCurrent() {
      try {
         if (!this.ticks.isEmpty()) {
            return (Float)this.ticks.getFirst();
         }
      } catch (NoSuchElementException var2) {
      }

      return 20.0F;
   }

   public float getTpsMin() {
      float min = 20.0F;

      try {
         Iterator var2 = this.ticks.iterator();

         while(var2.hasNext()) {
            float t = (Float)var2.next();
            if (t < min) {
               min = t;
            }
         }
      } catch (NullPointerException var4) {
      }

      return min;
   }

   public float getTickSync(TickSync tps) {
      float var10000;
      switch (tps) {
         case AVERAGE -> var10000 = this.getTpsAverage();
         case CURRENT -> var10000 = this.getTpsCurrent();
         case MINIMAL -> var10000 = this.getTpsMin();
         case NONE -> var10000 = 20.0F;
         default -> throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
