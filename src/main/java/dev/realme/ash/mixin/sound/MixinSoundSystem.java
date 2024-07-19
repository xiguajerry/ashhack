package dev.realme.ash.mixin.sound;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.world.PlaySoundEvent;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SoundSystem.class})
public class MixinSoundSystem {
   @Inject(
      method = {"play(Lnet/minecraft/client/sound/SoundInstance;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onPlay(SoundInstance soundInstance, CallbackInfo info) {
      PlaySoundEvent event = new PlaySoundEvent(soundInstance);
      Ash.EVENT_HANDLER.dispatch(event);
      if (event.isCanceled()) {
         info.cancel();
      }

   }
}
