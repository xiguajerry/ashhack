package dev.realme.ash.mixin.texture;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({PlayerSkinProvider.FileCache.class})
public class MixinPlayerSkinProvider {
   @Shadow
   @Final
   private MinecraftProfileTexture.Type type;
}
