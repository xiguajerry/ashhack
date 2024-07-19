package dev.realme.ash.impl.event.gui.hud;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import java.util.UUID;
import net.minecraft.text.Text;

@Cancelable
public class PlayerListNameEvent extends Event {
   private Text playerName;
   private final UUID id;

   public PlayerListNameEvent(Text playerName, UUID id) {
      this.playerName = playerName;
      this.id = id;
   }

   public void setPlayerName(Text playerName) {
      this.playerName = playerName;
   }

   public Text getPlayerName() {
      return this.playerName;
   }

   public UUID getId() {
      return this.id;
   }
}
