package dev.realme.ash.mixin.accessor;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ExplosionS2CPacket.class})
public interface AccessorExplosionS2CPacket {
   @Accessor("playerVelocityX")
   @Mutable
   void setPlayerVelocityX(float var1);

   @Accessor("playerVelocityY")
   @Mutable
   void setPlayerVelocityY(float var1);

   @Accessor("playerVelocityZ")
   @Mutable
   void setPlayerVelocityZ(float var1);
}
