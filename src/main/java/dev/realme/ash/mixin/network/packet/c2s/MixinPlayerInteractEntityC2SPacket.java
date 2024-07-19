package dev.realme.ash.mixin.network.packet.c2s;

import dev.realme.ash.impl.imixin.IPlayerInteractEntityC2SPacket;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.network.InteractType;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({PlayerInteractEntityC2SPacket.class})
public abstract class MixinPlayerInteractEntityC2SPacket implements IPlayerInteractEntityC2SPacket, Globals {
   @Shadow
   @Final
   private int entityId;

   @Shadow
   public abstract void write(PacketByteBuf var1);

   public Entity getEntity() {
      return mc.world.getEntityById(this.entityId);
   }

   public InteractType getType() {
      PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
      this.write(packetBuf);
      packetBuf.readVarInt();
      return packetBuf.readEnumConstant(InteractType.class);
   }
}
