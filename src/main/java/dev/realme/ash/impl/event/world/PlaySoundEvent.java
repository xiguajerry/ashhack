package dev.realme.ash.impl.event.world;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.client.sound.SoundInstance;

@Cancelable
public class PlaySoundEvent extends Event {
   private final SoundInstance sound;

   public PlaySoundEvent(SoundInstance soundInstance) {
      this.sound = soundInstance;
   }

   public SoundInstance getSound() {
      return this.sound;
   }
}
