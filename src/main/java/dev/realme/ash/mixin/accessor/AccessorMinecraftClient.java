package dev.realme.ash.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({MinecraftClient.class})
public interface AccessorMinecraftClient {
   @Accessor("itemUseCooldown")
   void hookSetItemUseCooldown(int var1);

   @Accessor("itemUseCooldown")
   int hookGetItemUseCooldown();

   @Accessor("attackCooldown")
   void hookSetAttackCooldown(int var1);

   @Accessor("session")
   @Final
   @Mutable
   void setSession(Session var1);
}
