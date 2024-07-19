package dev.realme.ash.mixin.accessor;

import net.minecraft.entity.LimbAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LimbAnimator.class})
public interface AccessorLimbAnimator {
   @Accessor("pos")
   void hookSetPos(float var1);

   @Accessor("speed")
   void hookSetSpeed(float var1);
}
