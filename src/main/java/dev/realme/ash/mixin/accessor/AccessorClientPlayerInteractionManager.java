package dev.realme.ash.mixin.accessor;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({ClientPlayerInteractionManager.class})
public interface AccessorClientPlayerInteractionManager {
   @Invoker("syncSelectedSlot")
   void hookSyncSelectedSlot();

   @Accessor("currentBreakingProgress")
   float hookGetCurrentBreakingProgress();

   @Accessor("currentBreakingProgress")
   void hookSetCurrentBreakingProgress(float var1);
}
