package dev.realme.ash.mixin.accessor;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({ClientWorld.class})
public interface AccessorClientWorld {
   @Invoker("playSound")
   void hookPlaySound(double var1, double var3, double var5, SoundEvent var7, SoundCategory var8, float var9, float var10, boolean var11, long var12);

   @Invoker("getPendingUpdateManager")
   PendingUpdateManager hookGetPendingUpdateManager();
}
