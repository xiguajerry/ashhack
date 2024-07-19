package dev.realme.ash.mixin.network;

import com.mojang.authlib.GameProfile;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListEntry.class})
public class MixinPlayerListEntry implements Globals {
   @Unique
   private Identifier capeTexture;
   @Unique
   private boolean capeTextureLoaded;

   @Inject(
      method = {"<init>(Lcom/mojang/authlib/GameProfile;Z)V"},
      at = {@At("TAIL")}
   )
   private void hookInit(GameProfile profile, boolean secureChatEnforced, CallbackInfo ci) {
      if (!this.capeTextureLoaded) {
         Managers.CAPES.loadPlayerCape(profile, (identifier) -> this.capeTexture = identifier);
         this.capeTextureLoaded = true;
      }
   }

   @Inject(
      method = {"getSkinTextures"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void hookGetSkinTextures(CallbackInfoReturnable cir) {
      if (this.capeTexture != null && Modules.CAPES.isEnabled() && Modules.CAPES.getOptifineConfig().getValue()) {
         SkinTextures t = (SkinTextures)cir.getReturnValue();
         SkinTextures customCapeTexture = new SkinTextures(t.texture(), t.textureUrl(), this.capeTexture, this.capeTexture, t.model(), t.secure());
         cir.setReturnValue(customCapeTexture);
      }

   }
}
