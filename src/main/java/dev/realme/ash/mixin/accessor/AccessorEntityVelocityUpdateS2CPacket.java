package dev.realme.ash.mixin.accessor;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({EntityVelocityUpdateS2CPacket.class})
public interface AccessorEntityVelocityUpdateS2CPacket {
   @Accessor("velocityX")
   @Mutable
   void setVelocityX(int var1);

   @Accessor("velocityY")
   @Mutable
   void setVelocityY(int var1);

   @Accessor("velocityZ")
   @Mutable
   void setVelocityZ(int var1);
}
