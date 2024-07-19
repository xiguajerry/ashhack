package dev.realme.ash.mixin.accessor;

import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({PlayerSkinProvider.class})
public interface AccessorPlayerSkinProvider {
   @Accessor("skinCache")
   PlayerSkinProvider.FileCache getSkinCacheDir();
}
