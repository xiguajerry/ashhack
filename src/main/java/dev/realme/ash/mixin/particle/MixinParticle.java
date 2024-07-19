package dev.realme.ash.mixin.particle;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({Particle.class})
public abstract class MixinParticle {
   @Shadow
   public abstract void setColor(float var1, float var2, float var3);
}
