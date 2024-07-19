package dev.realme.ash.mixin.accessor;

import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LightmapTextureManager.class})
public interface AccessorLightmapTextureManager {
   @Accessor("dirty")
   void setUpdateLightmap(boolean var1);
}
