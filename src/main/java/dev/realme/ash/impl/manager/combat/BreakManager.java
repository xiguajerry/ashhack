package dev.realme.ash.impl.manager.combat;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.Globals;
import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;

public class BreakManager implements Globals {
   public final HashMap breakMap = new HashMap();

   public BreakManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   @EventListener
   public void onPacketInbound(PacketEvent.Receive event) {
      if (mc.player != null && mc.world != null) {
         Packet var3 = event.getPacket();
         if (var3 instanceof BlockBreakingProgressS2CPacket packet) {
             if (packet.getPos() == null) {
               return;
            }

            BreakData breakData = new BreakData(packet.getPos(), packet.getEntityId());
            if (this.breakMap.containsKey(packet.getEntityId()) && ((BreakData)this.breakMap.get(packet.getEntityId())).pos.equals(packet.getPos())) {
               return;
            }

            if (breakData.getEntity() == null) {
               return;
            }

            this.breakMap.put(packet.getEntityId(), breakData);
         }

      }
   }

   public boolean isFriendMining(BlockPos pos) {
      boolean mining = false;

       for (Object o : (new HashMap(this.breakMap)).values()) {
           BreakData breakData = (BreakData) o;
           if (breakData.getEntity() != null && Managers.SOCIAL.isFriend(breakData.getEntity().getName().getString()) && breakData.pos.equals(pos)) {
               mining = true;
               break;
           }
       }

      return mining;
   }

   public boolean isMining(BlockPos pos) {
      boolean mining = false;

       for (Object o : (new HashMap(this.breakMap)).values()) {
           BreakData breakData = (BreakData) o;
           if (breakData.getEntity() != null && breakData.pos.equals(pos)) {
               mining = true;
               break;
           }
       }

      return mining;
   }

   public record BreakData(BlockPos pos, int entityId) {
      public BreakData(BlockPos pos, int entityId) {
         this.pos = pos;
         this.entityId = entityId;
      }

      public Entity getEntity() {
         Entity entity = Globals.mc.world.getEntityById(this.entityId);
         return entity instanceof PlayerEntity ? entity : null;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public int entityId() {
         return this.entityId;
      }
   }
}
