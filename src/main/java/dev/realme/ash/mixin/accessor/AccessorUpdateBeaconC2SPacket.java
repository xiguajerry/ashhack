package dev.realme.ash.mixin.accessor;

import java.util.Optional;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({UpdateBeaconC2SPacket.class})
public interface AccessorUpdateBeaconC2SPacket {
   @Accessor("primaryEffectId")
   @Mutable
   void setPrimaryEffect(Optional var1);

   @Accessor("secondaryEffectId")
   @Mutable
   void setSecondaryEffect(Optional var1);
}
