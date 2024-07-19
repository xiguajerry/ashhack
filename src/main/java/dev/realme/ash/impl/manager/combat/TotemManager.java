package dev.realme.ash.impl.manager.combat;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.impl.event.entity.EntityDeathEvent;
import dev.realme.ash.impl.event.entity.TotemPopEvent;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.util.Globals;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class TotemManager implements Globals {
   private final ConcurrentMap totems = new ConcurrentHashMap();
   public final ArrayList deadPlayer = new ArrayList();

   public TotemManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   @EventListener
   public void onPacketInbound(PacketEvent.Receive event) {
      if (mc.world != null) {
         Packet var3 = event.getPacket();
         if (var3 instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket packet = (EntityStatusS2CPacket)var3;
            if (packet.getStatus() == 35) {
               Entity entity = packet.getEntity(mc.world);
               if (entity != null && entity.isAlive() && entity instanceof PlayerEntity) {
                  Ash.EVENT_HANDLER.dispatch(new TotemPopEvent((PlayerEntity)entity));
                  this.totems.put(entity.getUuid(), this.totems.containsKey(entity.getUuid()) ? (Integer)this.totems.get(entity.getUuid()) + 1 : 1);
               }
            }
         }
      }

   }

   @EventListener
   public void onUpdateWalking(UpdateWalkingEvent event) {
      if (mc.player != null && mc.world != null) {
         Iterator var2 = mc.world.getPlayers().iterator();

         while(true) {
            while(var2.hasNext()) {
               PlayerEntity player = (PlayerEntity)var2.next();
               if (player != null && !player.isAlive()) {
                  if (!this.deadPlayer.contains(player)) {
                     Ash.EVENT_HANDLER.dispatch(new EntityDeathEvent(player));
                     this.deadPlayer.add(player);
                  }
               } else {
                  this.deadPlayer.remove(player);
               }
            }

            return;
         }
      }
   }

   @EventListener(
      priority = Integer.MIN_VALUE
   )
   public void onRemoveEntity(EntityDeathEvent event) {
      this.totems.remove(event.getEntity().getUuid());
   }

   @EventListener
   public void onDisconnect(DisconnectEvent event) {
      this.totems.clear();
   }

   public int getTotems(Entity entity) {
      return (Integer)this.totems.getOrDefault(entity.getUuid(), 0);
   }
}
